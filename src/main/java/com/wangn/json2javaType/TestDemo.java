package com.wangn.json2javaType;


import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.function.Function;

/**
 * class functional description
 *
 * @author wang.xiongfei
 * @version 1.0.0
 * @since 2018-07-30
 */
public class TestDemo {

    @Test
    public void test1() {
        Context context = Context.builder()
                .json(DEMO)
                .packageName("com.wangn.gendir")
                .simpleClassName("Demo")
                .targetPath("/Users/wangn/Documents/ideawork2/jsonToJavaType/src/main/java")
                .build();
        Generator generator = buildSimpleGenerator();
        generator.gen(context);
    }

    public static final String DEMO = "{\n" +
            "    \"errcode\":0 ,\n" +
            "    \"errmsg\":\"ok\" ,\n" +
            "    \"access_token\": \"xxxxxx\", \n" +
            "    \"expires_in\": 7200, \n" +
            "    \"permanent_code\": \"xxxx\", \n" +
            "    \"listTest\": [1,2,3,4], \n" +
            "    \"auth_corp_info\": \n" +
            "    [{\n" +
            "        \"corpid\": \"xxxx\",\n" +
            "        \"corp_name\": \"name\",\n" +
            "        \"corp_type\": \"verified\",\n" +
            "        \"corp_square_logo_url\": \"yyyyy\",\n" +
            "        \"corp_user_max\": 50,\n" +
            "        \"corp_agent_max\": 30,\n" +
            "        \"corp_full_name\":\"full_name\",\n" +
            "        \"verified_end_time\":1431775834,\n" +
            "        \"subject_type\": 1,\n" +
            "        \"corp_wxqrcode\": \"zzzzz\",\n" +
            "        \"corp_scale\": \"1-50人\",\n" +
            "        \"corp_industry\": \"IT服务\",\n" +
            "        \"corp_sub_industry\": \"计算机软件/硬件/信息服务\"\n" +
            "    }]\n" +
            "}";

    private Generator buildSimpleGenerator() {
        return Generators.builder()
                .resolveClassName(underlineToHump.andThen(upperFirstChar))
                .resolveFieldName(underlineToHump)
                .fieldSpecVisitor((builder, fieldName) -> {
                    builder.addAnnotation(AnnotationSpec
                            .builder(ClassName.get(Column.class)).addMember("name", "$S", fieldName).build());
                })
                .typeSpecVisitor((builder, fieldName) -> {
                    builder.addAnnotation(AnnotationSpec.builder(Table.class)
                            .addMember("name", "$S", fieldName).build())
                            .addAnnotation(AnnotationSpec.builder(Entity.class).build());
                }).build().build2();
    }

    public static Function<String, String> underlineToHump = para -> {
        StringBuilder result=new StringBuilder();
        String a[]=para.split("_");
        for(String s:a){
            if(result.length()==0){
                result.append(s.toLowerCase());
            }else{
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    };

    public static Function<String, String> upperFirstChar = line -> {
        if (line.length() == 1) {
            return line.substring(0, 1).toUpperCase();
        } else {
            return line.substring(0, 1).toUpperCase() + line.substring(1, line.length());
        }
    };
}
