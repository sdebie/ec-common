package org.ecommerce.common.dto;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Permanent regression guard (dto-encapsulation spec, Requirement 6): every
 * non-record class in this package must declare all instance fields private —
 * access goes through Lombok accessors only. Records are exempt by construction.
 */
class DtoEncapsulationTest
{
    private static final String DTO_PACKAGE = "org.ecommerce.common.dto";

    @Test
    void allDtoClassInstanceFieldsArePrivate() throws Exception
    {
        List<Class<?>> classes = loadPackageClasses();
        assertFalse(classes.isEmpty(), "DTO package scan found no classes — the scan itself is broken");

        List<String> violations = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isRecord() || clazz.isEnum() || clazz.isInterface() || clazz.isAnnotation()) {
                continue;
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                if (!Modifier.isPrivate(field.getModifiers())) {
                    violations.add(clazz.getName() + "." + field.getName());
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Non-private instance fields in DTO classes (fields must be private with Lombok @Getter/@Setter): " + violations);
    }

    private static List<Class<?>> loadPackageClasses() throws Exception
    {
        String path = DTO_PACKAGE.replace('.', '/');
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        List<Class<?>> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            File dir = new File(resources.nextElement().toURI());
            collectClasses(dir, DTO_PACKAGE, classes);
        }
        return classes;
    }

    private static void collectClasses(File dir, String packageName, List<Class<?>> classes) throws Exception
    {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                collectClasses(entry, packageName + "." + entry.getName(), classes);
            } else if (entry.getName().endsWith(".class")) {
                String simpleName = entry.getName().substring(0, entry.getName().length() - ".class".length());
                classes.add(Class.forName(packageName + "." + simpleName));
            }
        }
    }
}
