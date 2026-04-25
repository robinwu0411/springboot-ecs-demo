package com.demo.crud.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class OverviewResponse {
    private String period;   // e.g. "April 2025"
    private Integer year;
    private Integer month;
    private List<OverviewMetricDto> metrics;
}
