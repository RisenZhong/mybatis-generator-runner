package com.risen.runner;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * generate @ApiModelProperty annotation for model field using column comment
 *
 * @author sen
 * @email zhongrisen@gmail.com
 * @date 2018/4/9
 */
public class SwaggerAnnotationPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                IntrospectedColumn introspectedColumn,
                                IntrospectedTable introspectedTable, ModelClassType modelClassType) {

        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)){
            field.addAnnotation("@ApiModelProperty(\"" + remarks + "\")");
        }
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                          IntrospectedTable introspectedTable) {

        topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
        return true;
    }
}
