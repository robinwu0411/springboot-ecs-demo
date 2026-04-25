package com.demo.crud.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class DimensionBreakdownResponse {
    private String type;           // category | subcategory | brand | asin
    private Integer metricId;
    private String metricName;
    private String metricType;
    private String period;
    private List<BreakdownRowDto> rows;
}
