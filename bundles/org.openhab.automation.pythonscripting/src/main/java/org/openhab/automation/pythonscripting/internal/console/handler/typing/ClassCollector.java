/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pythonscripting.internal.console.handler.TypingCmd.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Collects classes
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class ClassCollector {
    private final Logger logger;

    public ClassCollector(Logger logger) {
        this.logger = logger;
    }

    public Map<String, ClassContainer> collectBundleClasses(String packageName) throws Exception {
        List<Class<?>> clsList = new ArrayList<Class<?>>();
        Bundle bundle = FrameworkUtil.getBundle(ClassCollector.class);
        Bundle[] bundles = bundle.getBundleContext().getBundles();
        for (Bundle b : bundles) {
            List<Class<?>> bundleClsList = new ArrayList<Class<?>>();
            Enumeration<URL> entries = b.findEntries(packageName.replace(".", "/"), "*.class", true);
            if (entries != null) {
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().toString();
                    String clsName = entry.substring(entry.indexOf(packageName.replace(".", "/")));
                    if (clsName.indexOf("/internal") != -1) {
                        continue;
                    }
                    clsName = clsName.replace(".class", "").replace("/", ".");
                    try {
                        Class<?> cls = Class.forName(clsName);
                        if (!Modifier.isPublic(cls.getModifiers())) {
                            continue;
                        }
                        bundleClsList.add(cls);
                    } catch (ClassNotFoundException e) {
                        logger.warn("BUNDLE: " + b + " class " + clsName + " not found");
                    }
                }
            }
            if (!bundleClsList.isEmpty()) {
                logger.info("BUNDLE: " + b + " with " + bundleClsList.size() + " classes processed");
                clsList.addAll(bundleClsList);
            }
        }
        return processClasses(clsList);
    }

    public Map<String, ClassContainer> collectReflectionClasses(Set<String> imports) throws Exception {
        List<Class<?>> clsList = new ArrayList<Class<?>>();
        for (String imp : imports) {
            if (imp.startsWith("__")) {
                continue;
            }

            try {
                clsList.add(Class.forName(imp));
            } catch (ClassNotFoundException e) {
                logger.warn("Class " + imp + " not found");
            }
        }
        return processClasses(clsList);
    }

    private Map<String, ClassContainer> processClasses(List<Class<?>> clsList) {
        Map<String, ClassContainer> result = new HashMap<String, ClassContainer>();
        for (Class<?> cls : clsList) {
            result.put(cls.getName(), new ClassContainer(cls));
        }
        return result;
    }

    public static class ClassContainer {
        private Class<?> cls;
        private List<Field> fields;
        private Map<String, MethodContainer> methods = new HashMap<String, ClassCollector.MethodContainer>();

        private String pythonClassName;
        private String pythonModuleName;

        public ClassContainer(Class<?> cls) {
            this.cls = cls;

            String packageName = cls.getName();
            this.pythonClassName = ClassContainer.parsePythonClassName(packageName);
            this.pythonModuleName = ClassContainer.parsePythonModuleName(packageName);

            fields = Arrays.stream(cls.getDeclaredFields()).filter(
                    method -> Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()))
                    .collect(Collectors.toList());

            Constructor<?>[] constructors = cls.getConstructors();
            for (Constructor<?> constructor : constructors) {
                String uid = constructor.getName();
                MethodContainer methodContainer;
                if (!this.methods.containsKey(uid)) {
                    methodContainer = new MethodContainer(constructor);
                    this.methods.put(uid, methodContainer);
                }
                this.methods.get(uid).addParametersFrom(constructor);
            }

            List<Method> methods = Arrays.stream(cls.getDeclaredMethods()).filter(
                    method -> Modifier.isPublic(method.getModifiers()) && !Modifier.isVolatile(method.getModifiers()))
                    .collect(Collectors.toList());
            for (Method method : methods) {
                String uid = method.getName();
                MethodContainer methodContainer;
                if (!this.methods.containsKey(uid)) {
                    methodContainer = new MethodContainer(method);
                    this.methods.put(uid, methodContainer);
                }
                this.methods.get(uid).addParametersFrom(method);
            }
        }

        public Class<?> getRelatedClass() {
            return cls;
        }

        public List<Field> getFields() {
            return fields;
        }

        public List<MethodContainer> getMethods() {
            return new ArrayList<MethodContainer>(methods.values());
        }

        public String getPythonClassName() {
            return pythonClassName;
        }

        public String getPythonModuleName() {
            return pythonModuleName;
        }

        public static String parsePythonClassName(String name) {
            String className = name.substring(name.lastIndexOf(".") + 1);
            if (className.contains("$")) {
                className = className.replace("$", "_");
            }
            return className;
        }

        public static String parsePythonModuleName(String name) {
            return name.substring(0, name.lastIndexOf("."));
        }
    }

    public static class MethodContainer {
        int modifier;
        String methodName;
        String rawStringRepresentation;
        boolean isConstructor = false;
        int mandatoryParameterCount = 999;

        List<Type> returnTypes = new ArrayList<Type>();
        List<Class<?>> returnClasses = new ArrayList<Class<?>>();
        List<ParameterContainer> args = new ArrayList<ParameterContainer>();

        public MethodContainer(Constructor<?> constructor) {
            this.modifier = constructor.getModifiers();
            this.methodName = ClassContainer.parsePythonClassName(constructor.getName());
            this.rawStringRepresentation = constructor.toString();
            this.isConstructor = true;
        }

        public MethodContainer(Method method) {
            this.modifier = method.getModifiers();
            this.methodName = method.getName();
            this.rawStringRepresentation = method.toString();
            this.returnTypes.add(method.getGenericReturnType());
            this.returnClasses.add(method.getReturnType());
        }

        public void addParametersFrom(Constructor<?> constructor) {
            init(constructor.getParameters(), constructor.getGenericParameterTypes());
        }

        public void addParametersFrom(Method method) {
            returnTypes.add(method.getGenericReturnType());
            returnClasses.add(method.getReturnType());

            init(method.getParameters(), method.getGenericParameterTypes());
        }

        private void init(Parameter[] parameters, Type[] gpType) {
            for (int i = 0; i < parameters.length; i++) {
                if (args.size() <= i) {
                    args.add(new ParameterContainer(parameters[i], gpType[i]));
                } else {
                    args.get(i).addParameter(gpType[i]);
                }
            }

            if (parameters.length < mandatoryParameterCount) {
                mandatoryParameterCount = parameters.length;
            }
            for (int i = mandatoryParameterCount; i < args.size(); i++) {
                args.get(i).markAsOptional();
            }
        }

        public String getPythonMethodName() {
            return methodName;
        }

        public boolean isConstructor() {
            return this.isConstructor;
        }

        public int getModifiers() {
            return modifier;
        }

        public String getRawStringRepresentation() {
            return rawStringRepresentation;
        }

        public int getReturnTypeCount() {
            return returnTypes.size();
        }

        public Type getGenericReturnType(int index) {
            return returnTypes.get(index);
        }

        public Class<?> getReturnType(int index) {
            return returnClasses.get(index);
        }

        public List<ParameterContainer> getParameters() {
            return args;
        }

        public int getParameterCount() {
            return args.size();
        }

        public ParameterContainer getParameter(int index) {
            return args.get(index);
        }
    }

    public static class ParameterContainer {
        Parameter parameter;
        boolean isOptional = false;
        List<Type> types = new ArrayList<Type>();

        public ParameterContainer(Parameter arg, Type type) {
            this.parameter = arg;
            this.types.add(type);
        }

        public void addParameter(Type type) {
            types.add(type);
        }

        public String getName() {
            return parameter.getName();
        }

        public int getTypeCount() {
            return types.size();
        }

        public Type getGenericType(int index) {
            return types.get(index);
        }

        public void markAsOptional() {
            isOptional = true;
        }

        public boolean isOptional() {
            return isOptional;
        }
    }
}
