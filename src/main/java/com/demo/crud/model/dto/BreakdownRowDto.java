package com.demo.crud.model.dto;

import lombok.Data;

@Data
public class BreakdownRowDto {
    private String dimensionValue;
    private String productTitle;
    private Boolean isTotal;
    private BreakdownDataDto current;
    private BreakdownDataDto ytd;
}
