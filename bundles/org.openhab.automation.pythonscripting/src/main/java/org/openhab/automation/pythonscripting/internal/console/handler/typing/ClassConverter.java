/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.pythonscripting.internal.console.handler.typing;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector.ClassContainer;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector.MethodContainer;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector.ParameterContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a Java class to a Python class stub
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class ClassConverter {
    private final Logger logger = LoggerFactory.getLogger(ClassConverter.class);

    private static Pattern classMatcher = Pattern
            .compile("(?:(?:super|extends) )?([a-z0-9\\.\\$_]+|\\?)(?:<.*?>|\\[\\])?$", Pattern.CASE_INSENSITIVE);

    private static Pattern instanceMatcher = Pattern.compile("([a-z0-9\\.\\$_]+)[;]{0,1}@[a-z0-9]+",
            Pattern.CASE_INSENSITIVE);

    private static String baseUrl;
    static {
        // Version version = FrameworkUtil.getBundle(OpenHAB.class).getVersion();
        String v = "latest"; // version.getQualifier() == null || version.getQualifier().isEmpty() ? version.toString()
                             // : "latest";
        baseUrl = "https://www.openhab.org/javadoc/" + v + "/";
    }

    private final Map<String, String> classGenerics;
    private final Map<String, String> imports;
    private final ClassContainer container;

    public ClassConverter(ClassContainer container) {
        this.container = container;
        this.classGenerics = new HashMap<String, String>();
        this.imports = new HashMap<String, String>();
    }

    public List<String> getImports() {
        return new ArrayList<String>(this.imports.keySet());
    }

    public String build() throws IOException, ClassNotFoundException {
        // Class head
        StringBuilder classBody = new StringBuilder();
        classBody.append(buildClassHead());

        // Class documentation
        String doc = buildClassDocumentationBlock();
        if (doc != null) {
            classBody.append(doc);
            classBody.append("\n");
        }

        // Class fields
        classBody.append(buildClassFields());

        // Class methods
        List<MethodContainer> methods = container.getMethods();
        Collections.sort(methods, new Comparator<MethodContainer>() {
            @Override
            public int compare(MethodContainer o1, MethodContainer o2) {
                return o1.getPythonMethodName().compareTo(o2.getPythonMethodName());
            }
        });
        for (MethodContainer method : methods) {

            classBody.append(buildClassMethod(method));
        }

        // Class imports
        if (!imports.isEmpty()) {
            classBody.insert(0, "\n\n");
            classBody.insert(0, buildClassImports());
        }

        return classBody.toString();
    }

    private String buildClassHead() {
        Type type = container.getRelatedClass().getGenericSuperclass();
        if (type != null) {
            classGenerics.putAll(collectGenerics(type));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("class " + container.getPythonClassName());
        List<String> parentTypes = new ArrayList<String>();
        Class<?> parentClass = container.getRelatedClass().getSuperclass();
        if (parentClass != null) {
            String pythonType = convertBaseJavaToPythonType(new JavaType(parentClass.getName()), classGenerics);
            if (!"object".equals(pythonType)) {
                parentTypes.add(pythonType);
            }
        }
        Class<?>[] parentInterfaces = container.getRelatedClass().getInterfaces();
        for (Class<?> parentInterface : parentInterfaces) {
            String pythonType = convertBaseJavaToPythonType(new JavaType(parentInterface.getName()), classGenerics);
            parentTypes.add(pythonType);
        }
        if (parentTypes.isEmpty()) {
            if (container.getRelatedClass().isInterface()) {
                parentTypes.add("Protocol");
                imports.put("__typing.Protocol", "from typing import Protocol");
            }
        }
        if (!parentTypes.isEmpty()) {
            builder.append("(" + String.join(", ", parentTypes) + ")");
        }
        builder.append(":\n");
        return builder.toString();
    }

    private String buildClassFields() {
        StringBuilder builder = new StringBuilder();
        for (Field field : container.getFields()) {
            try {
                JavaType javaType = collectJavaTypes(field.getGenericType(), classGenerics);
                String type = convertJavatToPythonType(javaType, classGenerics);
                String value = "";
                Class<?> t = field.getType();

                if (Collection.class.isAssignableFrom(field.getType())) {
                    List<String> values = new ArrayList<String>();
                    if (field.get(null) instanceof Collection col) {
                        for (Object val : col) {
                            values.add(convertFieldValue(val));
                        }
                    }
                    value = "[" + String.join(",", values) + "]";
                } else if (t == short.class) {
                    value = convertFieldValue(field.getShort(null));
                } else if (t == int.class) {
                    value = convertFieldValue(field.getInt(null));
                } else if (t == long.class) {
                    value = convertFieldValue(field.getLong(null));
                } else if (t == float.class) {
                    value = convertFieldValue(field.getFloat(null));
                } else if (t == double.class) {
                    value = convertFieldValue(field.getDouble(null));
                } else if (t == boolean.class) {
                    value = convertFieldValue(field.getBoolean(null));
                } else if (t == char.class) {
                    value = convertFieldValue(field.getChar(null));
                } else {
                    value = convertFieldValue(field.get(null));
                }

                builder.append("    " + field.getName() + ": " + type + " = " + value + "\n");
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.warn("Cant convert static field {} of class {}", container.getRelatedClass().getName(),
                        field.getName(), e);
            }
        }
        if (!builder.isEmpty()) {
            builder.append("\n");
        }

        return builder.toString();
    }

    private String buildClassMethod(MethodContainer method) {
        HashMap<String, String> localGenerics = new HashMap<String, String>(this.classGenerics);
        // Collect generics
        for (int i = 0; i < method.getReturnTypeCount(); i++) {
            localGenerics.putAll(collectGenerics(method.getGenericReturnType(i)));
        }
        for (ParameterContainer p : method.getParameters()) {
            for (int i = 0; i < p.getTypeCount(); i++) {
                localGenerics.putAll(collectGenerics(p.getGenericType(i)));
            }
        }

        // Collect arguments
        List<String> arguments = new ArrayList<String>();
        if (!Modifier.isStatic(method.getModifiers())) {
            arguments.add("self");
        }
        for (ParameterContainer p : method.getParameters()) {
            Set<String> parameterTypes = new HashSet<String>();
            for (int i = 0; i < p.getTypeCount(); i++) {
                JavaType javaType = collectJavaTypes(p.getGenericType(i), localGenerics);
                String t = convertJavatToPythonType(javaType, localGenerics);
                parameterTypes.add(t);
            }
            List<String> sorted = parameterTypes.stream().sorted().collect(Collectors.toList());
            arguments.add(p.getName() + ": " + String.join(" | ", sorted) + (p.isOptional ? " = None" : ""));
        }

        // Build method
        StringBuilder builder = new StringBuilder();
        if (Modifier.isStatic(method.getModifiers())) {
            builder.append("    @staticmethod\n");
        }
        String methodName = method.isConstructor() ? "__init__" : method.getPythonMethodName();
        builder.append("    def " + methodName + "(" + String.join(", ", arguments) + ")");

        // Build return value
        if (method.getReturnTypeCount() > 0) {
            // Collect Return types
            Set<String> returnTypes = new HashSet<String>();
            for (int i = 0; i < method.getReturnTypeCount(); i++) {
                JavaType javaType = collectJavaTypes(method.getGenericReturnType(i), localGenerics);
                String t = convertJavatToPythonType(javaType, localGenerics);
                returnTypes.add(t);
            }
            List<String> sortedReturnTypes = returnTypes.stream().sorted().collect(Collectors.toList());

            builder.append(" -> " + String.join(" | ", sortedReturnTypes));
        }

        // Finalize method
        builder.append(":\n");
        String doc = buildMethodDocumentationBlock(method);
        if (doc != null) {
            builder.append(doc);
        } else {
            builder.append("        pass\n");
        }
        builder.append("\n");

        return builder.toString();
    }

    private Object buildClassImports() {
        StringBuilder builder = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>(imports.values());
        ArrayList<String> sortedImports = new ArrayList<>(hashSet);
        Collections.sort(sortedImports, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length() > o2.length()) {
                    return 1;
                }
                if (o1.length() < o2.length()) {
                    return -1;
                }
                return o1.compareTo(o2);
            }
        });
        for (String importLine : sortedImports) {
            builder.append(importLine + "\n");
        }
        return builder.toString();
    }

    private JavaType collectJavaTypes(Type genericType, Map<String, String> generics) {
        JavaType javaType = null;
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>) genericType;
            String type = generics.get(typeVar.getTypeName());
            if (type != null) {
                javaType = new JavaType(type);
            } else {
                javaType = new JavaType("?");
            }
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            // System.out.println("ParameterizedType | " + _type + " | " + _type.getRawType().getTypeName() + " | "
            // + _type.getActualTypeArguments()[0]);
            javaType = collectJavaTypes(paramType.getRawType(), generics);
            for (Type type : paramType.getActualTypeArguments()) {
                javaType.addSubType(collectJavaTypes(type, generics));
            }
        } else if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) genericType;
            // System.out.println("WildcardType | " + _type);
            if (wildcardType.getUpperBounds().length > 0) {
                javaType = collectJavaTypes(wildcardType.getUpperBounds()[0], generics);
            } else if (wildcardType.getLowerBounds().length > 0) {
                javaType = collectJavaTypes(wildcardType.getLowerBounds()[0], generics);
            } else {
                javaType = new JavaType("java.lang.Object");
            }
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            // System.out.println("GenericArrayType | " + _type);
            javaType = new JavaType("java.util.List");
            javaType.addSubType(collectJavaTypes(genericArrayType.getGenericComponentType(), generics));
        } else {
            // System.out.println("OtherType | " + genericType.getTypeName());
            String type = genericType.getTypeName();
            JavaType activeJavaType = null;
            while (type.endsWith("[]")) {
                JavaType currentJavaType = new JavaType("java.util.List");
                if (activeJavaType == null) {
                    javaType = activeJavaType = currentJavaType;
                } else {
                    activeJavaType.addSubType(currentJavaType);
                }
                activeJavaType = currentJavaType;
                type = type.substring(0, type.length() - 2);
            }
            JavaType currentJavaType = new JavaType(type);
            if (javaType == null) {
                javaType = currentJavaType;
            } else {
                activeJavaType.addSubType(currentJavaType);
            }
        }

        return javaType;
    }

    private String convertJavatToPythonType(JavaType javaType, Map<String, String> generics) {
        switch (javaType.getType()) {
            case "char":
                return "str";
            case "long":
            case "int":
            case "short":
                return "int";
            case "double":
            case "float":
                return "float";
            case "byte":
                return "bytes";
            case "boolean":
                return "bool";
            case "void":
                return "None";
            case "?":
                if (javaType.hasSubTypes(1)) {
                    return convertJavatToPythonType(javaType.getSubType(0), generics);
                }
                return "object";
        }

        try {
            Class<?> cls = Class.forName(javaType.getType());
            if (!cls.equals(java.lang.Object.class)) {
                if (Byte.class.equals(cls)) {
                    return "bytes";
                } else if (Double.class.equals(cls) || Float.class.equals(cls)) {
                    return "float";
                } else if (BigDecimal.class.equals(cls) || BigInteger.class.equals(cls) || Long.class.equals(cls)
                        || Integer.class.equals(cls) || Short.class.equals(cls)) {
                    return "int";
                } else if (Number.class.equals(cls)) {
                    return "float | int";
                } else if (Dictionary.class.equals(cls) || Hashtable.class.equals(cls) || Map.class.equals(cls)
                        || HashMap.class.equals(cls)) {
                    if (javaType.hasSubTypes(2)) {
                        return "dict[" + convertJavatToPythonType(javaType.getSubType(0), generics) + ","
                                + convertJavatToPythonType(javaType.getSubType(1), generics) + "]";
                    }
                    return "dict";
                } else if (Collection.class.equals(cls) || List.class.equals(cls) || Set.class.equals(cls)) {
                    if (javaType.hasSubTypes(1)) {
                        return "list[" + convertJavatToPythonType(javaType.getSubType(0), generics) + "]";
                    }
                    return "list";
                } else if (Iterable.class.equals(cls)) {
                    if (javaType.hasSubTypes(1)) {
                        return "iter[" + convertJavatToPythonType(javaType.getSubType(0), generics) + "]";
                    }
                    return "iter";
                } else if (cls.equals(Class.class)) {
                    if (javaType.hasSubTypes(1)) {
                        return "type[" + convertJavatToPythonType(javaType.getSubType(0), generics) + "]";
                    }
                    return "type";
                }
            }
        } catch (ClassNotFoundException e) {
        }

        return convertBaseJavaToPythonType(javaType, generics);
    }

    private String convertBaseJavaToPythonType(JavaType javaType, Map<String, String> generics) {
        switch (javaType.getType()) {
            case "java.lang.String":
                return "str";
            case "java.lang.Object":
                return "object";
            case "java.time.ZonedDateTime":
                imports.put("__java.time.ZonedDateTime", "from datetime import datetime");
                return "datetime";
            case "java.time.Instant":
                imports.put("__java.time.Instant", "from datetime import datetime");
                return "datetime";
            default:
                String typeName = cleanClassName(javaType.getType());
                if (typeName.contains(".")) {
                    String className = ClassContainer.parsePythonClassName(typeName);
                    // Handle import
                    if (!className.equals(container.getPythonClassName())) {
                        String moduleName = ClassContainer.parsePythonModuleName(typeName);
                        imports.put(typeName, "from " + moduleName + " import " + className);
                        return className;
                    }
                    return "\"" + className + "\"";
                } else if (typeName.length() == 1) {
                    String newTypeName = generics.get(typeName);
                    if (newTypeName != null) {
                        return newTypeName;
                    }
                }
                return typeName;
        }
    }

    private String convertFieldValue(@Nullable Object value) {
        if (value == null) {
            return "None";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "True" : "False";
        }
        if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        }
        String valueAsString = value.toString();
        Matcher matcher = instanceMatcher.matcher(valueAsString);
        if (matcher.find()) {
            valueAsString = "<" + matcher.group(1) + ">";
        }
        return "\"" + valueAsString + "\"";
    }

    private Map<String, String> collectGenerics(Type genericType) {
        return collectGenerics(genericType, new HashMap<String, String>());
    }

    private Map<String, String> collectGenerics(Type genericType, Map<String, String> generics) {
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>) genericType;
            if (!generics.containsKey(typeVar.getTypeName())) {
                generics.put(typeVar.getTypeName(), cleanClassName(typeVar.getBounds()[0].getTypeName()));
            }
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            generics.putAll(collectGenerics(paramType.getRawType(), generics));
            for (Type typeArg : paramType.getActualTypeArguments()) {
                generics.putAll(collectGenerics(typeArg, generics));
            }
        } else if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) genericType;
            if (wildcardType.getUpperBounds().length > 0) {
                for (Type upperType : wildcardType.getUpperBounds()) {
                    generics.putAll(collectGenerics(upperType, generics));
                }
            } else if (wildcardType.getLowerBounds().length > 0) {
                for (Type lowerType : wildcardType.getLowerBounds()) {
                    generics.putAll(collectGenerics(lowerType, generics));
                }
            }
        }
        return generics;
    }

    private String cleanClassName(String className) {
        Matcher matcher = classMatcher.matcher(className);
        if (matcher.find() && !className.equals(matcher.group(1))) {
            // System.out.println("parseSubType: " + container.getRelatedClass().getName() + " | "
            // + _type.getTypeName() + " | " + javaSubType + " | " + matcher.group(1));
            return matcher.group(1);
        }
        return className;
    }

    private @Nullable String buildClassDocumentationBlock() {
        if (!container.getPythonModuleName().startsWith("org.openhab.core")) {
            return null;
        }

        String classUrl = baseUrl
                + container.getRelatedClass().getName().toLowerCase().replace(".", "/").replace("$", ".");

        StringBuilder builder = new StringBuilder();
        builder.append("    \"\"\"\n");
        builder.append("    Java class: ").append(container.getRelatedClass().getName()).append("\n\n");
        builder.append("    Java doc: ").append(classUrl);
        builder.append("\n");
        builder.append("    \"\"\"\n");
        return builder.toString();
    }

    private @Nullable String buildMethodDocumentationBlock(MethodContainer method) {
        if (!container.getPythonModuleName().startsWith("org.openhab.core")) {
            return null;
        }

        String classUrl = baseUrl
                + container.getRelatedClass().getName().toLowerCase().replace(".", "/").replace("$", ".");

        StringBuilder builder = new StringBuilder();
        builder.append("        \"\"\"\n");
        builder.append("        Java doc url:\n");

        String functionRepresentation = method.getRawStringRepresentation();
        Pattern pattern = Pattern.compile("([^\\.]+\\([^\\)]*\\))", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern.matcher(functionRepresentation);
        if (matcher1.find()) {
            functionRepresentation = matcher1.group(1);
        }
        functionRepresentation = functionRepresentation.replaceAll("<>\\?", "").replace("$", ".");
        // System.out.println(classUrl + "#" + functionRepresentation);
        builder.append("        ").append(classUrl).append("#").append(functionRepresentation);

        builder.append("\n");
        builder.append("        \"\"\"\n");
        return builder.toString();
    }

    public static class JavaType {
        private final String type;
        private final List<JavaType> subTypes = new ArrayList<JavaType>();

        public JavaType(String type) {
            this.type = type;
        }

        public void addSubType(JavaType subType) {
            this.subTypes.add(subType);
        }

        public String getType() {
            return this.type;
        }

        public boolean hasSubTypes(int neededSize) {
            return this.subTypes.size() >= neededSize;
        }

        public JavaType getSubType(int index) {
            return this.subTypes.get(index);
        }
    }
}
