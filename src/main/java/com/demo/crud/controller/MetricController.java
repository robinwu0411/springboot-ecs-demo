package com.demo.crud.controller;

import com.demo.crud.model.Result;
import com.demo.crud.model.dto.DimensionBreakdownResponse;
import com.demo.crud.model.dto.OverviewResponse;
import com.demo.crud.model.dto.TrendBreakdownResponse;
import com.demo.crud.service.MetricService;
import java.util.Arrays;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metric")
@RequiredArgsConstructor
@Tag(name = "指标看板", description = "Scorecard 指标接口")
public class MetricController {

    private final MetricService metricService;

    @GetMapping("/overview")
    @Operation(summary = "总览页数据", description = "返回所有指标的当期、MoM、YoY、YTD数据，支持按类别筛选")
    public Result<OverviewResponse> overview(
            @Parameter(description = "年份，必填") @RequestParam(required = true) Integer year,
            @Parameter(description = "月份，必填") @RequestParam(required = true) Integer month,
            @Parameter(description = "类别筛选，逗号分隔") @RequestParam(required = false) String categories) {

        List<String> categoryList = (categories != null && !categories.isEmpty())
            ? Arrays.asList(categories.split(","))
            : null;

        return Result.success(metricService.getOverview(year, month, categoryList));
    }

    @GetMapping("/breakdown")
    @Operation(summary = "指标详情页数据")
    public Result<?> breakdown(
            @Parameter(description = "指标ID", required = true) @RequestParam Integer metricId,
            @Parameter(description = "视图类型") @RequestParam(defaultValue = "trend") String type,
            @Parameter(description = "年份，必填") @RequestParam(required = true) Integer year,
            @Parameter(description = "月份，必填") @RequestParam(required = true) Integer month,
            @Parameter(description = "聚合维度") @RequestParam(defaultValue = "monthly") String viewBy,
            @Parameter(description = "类别筛选，逗号分隔") @RequestParam(required = false) String categories) {

        List<String> categoryList = (categories != null && !categories.isEmpty())
            ? Arrays.asList(categories.split(","))
            : null;

        if ("trend".equals(type)) {
            TrendBreakdownResponse resp = metricService.getTrendBreakdown(metricId, viewBy, year, month);
            return Result.success(resp);
        } else {
            DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(metricId, type, year, month, categoryList);
            return Result.success(resp);
        }
    }
}
