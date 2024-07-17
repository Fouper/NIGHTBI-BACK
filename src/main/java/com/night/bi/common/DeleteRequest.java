package com.night.bi.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}