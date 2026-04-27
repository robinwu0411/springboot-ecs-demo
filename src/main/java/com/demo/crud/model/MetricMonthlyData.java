package com.demo.crud.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MetricMonthlyData {
    private Long id;
    private Integer metricId;
    private Integer year;
    private Integer month;
    private BigDecimal actual;
    private BigDecimal jbpGoal;
}
