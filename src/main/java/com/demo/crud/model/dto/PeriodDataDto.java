package com.demo.crud.model.dto;

import lombok.Data;

@Data
public class PeriodDataDto {
    private Double actual;
    private Double jbpGoal;
    private Double vsJbpGoalPct;
    private Double mom;
    private Double momPct;
    private Double yoy;
    private Double yoyPct;
}
