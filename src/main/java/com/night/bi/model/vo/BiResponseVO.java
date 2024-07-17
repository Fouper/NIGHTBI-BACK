package com.night.bi.model.vo;

import lombok.Data;

/**
 * BI 返回结果
 */
@Data
public class BiResponseVO {

    private Long chartId;

    private String genChart;

    private String genResult;
}
