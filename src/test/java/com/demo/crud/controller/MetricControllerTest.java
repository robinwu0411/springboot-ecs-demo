package com.demo.crud.controller;

import com.demo.crud.service.MetricService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricController.class)
@ActiveProfiles("test")
public class MetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricService metricService;

    @Test
    public void testOverviewWithCategories() throws Exception {
        mockMvc.perform(get("/api/metric/overview")
                    .param("year", "2025")
                    .param("month", "4")
                    .param("categories", "Cat1,Cat2"))
                    .andExpect(status().isOk());
    }

    @Test
    public void testBreakdownWithCategories() throws Exception {
        mockMvc.perform(get("/api/metric/breakdown")
                    .param("metricId", "1")
                    .param("type", "category")
                    .param("year", "2025")
                    .param("month", "4")
                    .param("categories", "Cat1,Cat2"))
                    .andExpect(status().isOk());
    }
}