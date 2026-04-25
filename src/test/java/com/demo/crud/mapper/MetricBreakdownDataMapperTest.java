package com.demo.crud.mapper;

import com.demo.crud.model.MetricBreakdownData;
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
class MetricBreakdownDataMapperTest {

    @Autowired
    private MetricBreakdownDataMapper mapper;

    @Test
    @DisplayName("findByMetricYearMonthType: category 返回4条数据")
    void findByMetricYearMonthType_category_returnsFourRows() {
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthType(1, 2025, 4, "category");

        assertThat(rows).hasSize(4);
        assertThat(rows).extracting(MetricBreakdownData::getDimensionType)
                .containsOnly("category");
    }

    @Test
    @DisplayName("findByMetricYearMonthType: 每条记录包含必要字段")
    void findByMetricYearMonthType_recordHasAllFields() {
        MetricBreakdownData row = mapper.findByMetricYearMonthType(1, 2025, 4, "category").get(0);

        assertThat(row.getDimensionValue()).isNotBlank();
        assertThat(row.getActual()).isNotNull();
        assertThat(row.getJbpGoal()).isNotNull();
    }

    @Test
    @DisplayName("findByMetricYearMonthType: asin 类型包含 productTitle")
    void findByMetricYearMonthType_asin_hasProductTitle() {
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthType(1, 2025, 4, "asin");

        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).getProductTitle()).isNotBlank();
    }

    @Test
    @DisplayName("findByMetricYearMonthType: 不存在的维度类型返回空列表")
    void findByMetricYearMonthType_unknownType_returnsEmpty() {
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthType(1, 2025, 4, "subcategory");

        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("findByMetricYearMonthType: 不存在的指标返回空列表")
    void findByMetricYearMonthType_unknownMetric_returnsEmpty() {
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthType(999, 2025, 4, "category");

        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("findByMetricYearMonthRangeType: 月份范围查询返回多月数据")
    void findByMetricYearMonthRangeType_returnsMultipleMonths() {
        // Schema has data for 2025 months 3 and 4
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthRangeType(1, 2025, 1, 4, "category");

        // 4 rows × 2 months (march + april) = 8
        assertThat(rows).hasSize(8);
    }

    @Test
    @DisplayName("findByMetricYearMonthRangeType: 无数据时返回空列表")
    void findByMetricYearMonthRangeType_emptyForMissingData() {
        List<MetricBreakdownData> rows = mapper.findByMetricYearMonthRangeType(1, 2099, 1, 12, "category");

        assertThat(rows).isEmpty();
    }
}
