package de.zorro909.generator.data;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class StringTestData implements TestData {
    private final String name;
    private final String str;

    public StringTestData(String name, String str) {
        this.name = name;
        this.str = str;
    }

    @Override
    public TypeName getType() {
        return ClassName.get(String.class);
    }

    @Override
    public String createDataCode(CodeBlock.Builder blockBuilder) {
        blockBuilder.addStatement("$T $N = $S", getType(), name, str);
        return name;
    }
}
