package de.zorro909.generator.excel;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.JavaVersion;

import java.util.List;

public class ImportUtils {

    public static ClassName fromString(List<String> imports, String className) {
        String resolved = imports.stream().filter(importDeclaration -> importDeclaration.endsWith("." + className)).findAny().orElse(className);

        String packageName = switch (resolved.lastIndexOf(".")) {
            case -1 -> "";
            default -> resolved.substring(0, resolved.lastIndexOf("."));
        };
        String simpleClassName = switch (resolved.lastIndexOf(".")) {
            case -1 -> resolved;
            default -> resolved.substring(resolved.lastIndexOf(".") + 1);
        };
        return ClassName.get(packageName, simpleClassName);
    }

}
