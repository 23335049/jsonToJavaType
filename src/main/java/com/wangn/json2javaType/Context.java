package com.wangn.json2javaType;

import com.squareup.javapoet.TypeSpec;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * class functional description
 *
 * @author wang.xiongfei
 * @version 1.0.0
 * @since 2018-07-30
 */
@Data
@Builder
public class Context {

    private String json;

    private String simpleClassName;

    private String packageName;

    private String targetPath;

    @Builder.Default
    private List<TypeSpec> typeSpecs = new ArrayList<>();
}
