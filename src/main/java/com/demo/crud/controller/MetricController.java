package com.demo.crud.controller;

import com.demo.crud.model.Result;
import com.demo.crud.model.dto.DimensionBreakdownResponse;
import com.demo.crud.model.dto.OverviewResponse;
import com.demo.crud.model.dto.TrendBreakdownResponse;
import com.demo.crud.service.MetricService;
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
    @Operation(summary = "总览页数据", description = "返回所有指标的当期、MoM、YoY、YTD数据")
    public Result<OverviewResponse> overview(
            @Parameter(description = "年份，默认当年") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份，默认最新有数据月份") @RequestParam(required = false) Integer month,
            @Parameter(description = "类别过滤，为空不过滤") @RequestParam(required = false) List<String> categories) {
        return Result.success(metricService.getOverview(year, month, categories));
    }

    @GetMapping("/breakdown")
    @Operation(summary = "指标详情页数据")
    public Result<?> breakdown(
            @Parameter(description = "指标ID", required = true) @RequestParam Integer metricId,
            @Parameter(description = "视图类型：trend/category/subcategory/brand/asin") @RequestParam(defaultValue = "trend") String type,
            @Parameter(description = "聚合维度，月度=monthly") @RequestParam(defaultValue = "monthly") String viewBy,
            @Parameter(description = "年份") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份") @RequestParam(required = false) Integer month) {
        if ("trend".equals(type)) {
            TrendBreakdownResponse resp = metricService.getTrendBreakdown(metricId, viewBy, year, month);
            return Result.success(resp);
        } else {
            DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(metricId, type, year, month);
            return Result.success(resp);
        }
    }
}
