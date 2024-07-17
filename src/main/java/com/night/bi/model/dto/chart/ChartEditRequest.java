package com.night.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Data
public class ChartEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图标数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}