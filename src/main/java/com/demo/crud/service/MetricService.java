package com.demo.crud.service;

import com.demo.crud.mapper.MetricBreakdownDataMapper;
import com.demo.crud.mapper.MetricMapper;
import com.demo.crud.mapper.MetricMonthlyDataMapper;
import com.demo.crud.model.Metric;
import com.demo.crud.model.MetricBreakdownData;
import com.demo.crud.model.MetricMonthlyData;
import com.demo.crud.model.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private static final List<String> MONTH_NAMES = List.of(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    private final MetricMapper metricMapper;
    private final MetricMonthlyDataMapper monthlyDataMapper;
    private final MetricBreakdownDataMapper breakdownDataMapper;

    public OverviewResponse getOverview(Integer year, Integer month, List<String> categories) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        int currentMonth;
        if (month != null) {
            currentMonth = month;
        } else {
            currentMonth = Optional.ofNullable(monthlyDataMapper.findLatestMonth(currentYear)).orElse(1);
        }

        List<Metric> metrics = metricMapper.findAll();
        List<OverviewMetricDto> dtos = new ArrayList<>();

        for (Metric metric : metrics) {
            int id = metric.getId();

            // Use new method that supports category filtering
            List<MetricMonthlyData> currentDataList = (categories == null || categories.isEmpty())
                ? (monthlyDataMapper.findByMetricYearMonth(id, currentYear, currentMonth) != null
                    ? List.of(monthlyDataMapper.findByMetricYearMonth(id, currentYear, currentMonth))
                    : List.of())
                : monthlyDataMapper.findByMetricYearMonthAndCategories(id, currentYear, currentMonth, categories);
            MetricMonthlyData current = sumCategoryData(currentDataList);

            // Similarly for prev, lastYear, thisYearData, lastYearData
            int prevYear = currentMonth == 1 ? currentYear - 1 : currentYear;
            int prevMonth = currentMonth == 1 ? 12 : currentMonth - 1;
            List<MetricMonthlyData> prevDataList = (categories == null || categories.isEmpty())
                ? (monthlyDataMapper.findByMetricYearMonth(id, prevYear, prevMonth) != null
                    ? List.of(monthlyDataMapper.findByMetricYearMonth(id, prevYear, prevMonth))
                    : List.of())
                : monthlyDataMapper.findByMetricYearMonthAndCategories(id, prevYear, prevMonth, categories);
            MetricMonthlyData prev = sumCategoryData(prevDataList);

            List<MetricMonthlyData> lastYearList = (categories == null || categories.isEmpty())
                ? (monthlyDataMapper.findByMetricYearMonth(id, currentYear - 1, currentMonth) != null
                    ? List.of(monthlyDataMapper.findByMetricYearMonth(id, currentYear - 1, currentMonth))
                    : List.of())
                : monthlyDataMapper.findByMetricYearMonthAndCategories(id, currentYear - 1, currentMonth, categories);
            MetricMonthlyData lastYear = sumCategoryData(lastYearList);

            List<MetricMonthlyData> thisYearDataList = (categories == null || categories.isEmpty())
                ? monthlyDataMapper.findByMetricAndYear(id, currentYear)
                : monthlyDataMapper.findByMetricYearAndCategories(id, currentYear, categories);
            List<MetricMonthlyData> lastYearDataList = (categories == null || categories.isEmpty())
                ? monthlyDataMapper.findByMetricAndYear(id, currentYear - 1)
                : monthlyDataMapper.findByMetricYearAndCategories(id, currentYear - 1, categories);

            OverviewMetricDto dto = new OverviewMetricDto();
            dto.setMetricId(id);
            dto.setMetricKey(metric.getMetricKey());
            dto.setMetricName(metric.getMetricName());
            dto.setMetricType(metric.getMetricType());
            dto.setBadDirection(metric.getBadDirection());
            dto.setCurrent(buildCurrentPeriodDto(current, prev, lastYear));
            dto.setYtd(buildYtdPeriodDto(thisYearDataList, lastYearDataList, currentMonth));
            dtos.add(dto);
        }

        OverviewResponse resp = new OverviewResponse();
        resp.setPeriod(formatPeriod(currentYear, currentMonth));
        resp.setYear(currentYear);
        resp.setMonth(currentMonth);
        resp.setMetrics(dtos);
        return resp;
    }

    public TrendBreakdownResponse getTrendBreakdown(Integer metricId, String viewBy, Integer year, Integer month) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        int currentMonth;
        if (month != null) {
            currentMonth = month;
        } else {
            currentMonth = Optional.ofNullable(monthlyDataMapper.findLatestMonth(currentYear)).orElse(1);
        }

        Metric metric = metricMapper.findById(metricId);
        List<MetricMonthlyData> thisYearData = monthlyDataMapper.findByMetricAndYear(metricId, currentYear);
        List<MetricMonthlyData> lastYearData = monthlyDataMapper.findByMetricAndYear(metricId, currentYear - 1);

        Map<Integer, MetricMonthlyData> thisYearMap = thisYearData.stream()
                .collect(Collectors.toMap(MetricMonthlyData::getMonth, d -> d));
        Map<Integer, MetricMonthlyData> lastYearMap = lastYearData.stream()
                .collect(Collectors.toMap(MetricMonthlyData::getMonth, d -> d));

        // Build 3 chart series with 12 data points each
        List<Double> lyActuals = new ArrayList<>();
        List<Double> tyActuals = new ArrayList<>();
        List<Double> tyTargets = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            MetricMonthlyData ly = lastYearMap.get(m);
            lyActuals.add(ly != null && ly.getActual() != null ? ly.getActual().doubleValue() : null);

            MetricMonthlyData ty = thisYearMap.get(m);
            tyActuals.add(m <= currentMonth && ty != null && ty.getActual() != null
                    ? ty.getActual().doubleValue() : null);
            tyTargets.add(ty != null && ty.getJbpGoal() != null ? ty.getJbpGoal().doubleValue() : null);
        }

        ChartSeries lySeries = new ChartSeries();
        lySeries.setName((currentYear - 1) + " Actual");
        lySeries.setData(lyActuals);

        ChartSeries tySeries = new ChartSeries();
        tySeries.setName(currentYear + " Actual");
        tySeries.setData(tyActuals);

        ChartSeries targetSeries = new ChartSeries();
        targetSeries.setName(currentYear + " Target");
        targetSeries.setData(tyTargets);

        // Build table columns: ["Year-to-date", "January", ..., "December"]
        List<String> columns = new ArrayList<>();
        columns.add("Year-to-date");
        columns.addAll(MONTH_NAMES);

        double ytdActual = sumActualUpTo(thisYearData, currentMonth);
        double ytdGoal = sumGoalUpTo(thisYearData, currentMonth);
        double ytdLyActual = sumActualUpTo(lastYearData, currentMonth);

        TrendBreakdownRow row0 = row(currentYear + " Financial", ytdActual,
                buildMonthValues(thisYearMap, d -> d.getActual() != null ? d.getActual().doubleValue() : null));
        TrendBreakdownRow row1 = row(currentYear + " JBP Goal", ytdGoal,
                buildMonthValues(thisYearMap, d -> d.getJbpGoal() != null ? d.getJbpGoal().doubleValue() : null));
        TrendBreakdownRow row2 = row("+/- JBP Goal (Dollar)", ytdActual - ytdGoal,
                buildDiffMonthValues(thisYearMap));
        TrendBreakdownRow row3 = row(currentYear + " YoY", ytdActual - ytdLyActual,
                buildYoyMonthValues(thisYearMap, lastYearMap));
        TrendBreakdownRow row4 = row((currentYear - 1) + " Financial", ytdLyActual,
                buildMonthValues(lastYearMap, d -> d.getActual() != null ? d.getActual().doubleValue() : null));

        TrendBreakdownResponse resp = new TrendBreakdownResponse();
        resp.setType("trend");
        resp.setMetricId(metricId);
        resp.setMetricName(metric != null ? metric.getMetricName() : "");
        resp.setMetricType(metric != null ? metric.getMetricType() : "");
        resp.setViewBy(viewBy);
        resp.setChartCategories(MONTH_NAMES);
        resp.setChartSeries(List.of(lySeries, tySeries, targetSeries));
        resp.setTableColumns(columns);
        resp.setTableRows(List.of(row0, row1, row2, row3, row4));
        return resp;
    }

    public DimensionBreakdownResponse getDimensionBreakdown(
            Integer metricId,
            String type,
            Integer year,
            Integer month,
            List<String> categories
    ) {
        Metric metric = metricMapper.findById(metricId);

        // Use new method with category filtering
        List<MetricBreakdownData> currentRows = (categories == null || categories.isEmpty())
            ? breakdownDataMapper.findByMetricYearMonthType(metricId, year, month, type)
            : breakdownDataMapper.findByMetricYearMonthTypeAndCategories(metricId, year, month, type, categories);

        int prevYear = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;
        List<MetricBreakdownData> prevRows = (categories == null || categories.isEmpty())
            ? breakdownDataMapper.findByMetricYearMonthType(metricId, prevYear, prevMonth, type)
            : breakdownDataMapper.findByMetricYearMonthTypeAndCategories(metricId, prevYear, prevMonth, type, categories);
        List<MetricBreakdownData> lyRows = (categories == null || categories.isEmpty())
            ? breakdownDataMapper.findByMetricYearMonthType(metricId, year - 1, month, type)
            : breakdownDataMapper.findByMetricYearMonthTypeAndCategories(metricId, year - 1, month, type, categories);
        List<MetricBreakdownData> ytdRows = (categories == null || categories.isEmpty())
            ? breakdownDataMapper.findByMetricYearMonthRangeType(metricId, year, 1, month, type)
            : breakdownDataMapper.findByMetricYearMonthRangeTypeAndCategories(metricId, year, 1, month, type, categories); // Need to add this method

        double currentTotal = sumActual(currentRows);
        double prevTotal = sumActual(prevRows);
        double lyTotal = sumActual(lyRows);
        double ytdTotal = sumActual(ytdRows);
        double ytdGoalTotal = sumGoal(ytdRows);

        Map<String, Double> prevMap = groupSumActual(prevRows);
        Map<String, Double> lyMap = groupSumActual(lyRows);
        Map<String, Double> ytdActualMap = groupSumActual(ytdRows);
        Map<String, Double> ytdGoalMap = groupSumGoal(ytdRows);

        // "All" row
        double currentGoalTotal = sumGoal(currentRows);
        BreakdownDataDto allCurrent = new BreakdownDataDto();
        allCurrent.setActual(currentTotal);
        allCurrent.setJbpGoal(currentGoalTotal);
        if (currentGoalTotal != 0) allCurrent.setVsJbpGoalPct((currentTotal - currentGoalTotal) / currentGoalTotal * 100);

        BreakdownDataDto allYtd = new BreakdownDataDto();
        allYtd.setActual(ytdTotal);
        allYtd.setJbpGoal(ytdGoalTotal);
        if (ytdGoalTotal != 0) allYtd.setVsJbpGoalPct((ytdTotal - ytdGoalTotal) / ytdGoalTotal * 100);

        BreakdownRowDto allRow = new BreakdownRowDto();
        allRow.setDimensionValue("All");
        allRow.setIsTotal(true);
        allRow.setCurrent(allCurrent);
        allRow.setYtd(allYtd);

        // Dimension rows
        List<BreakdownRowDto> dimRows = new ArrayList<>();
        for (MetricBreakdownData row : currentRows) {
            String dv = row.getDimensionValue();
            double rowActual = row.getActual() != null ? row.getActual().doubleValue() : 0;
            double rowGoal = row.getJbpGoal() != null ? row.getJbpGoal().doubleValue() : 0;

            BreakdownDataDto curr = new BreakdownDataDto();
            curr.setActual(rowActual);
            curr.setJbpGoal(rowGoal);
            if (rowGoal != 0) curr.setVsJbpGoalPct((rowActual - rowGoal) / rowGoal * 100);
            if (currentTotal != 0 && prevTotal != 0)
                curr.setMomCtcBps((rowActual / currentTotal - prevMap.getOrDefault(dv, 0.0) / prevTotal) * 10000);
            if (currentTotal != 0 && lyTotal != 0)
                curr.setYoyCtcBps((rowActual / currentTotal - lyMap.getOrDefault(dv, 0.0) / lyTotal) * 10000);

            double dimYtdActual = ytdActualMap.getOrDefault(dv, 0.0);
            double dimYtdGoal = ytdGoalMap.getOrDefault(dv, 0.0);
            BreakdownDataDto ytd = new BreakdownDataDto();
            ytd.setActual(dimYtdActual);
            ytd.setJbpGoal(dimYtdGoal);
            if (dimYtdGoal != 0) ytd.setVsJbpGoalPct((dimYtdActual - dimYtdGoal) / dimYtdGoal * 100);

            BreakdownRowDto dr = new BreakdownRowDto();
            dr.setDimensionValue(dv);
            dr.setProductTitle(row.getProductTitle());
            dr.setIsTotal(false);
            dr.setCurrent(curr);
            dr.setYtd(ytd);
            dimRows.add(dr);
        }

        List<BreakdownRowDto> allRows = new ArrayList<>();
        allRows.add(allRow);
        allRows.addAll(dimRows);

        DimensionBreakdownResponse resp = new DimensionBreakdownResponse();
        resp.setType(type);
        resp.setMetricId(metricId);
        resp.setMetricName(metric != null ? metric.getMetricName() : "");
        resp.setMetricType(metric != null ? metric.getMetricType() : "");
        resp.setPeriod(formatPeriod(year, month));
        resp.setRows(allRows);
        return resp;
    }

    // ---- helpers ----

    private PeriodDataDto buildCurrentPeriodDto(MetricMonthlyData current,
                                                 MetricMonthlyData prev,
                                                 MetricMonthlyData lastYear) {
        PeriodDataDto dto = new PeriodDataDto();
        if (current == null) return dto;

        double actual = current.getActual() != null ? current.getActual().doubleValue() : 0;
        dto.setActual(actual);

        if (current.getJbpGoal() != null) {
            double goal = current.getJbpGoal().doubleValue();
            dto.setJbpGoal(goal);
            if (goal != 0) dto.setVsJbpGoalPct((actual - goal) / goal * 100);
        }

        if (prev != null && prev.getActual() != null) {
            double prevActual = prev.getActual().doubleValue();
            double mom = actual - prevActual;
            dto.setMom(mom);
            if (prevActual != 0) dto.setMomPct(mom / prevActual * 100);
        }

        if (lastYear != null && lastYear.getActual() != null) {
            double lyActual = lastYear.getActual().doubleValue();
            double yoy = actual - lyActual;
            dto.setYoy(yoy);
            if (lyActual != 0) dto.setYoyPct(yoy / lyActual * 100);
        }

        return dto;
    }

    private PeriodDataDto buildYtdPeriodDto(List<MetricMonthlyData> thisYear,
                                             List<MetricMonthlyData> lastYear, int month) {
        PeriodDataDto dto = new PeriodDataDto();
        double ytdActual = sumActualUpTo(thisYear, month);
        double ytdGoal = sumGoalUpTo(thisYear, month);
        double lyYtd = sumActualUpTo(lastYear, month);

        dto.setActual(ytdActual);
        dto.setJbpGoal(ytdGoal);
        if (ytdGoal != 0) dto.setVsJbpGoalPct((ytdActual - ytdGoal) / ytdGoal * 100);
        if (!lastYear.isEmpty()) {
            double yoy = ytdActual - lyYtd;
            dto.setYoy(yoy);
            if (lyYtd != 0) dto.setYoyPct(yoy / lyYtd * 100);
        }
        return dto;
    }

    private TrendBreakdownRow row(String label, double ytd, List<Double> monthValues) {
        List<Double> values = new ArrayList<>();
        values.add(ytd);
        values.addAll(monthValues);
        TrendBreakdownRow r = new TrendBreakdownRow();
        r.setLabel(label);
        r.setValues(values);
        return r;
    }

    private List<Double> buildMonthValues(Map<Integer, MetricMonthlyData> map,
                                          Function<MetricMonthlyData, Double> extractor) {
        List<Double> vals = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            MetricMonthlyData d = map.get(m);
            vals.add(d != null ? extractor.apply(d) : null);
        }
        return vals;
    }

    private List<Double> buildDiffMonthValues(Map<Integer, MetricMonthlyData> map) {
        List<Double> vals = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            MetricMonthlyData d = map.get(m);
            if (d != null && d.getActual() != null && d.getJbpGoal() != null)
                vals.add(d.getActual().doubleValue() - d.getJbpGoal().doubleValue());
            else
                vals.add(null);
        }
        return vals;
    }

    private List<Double> buildYoyMonthValues(Map<Integer, MetricMonthlyData> tyMap,
                                              Map<Integer, MetricMonthlyData> lyMap) {
        List<Double> vals = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            MetricMonthlyData ty = tyMap.get(m);
            MetricMonthlyData ly = lyMap.get(m);
            if (ty != null && ty.getActual() != null && ly != null && ly.getActual() != null)
                vals.add(ty.getActual().doubleValue() - ly.getActual().doubleValue());
            else
                vals.add(null);
        }
        return vals;
    }

    private double sumActualUpTo(List<MetricMonthlyData> data, int month) {
        return data.stream().filter(d -> d.getMonth() <= month)
                .mapToDouble(d -> d.getActual() != null ? d.getActual().doubleValue() : 0).sum();
    }

    private double sumGoalUpTo(List<MetricMonthlyData> data, int month) {
        return data.stream().filter(d -> d.getMonth() <= month)
                .mapToDouble(d -> d.getJbpGoal() != null ? d.getJbpGoal().doubleValue() : 0).sum();
    }

    private double sumActual(List<MetricBreakdownData> rows) {
        return rows.stream().mapToDouble(r -> r.getActual() != null ? r.getActual().doubleValue() : 0).sum();
    }

    private double sumGoal(List<MetricBreakdownData> rows) {
        return rows.stream().mapToDouble(r -> r.getJbpGoal() != null ? r.getJbpGoal().doubleValue() : 0).sum();
    }

    private Map<String, Double> groupSumActual(List<MetricBreakdownData> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                MetricBreakdownData::getDimensionValue,
                Collectors.summingDouble(r -> r.getActual() != null ? r.getActual().doubleValue() : 0)));
    }

    private Map<String, Double> groupSumGoal(List<MetricBreakdownData> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                MetricBreakdownData::getDimensionValue,
                Collectors.summingDouble(r -> r.getJbpGoal() != null ? r.getJbpGoal().doubleValue() : 0)));
    }

    private String formatPeriod(int year, int month) {
        return MONTH_NAMES.get(month - 1) + " " + year;
    }

    private MetricMonthlyData sumCategoryData(List<MetricMonthlyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        // Filter out null values (can happen when wrapping single result)
        List<MetricMonthlyData> filteredList = dataList.stream()
            .filter(Objects::nonNull)
            .toList();

        if (filteredList.isEmpty()) {
            return null;
        }

        MetricMonthlyData result = new MetricMonthlyData();
        BigDecimal sumActual = filteredList.stream()
            .map(d -> d.getActual() != null ? d.getActual() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumGoal = filteredList.stream()
            .map(d -> d.getJbpGoal() != null ? d.getJbpGoal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.setActual(sumActual);
        result.setJbpGoal(sumGoal);
        return result;
    }
}
