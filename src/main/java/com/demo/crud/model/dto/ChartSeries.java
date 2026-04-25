package com.demo.crud.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChartSeries {
    private String name;
    private List<Double> data;
}
