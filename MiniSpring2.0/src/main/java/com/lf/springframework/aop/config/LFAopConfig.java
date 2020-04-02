package com.lf.springframework.aop.config;

import lombok.Data;

/**
 */
@Data
public class LFAopConfig {

    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}
