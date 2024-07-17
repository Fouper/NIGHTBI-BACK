package com.night.bi.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userName;

    private String userPassword;

    private String checkPassword;
}
