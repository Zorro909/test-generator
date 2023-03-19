package de.zorro909.generator;

import com.squareup.javapoet.ClassName;
import de.zorro909.generator.data.TestData;

import java.util.LinkedList;
import java.util.Optional;

public record TestCase(String testIdentifier, String targetMethod, String description, LinkedList<TestData> parameters, Optional<TestData> expectedOutput, Optional<ClassName> expectedException){
}