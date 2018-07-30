package com.wangn.json2javaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import lombok.Builder;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.function.Function.identity;

/**
 * class functional description
 *
 * @author wang.xiongfei
 * @version 1.0.0
 * @since 2018-07-30
 */
@Builder
public class Generators {

    private Function<String, String> resolveClassName = identity();
    private Function<String, String> resolveFieldName = identity();
    private BiConsumer<TypeSpec.Builder, String> typeSpecVisitor = (t, v) -> {};
    private BiConsumer<FieldSpec.Builder, String> fieldSpecVisitor = (t, v) -> {};

    public Generator build2() {
        return context -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = objectMapper.readTree(context.getJson());
            } catch (IOException e) {
                errLog("解析json出错!");
            }
            if(!node.isObject()) errLog("json顶层类型必须为对象!");
            TypeSpec.Builder builder = TypeSpec.classBuilder(context.getSimpleClassName()).addModifiers(Modifier.PUBLIC);
            typeSpecVisitor.accept(builder, context.getSimpleClassName());
            node.fields().forEachRemaining(entry -> visitorNode(context, builder, entry));
            context.getTypeSpecs().add(builder.build());
            final Path path = new File(context.getTargetPath()).toPath();
            context.getTypeSpecs().forEach(typeSpec -> {
                try {
                    JavaFile.builder(context.getPackageName(), typeSpec)
                            .build().writeTo(path);
                } catch (IOException e) {
                    errLog(e.getMessage());
                }
            });
        };
    }

    private void visitorNode(Context context, TypeSpec.Builder parentBuilder, Map.Entry<String, JsonNode> entry){
        String fieldName = entry.getKey();
        JsonNode node = entry.getValue();
        TypeName fieldClassName;
        switch (node.getNodeType()) {
            case OBJECT:
                TypeSpec.Builder builder = TypeSpec.classBuilder(resolveClassName.apply(fieldName)).addModifiers(Modifier.PUBLIC);
                node.fields().forEachRemaining(entry1 -> visitorNode(context, builder, entry1));
                typeSpecVisitor.accept(builder, fieldName);
                TypeSpec typeSpec = builder.build();
                context.getTypeSpecs().add(typeSpec);
                fieldClassName = ClassName.get(context.getPackageName(), typeSpec.name);
                break;
            case ARRAY:
                JsonNode jsonNode = node.get(0);
                Function<TypeName, TypeName> wrap = type -> ParameterizedTypeName.get(ClassName.get(List.class), type);
                TypeVariableName.get(List.class);
                Function<TypeName, TypeName> covert = wrap;
                while(jsonNode.isArray()) {
                    covert = covert.andThen(wrap);
                }
                if(jsonNode.isObject()) {
                    TypeSpec.Builder builder2 = TypeSpec.classBuilder(resolveClassName.apply(fieldName)).addModifiers(Modifier.PUBLIC);
                    node.fields().forEachRemaining(entry1 -> visitorNode(context, builder2, entry1));
                    typeSpecVisitor.accept(builder2, fieldName);
                    TypeSpec typeSpec2 = builder2.build();
                    context.getTypeSpecs().add(typeSpec2);
                    fieldClassName = covert.apply(ClassName.get(context.getPackageName(), typeSpec2.name));
                } else {
                    fieldClassName = covert.apply(ClassName.get(resolveType(jsonNode)));
                }
                break;
            default:
                fieldClassName = ClassName.get(resolveType(node));
        }

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldClassName, resolveFieldName.apply(fieldName), Modifier.PRIVATE);
        fieldSpecVisitor.accept(fieldBuilder, fieldName);
        parentBuilder.addField(fieldBuilder.build());
    }

    public static void errLog(String message) {
        throw new GenException(message);
    }

    public static class GenException extends RuntimeException {
        public GenException(String msg) {
            super(msg);
        }
    }

    private static Class<?> resolveType(JsonNode jsonNode) {
        JsonNodeType nodeType = jsonNode.getNodeType();
        switch (nodeType) {
            case BOOLEAN:
                return Boolean.TYPE;
            case STRING:
                return String.class;
            case NUMBER:
                if(jsonNode.isFloatingPointNumber()) {
                    return BigDecimal.class;
                }
                if(jsonNode.isInt()) {
                    return Integer.class;
                }
                return Long.class;
            default:return Object.class;
        }
    }
}
