package com.demo.crud.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MetricBreakdownData {
    private Long id;
    private Integer metricId;
    private Integer year;
    private Integer month;
    private String dimensionType;    // category | subcategory | brand | asin
    private String dimensionValue;
    private String productTitle;
    private BigDecimal actual;
    private BigDecimal jbpGoal;
}
