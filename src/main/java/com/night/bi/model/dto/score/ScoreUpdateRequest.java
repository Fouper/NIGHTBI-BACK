package com.night.bi.model.dto.score;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Data
public class ScoreUpdateRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户积分
     */
    private Long scoreTotal;

    private static final long serialVersionUID = 1L;
}