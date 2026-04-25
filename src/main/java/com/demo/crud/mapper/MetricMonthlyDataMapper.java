package com.demo.crud.mapper;

import com.demo.crud.model.MetricMonthlyData;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MetricMonthlyDataMapper {
    List<MetricMonthlyData> findByMetricAndYear(@Param("metricId") int metricId, @Param("year") int year);
    MetricMonthlyData findByMetricYearMonth(@Param("metricId") int metricId, @Param("year") int year, @Param("month") int month);
    Integer findLatestMonth(@Param("year") int year);
    List<MetricMonthlyData> findByMetricYearMonthAndCategories(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("categories") List<String> categories
    );
    List<MetricMonthlyData> findByMetricYearAndCategories(
            @Param("metricId") int metricId,
            @Param("year") int year,
            @Param("categories") List<String> categories
    );
}
