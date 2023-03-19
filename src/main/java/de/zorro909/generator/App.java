package de.zorro909.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import de.zorro909.generator.excel.DataReader;
import org.apache.commons.io.output.AppendableOutputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        File target = new File("src/test/java/");

        new DataReader().readTestCases(new File("TestDefinitions.xlsx")).forEach((className, testCases) -> {
            TestGenerator generator = new TestGenerator(className);
            testCases.forEach(generator::addTestCase);
            try {
                JavaFile.builder(className.packageName(), generator.generateTestFile()).indent("    ").build().writeTo(target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
