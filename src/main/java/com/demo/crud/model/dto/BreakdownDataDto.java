package com.demo.crud.model.dto;

import lombok.Data;

@Data
public class BreakdownDataDto {
    private Double actual;
    private Double jbpGoal;
    private Double vsJbpGoalPct;
    private Double momCtcBps;
    private Double yoyCtcBps;
}
