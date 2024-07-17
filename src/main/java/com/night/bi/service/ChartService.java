package com.night.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.night.bi.model.dto.chart.ChartQueryRequest;
import com.night.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 图表服务
 *
 * @author WL丶Night
 * @from WL丶Night
 */
public interface ChartService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 查询图表分页是否相同
     * @param page1
     * @param page2
     * @return
     */
    boolean isSamePage(Page<Chart> page1, Page<Chart> page2);

    /**
     * 查询图表是否相同
     * @param chart1
     * @param chart2
     * @return
     */
    boolean isSameChart(Chart chart1, Chart chart2);

}
