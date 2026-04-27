package com.demo.crud.service;

import com.demo.crud.mapper.MetricBreakdownDataMapper;
import com.demo.crud.mapper.MetricMapper;
import com.demo.crud.mapper.MetricMonthlyDataMapper;
import com.demo.crud.model.Metric;
import com.demo.crud.model.MetricBreakdownData;
import com.demo.crud.model.MetricMonthlyData;
import com.demo.crud.model.dto.DimensionBreakdownResponse;
import com.demo.crud.model.dto.OverviewResponse;
import com.demo.crud.model.dto.TrendBreakdownResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock private MetricMapper metricMapper;
    @Mock private MetricMonthlyDataMapper monthlyDataMapper;
    @Mock private MetricBreakdownDataMapper breakdownDataMapper;

    @InjectMocks private MetricService metricService;

    private Metric revenue;
    private MetricMonthlyData apr2025;
    private MetricMonthlyData mar2025;
    private MetricMonthlyData apr2024;

    @BeforeEach
    void setUp() {
        revenue = new Metric();
        revenue.setId(1); revenue.setMetricKey("revenue");
        revenue.setMetricName("Revenue"); revenue.setMetricType("financial");
        revenue.setSortOrder(1); revenue.setBadDirection("down");

        apr2025 = monthly(1, 2025, 4, "12918000", "10240000");
        mar2025 = monthly(1, 2025, 3, "13000000", "12500000");
        apr2024 = monthly(1, 2024, 4, "11500000", "11500000");
    }

    // ===================== getOverview =====================

    @Test
    @DisplayName("getOverview: 返回5个指标")
    void getOverview_returnsFiveMetrics() {
        List<Metric> five = List.of(revenue, metric(2), metric(3), metric(4), metric(5));
        when(metricMapper.findAll()).thenReturn(five);
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());
        when(monthlyDataMapper.findByMetricYearMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        assertThat(resp.getMetrics()).hasSize(5);
    }

    @Test
    @DisplayName("getOverview: period 格式正确")
    void getOverview_periodFormat() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());
        when(monthlyDataMapper.findByMetricYearMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        assertThat(resp.getPeriod()).isEqualTo("April 2025");
        assertThat(resp.getYear()).isEqualTo(2025);
        assertThat(resp.getMonth()).isEqualTo(4);
    }

    @Test
    @DisplayName("getOverview: vsJbpGoalPct 计算正确 = (actual-goal)/goal*100")
    void getOverview_vsJbpGoalPct_calculated() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(apr2025);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 3)).thenReturn(null);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2024, 4)).thenReturn(null);
        when(monthlyDataMapper.findByMetricAndYear(1, 2025)).thenReturn(List.of(apr2025));
        when(monthlyDataMapper.findByMetricAndYear(1, 2024)).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        // (12918000 - 10240000) / 10240000 * 100 = 26.15...
        Double vsJbp = resp.getMetrics().get(0).getCurrent().getVsJbpGoalPct();
        assertThat(vsJbp).isNotNull();
        assertThat(vsJbp).isCloseTo(26.15, within(0.1));
    }

    @Test
    @DisplayName("getOverview: JBP goal 为 0 时 vsJbpGoalPct 返回 null")
    void getOverview_vsJbpGoalPct_nullWhenGoalZero() {
        MetricMonthlyData zeroGoal = monthly(1, 2025, 4, "1000000", "0");
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(eq(1), anyInt(), anyInt())).thenReturn(null);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(zeroGoal);
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        assertThat(resp.getMetrics().get(0).getCurrent().getVsJbpGoalPct()).isNull();
    }

    @Test
    @DisplayName("getOverview: MoM 计算正确")
    void getOverview_mom_calculated() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(apr2025);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 3)).thenReturn(mar2025);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2024, 4)).thenReturn(null);
        when(monthlyDataMapper.findByMetricAndYear(1, 2025)).thenReturn(List.of(mar2025, apr2025));
        when(monthlyDataMapper.findByMetricAndYear(1, 2024)).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        // mom = 12918000 - 13000000 = -82000
        // momPct = -82000 / 13000000 * 100 = -0.63...
        assertThat(resp.getMetrics().get(0).getCurrent().getMom()).isCloseTo(-82000.0, within(1.0));
        assertThat(resp.getMetrics().get(0).getCurrent().getMomPct()).isCloseTo(-0.63, within(0.01));
    }

    @Test
    @DisplayName("getOverview: 上月无数据时 MoM 返回 null")
    void getOverview_mom_nullWhenPrevMissing() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(apr2025);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 3)).thenReturn(null);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2024, 4)).thenReturn(null);
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        assertThat(resp.getMetrics().get(0).getCurrent().getMom()).isNull();
        assertThat(resp.getMetrics().get(0).getCurrent().getMomPct()).isNull();
    }

    @Test
    @DisplayName("getOverview: YoY 计算正确")
    void getOverview_yoy_calculated() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(apr2025);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 3)).thenReturn(null);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2024, 4)).thenReturn(apr2024);
        when(monthlyDataMapper.findByMetricAndYear(1, 2025)).thenReturn(List.of(apr2025));
        when(monthlyDataMapper.findByMetricAndYear(1, 2024)).thenReturn(List.of(apr2024));

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        // yoy = 12918000 - 11500000 = 1418000
        // yoyPct = 1418000 / 11500000 * 100 = 12.33...
        assertThat(resp.getMetrics().get(0).getCurrent().getYoy()).isCloseTo(1418000.0, within(1.0));
        assertThat(resp.getMetrics().get(0).getCurrent().getYoyPct()).isCloseTo(12.33, within(0.01));
    }

    @Test
    @DisplayName("getOverview: 去年同期无数据时 YoY 返回 null")
    void getOverview_yoy_nullWhenLastYearMissing() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(monthlyDataMapper.findByMetricYearMonth(1, 2025, 4)).thenReturn(apr2025);
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        assertThat(resp.getMetrics().get(0).getCurrent().getYoy()).isNull();
    }

    @Test
    @DisplayName("getOverview: YTD actual = 当年1月至选定月累加")
    void getOverview_ytdActual_sumOfMonths1ToM() {
        MetricMonthlyData jan = monthly(1, 2025, 1, "11000000", "11500000");
        MetricMonthlyData feb = monthly(1, 2025, 2, "12000000", "12000000");
        MetricMonthlyData mar = monthly(1, 2025, 3, "13000000", "12500000");

        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findByMetricYearMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(monthlyDataMapper.findByMetricAndYear(1, 2025)).thenReturn(List.of(jan, feb, mar, apr2025));
        when(monthlyDataMapper.findByMetricAndYear(1, 2024)).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, null);

        // YTD = 11M + 12M + 13M + 12.918M = 48918000
        assertThat(resp.getMetrics().get(0).getYtd().getActual()).isCloseTo(48918000.0, within(1.0));
    }

    @Test
    @DisplayName("getOverview: 未传 year/month 时使用默认值")
    void getOverview_defaultYearAndMonth() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(monthlyDataMapper.findLatestMonth(anyInt())).thenReturn(4);
        when(monthlyDataMapper.findByMetricYearMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(null, null, null);

        assertThat(resp).isNotNull();
        assertThat(resp.getYear()).isNotNull();
        assertThat(resp.getMonth()).isNotNull();
    }

    @Test
    @DisplayName("getOverview: 无 categories 参数时查询所有类别")
    void getOverview_withoutCategories_queriesAllCategories() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), eq(2025), eq(4), eq("category")))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), eq(2025), eq(3), eq("category")))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), eq(2024), eq(4), eq("category")))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricAndYear(anyInt(), eq(2025), eq("category")))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricAndYear(anyInt(), eq(2024), eq("category")))
            .thenReturn(List.of());

        metricService.getOverview(2025, 4, null);

        // Verify it calls the method WITHOUT categories parameter
        verify(breakdownDataMapper, times(4)).findByMetricYearMonthType(
            anyInt(), eq(2025), eq(4), eq("category"));
    }

    @Test
    @DisplayName("getOverview: 空 categories 列表时查询所有类别")
    void getOverview_withEmptyCategories_queriesAllCategories() {
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), eq(2025), eq(4), eq("category")))
            .thenReturn(List.of());

        metricService.getOverview(2025, 4, List.of());

        // Should behave same as null
        verify(breakdownDataMapper, times(4)).findByMetricYearMonthType(
            anyInt(), eq(2025), eq(4), eq("category"));
    }

    @Test
    @DisplayName("getOverview: 有 categories 参数时查询指定类别")
    void getOverview_withCategories_queriesSpecificCategories() {
        List<String> categories = List.of("1080 Car Navigation", "1080 Car Recording");
        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(anyInt(), eq(2025), eq(4), eq("category"), eq(categories)))
            .thenReturn(List.of());

        metricService.getOverview(2025, 4, categories);

        // Verify it calls the method WITH categories parameter
        verify(breakdownDataMapper, times(4)).findByMetricYearMonthTypeAndCategories(
            anyInt(), eq(2025), eq(4), eq("category"), eq(categories));
    }

    @Test
    @DisplayName("getOverview: 单个类别时正确聚合")
    void getOverview_withSingleCategory_correctAggregation() {
        List<String> singleCat = List.of("1080 Car Navigation");
        List<MetricBreakdownData> navData = List.of(
            breakdown(1, 2025, 4, "category", "1080 Car Navigation", null,
                      new BigDecimal("3000000"), new BigDecimal("2500000"))
        );

        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2025, 4, "category", singleCat))
            .thenReturn(navData);
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2025, 3, "category", singleCat))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2024, 4, "category", singleCat))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearAndCategories(1, 2025, "category", singleCat))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearAndCategories(1, 2024, "category", singleCat))
            .thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, singleCat);

        // Should return single metric with the category's data
        assertThat(resp.getMetrics()).hasSize(1);
        assertThat(resp.getMetrics().get(0).getCurrent().getActual()).isCloseTo(3000000.0, within(1.0));
    }

    @Test
    @DisplayName("getOverview: 多个类别时正确聚合")
    void getOverview_withMultipleCategories_correctAggregation() {
        List<String> categories = List.of("1080 Car Navigation", "1080 Car Recording");
        List<MetricBreakdownData> navData = List.of(
            breakdown(1, 2025, 4, "category", "1080 Car Navigation", null,
                      new BigDecimal("3000000"), new BigDecimal("2500000"))
        );
        List<MetricBreakdownData> recData = List.of(
            breakdown(1, 2025, 4, "category", "1080 Car Recording", null,
                      new BigDecimal("2000000"), new BigDecimal("1800000"))
        );

        when(metricMapper.findAll()).thenReturn(List.of(revenue));
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2025, 4, "category", categories))
            .thenReturn(List.of(navData.get(0), recData.get(0)));
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2025, 3, "category", categories))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthTypeAndCategories(1, 2024, 4, "category", categories))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearAndCategories(1, 2025, "category", categories))
            .thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearAndCategories(1, 2024, "category", categories))
            .thenReturn(List.of());

        OverviewResponse resp = metricService.getOverview(2025, 4, categories);

        // Should sum the categories: 3M + 2M = 5M
        assertThat(resp.getMetrics()).hasSize(1);
        assertThat(resp.getMetrics().get(0).getCurrent().getActual()).isCloseTo(5000000.0, within(1.0));
        // Goals should also be summed: 2.5M + 1.8M = 4.3M
        assertThat(resp.getMetrics().get(0).getCurrent().getJbpGoal()).isCloseTo(4300000.0, within(1.0));
    }

    // ===================== getTrendBreakdown =====================

    @Test
    @DisplayName("getTrendBreakdown: 返回3条 chart series")
    void getTrendBreakdown_returnsThreeChartSeries() {
        MetricMonthlyData m = monthly(3, 2025, 4, "1180000", "1240000");
        when(metricMapper.findById(3)).thenReturn(dealOps());
        when(monthlyDataMapper.findByMetricAndYear(3, 2025)).thenReturn(List.of(m));
        when(monthlyDataMapper.findByMetricAndYear(3, 2024)).thenReturn(List.of());

        TrendBreakdownResponse resp = metricService.getTrendBreakdown(3, "monthly", 2025, 4);

        assertThat(resp.getType()).isEqualTo("trend");
        assertThat(resp.getChartSeries()).hasSize(3);
        assertThat(resp.getChartSeries().get(0).getName()).isEqualTo("2024 Actual");
        assertThat(resp.getChartSeries().get(1).getName()).isEqualTo("2025 Actual");
        assertThat(resp.getChartSeries().get(2).getName()).isEqualTo("2025 Target");
    }

    @Test
    @DisplayName("getTrendBreakdown: chart series 每条有12个月数据点")
    void getTrendBreakdown_chartSeriesHas12DataPoints() {
        when(metricMapper.findById(3)).thenReturn(dealOps());
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        TrendBreakdownResponse resp = metricService.getTrendBreakdown(3, "monthly", 2025, 4);

        resp.getChartSeries().forEach(s ->
                assertThat(s.getData()).hasSize(12));
    }

    @Test
    @DisplayName("getTrendBreakdown: 选定月份之后的数据点为 null")
    void getTrendBreakdown_futureMonthsAreNull() {
        MetricMonthlyData m = monthly(3, 2025, 4, "1180000", "1240000");
        when(metricMapper.findById(3)).thenReturn(dealOps());
        when(monthlyDataMapper.findByMetricAndYear(3, 2025)).thenReturn(List.of(m));
        when(monthlyDataMapper.findByMetricAndYear(3, 2024)).thenReturn(List.of());

        TrendBreakdownResponse resp = metricService.getTrendBreakdown(3, "monthly", 2025, 4);

        // index 4..11 (May..Dec of 2025 Actual) must be null
        List<Double> actual2025 = resp.getChartSeries().get(1).getData();
        for (int i = 4; i < 12; i++) {
            assertThat(actual2025.get(i)).as("Month %d should be null", i + 1).isNull();
        }
    }

    @Test
    @DisplayName("getTrendBreakdown: ytdTable 有5行")
    void getTrendBreakdown_ytdTableHasFiveRows() {
        when(metricMapper.findById(3)).thenReturn(dealOps());
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        TrendBreakdownResponse resp = metricService.getTrendBreakdown(3, "monthly", 2025, 4);

        assertThat(resp.getTableRows()).hasSize(5);
        assertThat(resp.getTableRows().get(0).getLabel()).isEqualTo("2025 Financial");
        assertThat(resp.getTableRows().get(1).getLabel()).isEqualTo("2025 JBP Goal");
        assertThat(resp.getTableRows().get(2).getLabel()).isEqualTo("+/- JBP Goal (Dollar)");
        assertThat(resp.getTableRows().get(3).getLabel()).isEqualTo("2025 YoY");
        assertThat(resp.getTableRows().get(4).getLabel()).isEqualTo("2024 Financial");
    }

    @Test
    @DisplayName("getTrendBreakdown: tableColumns 共13列（YTD + 12个月）")
    void getTrendBreakdown_tableColumnsCount() {
        when(metricMapper.findById(3)).thenReturn(dealOps());
        when(monthlyDataMapper.findByMetricAndYear(anyInt(), anyInt())).thenReturn(List.of());

        TrendBreakdownResponse resp = metricService.getTrendBreakdown(3, "monthly", 2025, 4);

        assertThat(resp.getTableColumns()).hasSize(13);
        assertThat(resp.getTableColumns().get(0)).isEqualTo("Year-to-date");
        assertThat(resp.getTableColumns().get(1)).isEqualTo("January");
        assertThat(resp.getTableColumns().get(12)).isEqualTo("December");
    }

    // ===================== getDimensionBreakdown =====================

    @Test
    @DisplayName("getDimensionBreakdown: 首行为 All 汇总行")
    void getDimensionBreakdown_firstRowIsTotal() {
        when(metricMapper.findById(1)).thenReturn(revenue);
        when(breakdownDataMapper.findByMetricYearMonthType(1, 2025, 4, "category"))
                .thenReturn(categoryRows(2025, 4));
        when(breakdownDataMapper.findByMetricYearMonthType(1, 2025, 3, "category"))
                .thenReturn(categoryRows(2025, 3));
        when(breakdownDataMapper.findByMetricYearMonthType(1, 2024, 4, "category"))
                .thenReturn(categoryRows(2024, 4));
        when(breakdownDataMapper.findByMetricYearMonthRangeType(eq(1), eq(2025), eq(1), eq(4), eq("category")))
                .thenReturn(categoryRows(2025, 4));

        DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(1, "category", 2025, 4, null);

        assertThat(resp.getRows()).isNotEmpty();
        assertThat(resp.getRows().get(0).getIsTotal()).isTrue();
        assertThat(resp.getRows().get(0).getDimensionValue()).isEqualTo("All");
    }

    @Test
    @DisplayName("getDimensionBreakdown: All 行 actual = 所有维度行之和")
    void getDimensionBreakdown_totalActualIsSumOfDims() {
        List<MetricBreakdownData> rows = categoryRows(2025, 4); // 4 rows × 3229500 = 12918000
        when(metricMapper.findById(1)).thenReturn(revenue);
        // generic first, specific last (Mockito picks last-registered matching stub)
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthType(1, 2025, 4, "category")).thenReturn(rows);
        when(breakdownDataMapper.findByMetricYearMonthRangeType(anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(rows);

        DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(1, "category", 2025, 4, null);

        assertThat(resp.getRows().get(0).getCurrent().getActual()).isCloseTo(12918000.0, within(1.0));
    }

    @Test
    @DisplayName("getDimensionBreakdown: type 为 asin 时包含 productTitle")
    void getDimensionBreakdown_asin_includesProductTitle() {
        MetricBreakdownData asinRow = new MetricBreakdownData();
        asinRow.setDimensionType("asin"); asinRow.setDimensionValue("B08XYZ1234");
        asinRow.setProductTitle("AutoVision 4K Dashcam Pro");
        asinRow.setActual(new BigDecimal("3229500")); asinRow.setJbpGoal(new BigDecimal("2560000"));

        when(metricMapper.findById(1)).thenReturn(revenue);
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthType(1, 2025, 4, "asin")).thenReturn(List.of(asinRow));
        when(breakdownDataMapper.findByMetricYearMonthRangeType(anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of(asinRow));

        DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(1, "asin", 2025, 4, null);

        assertThat(resp.getRows()).hasSizeGreaterThan(1); // All + 1 asin
        assertThat(resp.getRows().get(1).getProductTitle()).isEqualTo("AutoVision 4K Dashcam Pro");
    }

    @Test
    @DisplayName("getDimensionBreakdown: period 格式正确")
    void getDimensionBreakdown_periodFormat() {
        when(metricMapper.findById(1)).thenReturn(revenue);
        when(breakdownDataMapper.findByMetricYearMonthType(anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of());
        when(breakdownDataMapper.findByMetricYearMonthRangeType(anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of());

        DimensionBreakdownResponse resp = metricService.getDimensionBreakdown(1, "category", 2025, 4, null);

        assertThat(resp.getPeriod()).isEqualTo("April 2025");
        assertThat(resp.getType()).isEqualTo("category");
    }

    @Test
    @DisplayName("getDimensionBreakdown: 支持 categories 参数过滤")
    void testGetDimensionBreakdownWithCategories() {
        DimensionBreakdownResponse result = metricService.getDimensionBreakdown(1, "category", 2025, 4, List.of("TestCat"));
        assertNotNull(result);
    }

    // ===================== helpers =====================

    private MetricMonthlyData monthly(int metricId, int year, int month, String actual, String goal) {
        MetricMonthlyData d = new MetricMonthlyData();
        d.setMetricId(metricId); d.setYear(year); d.setMonth(month);
        d.setActual(new BigDecimal(actual)); d.setJbpGoal(new BigDecimal(goal));
        return d;
    }

    private Metric metric(int id) {
        Metric m = new Metric();
        m.setId(id); m.setMetricKey("metric_" + id); m.setMetricName("Metric " + id);
        m.setMetricType("financial"); m.setSortOrder(id); m.setBadDirection("down");
        return m;
    }

    private Metric dealOps() {
        Metric m = new Metric();
        m.setId(3); m.setMetricKey("deal_ops"); m.setMetricName("Deal OPS");
        m.setMetricType("financial"); m.setSortOrder(3); m.setBadDirection("down");
        return m;
    }

    private List<MetricBreakdownData> categoryRows(int year, int month) {
        BigDecimal actual = new BigDecimal("3229500");
        BigDecimal goal   = new BigDecimal("2560000");
        return List.of(
                breakdown(1, year, month, "category", "1080 Car Navigation", null, actual, goal),
                breakdown(1, year, month, "category", "1080 Car Recording",  null, actual, goal),
                breakdown(1, year, month, "category", "1200 Car Audio",       null, actual, goal),
                breakdown(1, year, month, "category", "1200 Dashboard Cam",   null, actual, goal)
        );
    }

    private MetricBreakdownData breakdown(int metricId, int year, int month,
                                          String type, String value, String title,
                                          BigDecimal actual, BigDecimal goal) {
        MetricBreakdownData d = new MetricBreakdownData();
        d.setMetricId(metricId); d.setYear(year); d.setMonth(month);
        d.setDimensionType(type); d.setDimensionValue(value); d.setProductTitle(title);
        d.setActual(actual); d.setJbpGoal(goal);
        return d;
    }
}
