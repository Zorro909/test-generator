package de.zorro909.generator.data;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public interface TestData {
    TypeName getType();

    String createDataCode(CodeBlock.Builder blockBuilder);
}
