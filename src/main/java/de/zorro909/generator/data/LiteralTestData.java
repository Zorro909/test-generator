package de.zorro909.generator.data;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class LiteralTestData implements TestData {
    private final TypeName type;
    private final String name;
    private final String literal;

    public LiteralTestData(TypeName type, String name, String literal) {
        this.type = type;
        this.name = name;
        this.literal = literal;
    }

    @Override
    public TypeName getType() {
        return type;
    }

    @Override
    public String createDataCode(CodeBlock.Builder blockBuilder) {
        blockBuilder.addStatement("$T $N = $L", getType(), name, literal);
        return name;
    }
}
