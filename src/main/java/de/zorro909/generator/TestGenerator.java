package de.zorro909.generator;

import com.squareup.javapoet.*;
import de.zorro909.generator.data.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestGenerator {

    private final ClassName targetClassName;

    private final List<TestCase> testCases = new ArrayList<>();

    public TestGenerator(ClassName targetClassName) {
        this.targetClassName = targetClassName;
    }

    public TestGenerator addTestCase(TestCase testCase) {
        this.testCases.add(testCase);
        return this;
    }

    public TypeSpec generateTestFile() {
        AnnotationSpec extendWithAnnotation = AnnotationSpec.builder(ExtendWith.class)
                .addMember("value", "$T.class", ClassName.get(MockitoExtension.class))
                .build();

        FieldSpec unitUnderTest = FieldSpec
                .builder(getTargetClassName(), "uut")
                .addAnnotation(InjectMocks.class)
                .build();

        TypeSpec.Builder testBuilder = TypeSpec.classBuilder(getTestClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(extendWithAnnotation)
                .addField(unitUnderTest);

        testCases.stream().map(this::generateTestMethod).forEach(testBuilder::addMethod);

        return testBuilder.build();
    }

    private ClassName getTargetClassName() {
        return targetClassName;
    }

    private String getTestClassName() {
        return getSimpleTargetClassName() + "Test";
    }

    private String getTargetClassPackage() {
        return targetClassName.packageName();
    }

    private String getSimpleTargetClassName() {
        return targetClassName.simpleName();
    }

    private MethodSpec generateTestMethod(TestCase testCase) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(testCase.testIdentifier() + "Test");

        methodSpecBuilder.addAnnotation(Test.class);
        methodSpecBuilder.addJavadoc("$L", testCase.description());

        CodeBlock.Builder blockBuilder = CodeBlock.builder();
        blockBuilder.add("// Input Parameters\n");
        String inputArguments = testCase.parameters().stream().map(parameter -> parameter.createDataCode(blockBuilder)).collect(Collectors.joining(","));
        blockBuilder.add("\n");

        if (testCase.expectedOutput().isPresent()) {
            TestData expectedOutput = testCase.expectedOutput().get();

            blockBuilder.add("// Expected Output\n");
            String expectedOutputVariableName = expectedOutput.createDataCode(blockBuilder);

            blockBuilder.add("// Test\n");
            blockBuilder
                    .addStatement("$T returnObject = this.$N.$N($L)", expectedOutput.getType(), "uut", testCase.targetMethod(), inputArguments)
                    .addStatement("$T.$N($N).isEqualTo($N)", Assertions.class, "assertThat", "returnObject", expectedOutputVariableName);
        } else if (testCase.expectedException().isPresent()) {
            ClassName expectedException = testCase.expectedException().get();

            blockBuilder.addStatement("$T.$N(() -> $N.$N($L)).$N($T.class)", Assertions.class, "assertThatCode", "uut", testCase.targetMethod(), inputArguments, "isInstanceOf", expectedException);
        } else {
            throw new RuntimeException("Neither expectedOutput or expectedException is set for " + targetClassName + "#" + testCase.targetMethod());
        }

        methodSpecBuilder.addCode(blockBuilder.build());
        return methodSpecBuilder.build();
    }


}
