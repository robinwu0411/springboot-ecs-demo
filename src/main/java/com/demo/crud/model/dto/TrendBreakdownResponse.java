package com.demo.crud.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrendBreakdownResponse {
    private String type;           // "trend"
    private Integer metricId;
    private String metricName;
    private String metricType;
    private String viewBy;
    private List<String> chartCategories;    // ["Jan".."Dec"]
    private List<ChartSeries> chartSeries;   // 3 series
    private List<String> tableColumns;       // ["Year-to-date","January".."December"]
    private List<TrendBreakdownRow> tableRows;
}
