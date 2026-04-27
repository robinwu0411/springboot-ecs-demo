package com.demo.crud.mapper;

import com.demo.crud.model.MetricBreakdownData;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MetricBreakdownDataMapper {
    List<MetricBreakdownData> findByMetricYearMonthType(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("dimensionType") String dimensionType);

    List<MetricBreakdownData> findByMetricYearMonthRangeType(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("monthFrom") int monthFrom,
            @Param("monthTo") int monthTo,
            @Param("dimensionType") String dimensionType);

    List<MetricBreakdownData> findByMetricYearMonthTypeAndCategories(
            @Param("metricId") Integer metricId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("dimensionType") String dimensionType,
            @Param("categories") List<String> categories);

    List<MetricBreakdownData> findByMetricYearMonthRangeTypeAndCategories(
            @Param("metricId") Integer metricId,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth,
            @Param("dimensionType") String dimensionType,
            @Param("categories") List<String> categories);

    List<MetricBreakdownData> findByMetricAndYear(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("dimensionType") String dimensionType);

    List<MetricBreakdownData> findByMetricYearAndCategories(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("dimensionType") String dimensionType,
            @Param("categories") List<String> categories);
}
