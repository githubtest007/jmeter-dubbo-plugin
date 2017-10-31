package com.test.dubbo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author phwang
 * @date 2016/10/25
 */

@ToString
@Getter
@Setter
public class ApiResponse implements Serializable {

    private Integer  id;
    private String   name;
    private Long     startTime;
    private Long     endTime;
    private String   respText;             // 接口的返回文本       
    private Class<?> returnType;           // 接口的返回类型
    private Object   responseBody;         // 接口的返回内容
    private boolean  checkResult   = true;
    private Boolean  invokeSuccess = false;
    private Object   originalResponse;
}
