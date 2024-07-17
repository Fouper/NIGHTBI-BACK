package com.night.bi.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户重置请求
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Data
public class UserResetRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}