package com.demo.crud.mapper;

import com.demo.crud.model.MetricMonthlyData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/schema-test.sql")
@Transactional
class MetricMonthlyDataMapperTest {

    @Autowired
    private MetricMonthlyDataMapper mapper;

    @Test
    @DisplayName("findByMetricAndYear: 返回该指标当年所有月份数据")
    void findByMetricAndYear_returnsAllMonthsForMetric() {
        List<MetricMonthlyData> data = mapper.findByMetricAndYear(1, 2024);

        assertThat(data).hasSize(12);
        assertThat(data).extracting(MetricMonthlyData::getMonth)
                .containsExactlyInAnyOrder(1,2,3,4,5,6,7,8,9,10,11,12);
    }

    @Test
    @DisplayName("findByMetricAndYear: 2025年只有1-4月数据")
    void findByMetricAndYear_2025_hasFourMonths() {
        List<MetricMonthlyData> data = mapper.findByMetricAndYear(1, 2025);

        assertThat(data).hasSize(4);
    }

    @Test
    @DisplayName("findByMetricAndYear: 不存在的指标返回空列表")
    void findByMetricAndYear_returnsEmptyForNonExistentMetric() {
        List<MetricMonthlyData> data = mapper.findByMetricAndYear(999, 2025);

        assertThat(data).isEmpty();
    }

    @Test
    @DisplayName("findByMetricYearMonth: 返回单条正确记录")
    void findByMetricYearMonth_returnsSingleRecord() {
        MetricMonthlyData d = mapper.findByMetricYearMonth(1, 2025, 4);

        assertThat(d).isNotNull();
        assertThat(d.getMetricId()).isEqualTo(1);
        assertThat(d.getYear()).isEqualTo(2025);
        assertThat(d.getMonth()).isEqualTo(4);
        assertThat(d.getActual()).isNotNull();
        assertThat(d.getJbpGoal()).isNotNull();
    }

    @Test
    @DisplayName("findByMetricYearMonth: 不存在的月份返回 null")
    void findByMetricYearMonth_returnsNullWhenNotFound() {
        MetricMonthlyData d = mapper.findByMetricYearMonth(1, 2025, 12);

        assertThat(d).isNull();
    }

    @Test
    @DisplayName("findLatestMonth: 返回有数据的最大月份")
    void findLatestMonth_returnsMaxMonth() {
        Integer latest = mapper.findLatestMonth(2025);

        assertThat(latest).isEqualTo(4);
    }

    @Test
    @DisplayName("findLatestMonth: 2024年返回12")
    void findLatestMonth_2024_returns12() {
        Integer latest = mapper.findLatestMonth(2024);

        assertThat(latest).isEqualTo(12);
    }

    @Test
    @DisplayName("findLatestMonth: 无数据年份返回 null")
    void findLatestMonth_returnsNullForEmptyYear() {
        Integer latest = mapper.findLatestMonth(2099);

        assertThat(latest).isNull();
    }

    @Test
    @DisplayName("测试 category 字段")
    void testCategoryField() {
        MetricMonthlyData data = new MetricMonthlyData();
        data.setCategory("Test Category");
        assertThat(data.getCategory()).isEqualTo("Test Category");
    }
}
