package com.demo.crud.model.dto;

import lombok.Data;

@Data
public class OverviewMetricDto {
    private Integer metricId;
    private String metricKey;
    private String metricName;
    private String metricType;
    private String badDirection;
    private PeriodDataDto current;
    private PeriodDataDto ytd;
}
