package com.demo.crud.mapper;

import com.demo.crud.model.Metric;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MetricMapper {
    List<Metric> findAll();
    Metric findById(@Param("id") int id);
}
