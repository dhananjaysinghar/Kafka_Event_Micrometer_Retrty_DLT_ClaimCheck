package com.ex.kafka.common;

import com.ex.KafkaTestApplication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class CommonUtils {

    public static void storeInFile(String filePath, String data) {
        try {
            //Use any distributed storage location to store the data
            byte[] compressedData = CommonUtils.compress(data);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(compressedData, 0, compressedData.length);
            }
        } catch (Exception ex) {
            log.error("Exception Occurred during file storage");
        }
    }

    @SneakyThrows
    public static String readFromFile(String filePath) {
        return decompress(Files.readAllBytes(Paths.get(filePath)));
    }

    private static byte[] compress(final String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.flush();
        gzip.close();
        return obj.toByteArray();
    }

    private static String decompress(final byte[] compressed) throws IOException {
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
        } else {
            outStr.append(compressed);
        }
        return outStr.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    @SneakyThrows
    public static void startKafkaServer() {
        InetAddress localhost = InetAddress.getLocalHost();
        log.info("Starting docker container with KAFKA_BOOTSTRAP_SERVER: {}:9092", localhost.getHostAddress().trim());
        System.setProperty("SYSTEM_IP", localhost.getHostAddress().trim());
        new ProcessBuilder(
                "cmd.exe", "/c", "setx SYSTEM_IP \"" + localhost.getHostAddress().trim() + "\"");
        String fileLocation = KafkaTestApplication.class.getResource("/docker-compose.yml")
                .getPath().replace("/", "//").replaceFirst("//", "");
        Path path = Paths.get(fileLocation);
        List<String> lines = Files.readAllLines(path);
        lines = lines.stream()
                .map(e -> {
                    if (e.contains("SYSTEM_IP")) {
                        return e.replace("${SYSTEM_IP}", localhost.getHostAddress().trim());
                    }
                    return e;
                }).collect(Collectors.toList());
        Files.write(path, lines);

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "docker compose -f " + fileLocation + " up -d");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            log.info(line);
        }
        log.info("Wait for 10 sec for initial setup");
        TimeUnit.SECONDS.sleep(10);
    }
}
