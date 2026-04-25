package com.demo.crud.model;

import lombok.Data;

@Data
public class Metric {
    private Integer id;
    private String metricKey;
    private String metricName;
    private String metricType;      // financial | percentage | integer
    private Integer sortOrder;
    private String badDirection;    // up | down
}
