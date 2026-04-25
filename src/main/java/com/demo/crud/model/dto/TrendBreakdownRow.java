package com.demo.crud.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrendBreakdownRow {
    private String label;
    private List<Double> values;  // index 0 = YTD, index 1..12 = Jan..Dec
}
