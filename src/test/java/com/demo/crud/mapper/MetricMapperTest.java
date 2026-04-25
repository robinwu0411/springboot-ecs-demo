package com.demo.crud.mapper;

import com.demo.crud.model.Metric;
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
class MetricMapperTest {

    @Autowired
    private MetricMapper metricMapper;

    @Test
    @DisplayName("findAll: 返回所有指标，按 sort_order 排序")
    void findAll_returnsAllMetricsOrderedBySortOrder() {
        List<Metric> metrics = metricMapper.findAll();

        assertThat(metrics).hasSize(5);
        assertThat(metrics.get(0).getMetricKey()).isEqualTo("revenue");
        assertThat(metrics.get(1).getMetricKey()).isEqualTo("net_ppm");
        assertThat(metrics.get(4).getMetricKey()).isEqualTo("net_ordered_gms");
    }

    @Test
    @DisplayName("findAll: 每条记录包含所有必要字段")
    void findAll_recordHasAllFields() {
        Metric m = metricMapper.findAll().get(0);

        assertThat(m.getId()).isNotNull();
        assertThat(m.getMetricKey()).isNotBlank();
        assertThat(m.getMetricName()).isNotBlank();
        assertThat(m.getMetricType()).isNotBlank();
        assertThat(m.getBadDirection()).isNotBlank();
    }

    @Test
    @DisplayName("findById: 返回正确的指标")
    void findById_returnsCorrectMetric() {
        Metric m = metricMapper.findById(3);

        assertThat(m).isNotNull();
        assertThat(m.getMetricKey()).isEqualTo("deal_ops");
        assertThat(m.getMetricName()).isEqualTo("Deal OPS");
        assertThat(m.getMetricType()).isEqualTo("financial");
    }

    @Test
    @DisplayName("findById: 不存在的 id 返回 null")
    void findById_returnsNullForNonExistentId() {
        Metric m = metricMapper.findById(999);

        assertThat(m).isNull();
    }
}
