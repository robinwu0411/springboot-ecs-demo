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
}
