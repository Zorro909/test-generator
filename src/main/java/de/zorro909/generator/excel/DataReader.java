package de.zorro909.generator.excel;

import com.squareup.javapoet.ClassName;
import de.zorro909.generator.data.LiteralTestData;
import de.zorro909.generator.data.StringTestData;
import de.zorro909.generator.TestCase;
import de.zorro909.generator.data.TestData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DataReader {

    public Map<ClassName, List<TestCase>> readTestCases(File excelFile) throws IOException, InvalidFormatException {
        String fileEnding = excelFile.getName().substring(excelFile.getName().lastIndexOf(".") + 1);
        InputStream inputStream = new FileInputStream(excelFile);
        Workbook workbook = switch (fileEnding.toLowerCase()) {
            case "xlsx" -> new XSSFWorkbook(inputStream);
            case "xls" -> new HSSFWorkbook(inputStream);
            default -> throw new IllegalArgumentException("Excel File needs to be of the format xls or xlsx!");
        };

        return StreamSupport.stream(workbook.spliterator(), false)
                .flatMap(this::readTestClassGroups)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, ListUtils::union));
    }

    private Stream<Pair<ClassName, List<TestCase>>> readTestClassGroups(Sheet sheet) {
        String importsDefinition = getCell(sheet, 0, 0);
        if (!importsDefinition.equalsIgnoreCase("Imports")) {
            return Stream.empty();
        }

        String importString = getCell(sheet, 1, 0);
        List<String> imports = Arrays.stream(importString.split("\n")).map(String::trim).toList();

        List<Pair<ClassName, List<TestCase>>> testCases = new ArrayList<>();
        int lastRow = sheet.getLastRowNum();
        for (int row = 3; row <= lastRow; row++) {
            String cellContent = getCell(sheet, row, 0);
            if (cellContent.equalsIgnoreCase("TargetClass")) {
                String targetClass = getCell(sheet, row, 1);
                testCases.add(readTestClassGroup(sheet, row + 1, imports, targetClass));
            }
        }
        return testCases.stream();
    }

    private Pair<ClassName, List<TestCase>> readTestClassGroup(Sheet sheet, int row, List<String> imports, String targetClass) {
        ClassName className = ImportUtils.fromString(imports, targetClass);

        int possibleParameters = 0;
        while (getCell(sheet, row, 3 + (possibleParameters * 3)).startsWith("Parameter")) {
            possibleParameters++;
        }
        row++;

        List<TestCase> cases = new ArrayList<>();
        while (!isBlank(sheet, ++row, 0)) {
            String identifier = getCell(sheet, row, 0);
            String targetMethod = getCell(sheet, row, 1);
            String description = getCell(sheet, row, 2);

            LinkedList<TestData> inputParameters = new LinkedList<>();
            for (int parameter = 0; parameter < possibleParameters; parameter++) {
                if (isBlank(sheet, row, 3 + parameter * 3)) {
                    break;
                }

                inputParameters.add(readTestData(sheet, imports, row, 3 + parameter * 3));
            }

            Optional<TestData> expectedOutput = Optional.empty();

            if (!isBlank(sheet, row, 3 + possibleParameters * 3)) {
                String type = getCell(sheet, row, 3 + possibleParameters * 3);
                String value = getCell(sheet, row, 4 + possibleParameters * 3);
                expectedOutput = Optional.of(parseTestData(imports, type, "expectedOutput", value));
            }

            Optional<ClassName> expectedException = Optional.empty();
            if (!isBlank(sheet, row, 5 + possibleParameters * 3)) {
                String exception = getCell(sheet, row, 5 + possibleParameters * 3);
                expectedException = Optional.of(ImportUtils.fromString(imports, exception));
            }

            TestCase testCase = new TestCase(identifier, targetMethod, description, inputParameters, expectedOutput, expectedException);
            cases.add(testCase);
        }
        return Pair.create(className, cases);
    }

    private TestData readTestData(Sheet sheet, List<String> imports, int row, int column) {
        String type = getCell(sheet, row, column);
        String name = getCell(sheet, row, column + 1);
        String value = getCell(sheet, row, column + 2);
        return parseTestData(imports, type, name, value);
    }

    private TestData parseTestData(List<String> imports, String typeClass, String name, String value) {
        ClassName className = ClassName.OBJECT;
        String testDataType = "literal";
        if (typeClass.contains(":")) {
            String[] split = typeClass.split(":", 2);
            testDataType = split[0].toLowerCase();
            typeClass = split[1];
            className = ImportUtils.fromString(imports, typeClass);
        } else {
            className = ImportUtils.fromString(imports, typeClass);
            if (className.equals(ClassName.get(String.class))) {
                testDataType = "string";
            }
        }

        return switch (testDataType) {
            case "literal" -> new LiteralTestData(className, name, value);
            case "string" -> new StringTestData(name, value);
            default -> throw new IllegalArgumentException("Only literal and string Data Types are supported for now!");
        };
    }

    private static boolean isBlank(Sheet sheet, int row, int column) {


        return getCell(sheet, row, column).isBlank();
    }

    private static String getCell(Sheet sheet, int row, int column) {
        return Optional.ofNullable(sheet.getRow(row)).map(actualRow -> actualRow.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).map(Cell::getStringCellValue).orElse("");
    }

}
