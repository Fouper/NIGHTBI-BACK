package com.night.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.night.bi.constant.CommonConstant;
import com.night.bi.model.dto.chart.ChartQueryRequest;
import com.night.bi.model.entity.Chart;
import com.night.bi.service.ChartService;
import com.night.bi.mapper.ChartMapper;
import com.night.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图表服务实现
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    public boolean isSamePage(Page<Chart> page1, Page<Chart> page2) {
        // 检查两个分页对象是否为null
        if (page1 == null || page2 == null) {
            return false;
        }
        // 检查总页数是否相同
        if (page1.getPages() != page2.getPages()) {
            return false;
        }
        // 检查当前页数是否相同
        if (page1.getCurrent() != page2.getCurrent()) {
            return false;
        }
        // 检查每页大小是否相同
        if (page1.getSize() != page2.getSize()) {
            return false;
        }
        // 检查数据项是否相同
        List<Chart> list1 = page1.getRecords();
        List<Chart> list2 = page2.getRecords();
        // 如果数据项个数不同，则两个分页对象不同
        if (list1.size() != list2.size()) {
            return false;
        }
        // 检查每个数据项是否相同
        for (int i = 0; i < list1.size(); i++) {
            Chart chart1 = list1.get(i);
            Chart chart2 = list2.get(i);
            // 检查数据项是否相同，可以根据具体业务需求来定义
            if (!isSameChart(chart1, chart2)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSameChart(Chart chart1, Chart chart2) {
        // 比较图表对象的各个属性是否相同，例如ID、名称、类型等等
        // 如果所有属性都相同，则返回true；否则返回false
        return chart1.getId().equals(chart2.getId())
                && chart1.getStatus().equals(chart2.getStatus());
    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String name = chartQueryRequest.getName();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 当我进行图表查询时
        if (StringUtils.isNotBlank(goal) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(chartType))
            queryWrapper.and(wrapper -> wrapper
                            .like(StringUtils.isNotBlank(goal), "goal", "%" + goal + "%")
                            .or()
                            .like(StringUtils.isNotBlank(name), "name", "%" + name + "%")
                            .or()
                            .like(StringUtils.isNotBlank(chartType), "chartType", "%" + chartType + "%"))
                    .eq(ObjectUtils.isNotEmpty(userId), "userId", userId)
                    .eq("isDelete", false)
                    .orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                            sortField);
        else {
            queryWrapper.eq(id != null && id > 0, "id", id);
            queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
            queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
            queryWrapper.like(StringUtils.isNotBlank(chartType), "chartType", chartType);
            queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
            queryWrapper.eq("isDelete", false);
            queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    sortField);
        }
        return queryWrapper;
    }

}




