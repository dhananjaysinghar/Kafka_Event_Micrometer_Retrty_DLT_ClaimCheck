package com.ex.kafka.common;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private String name;

    private String mobile;

    private String address;
}
