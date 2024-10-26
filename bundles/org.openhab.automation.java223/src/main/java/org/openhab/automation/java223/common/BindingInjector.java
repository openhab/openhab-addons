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
package org.openhab.automation.java223.common;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.internal.strategy.jarloader.JarClassLoader;
import org.openhab.core.automation.module.script.ScriptExtensionManagerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.MemoryClassLoader;

/**
 * Injecting value from binding for script execution
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class BindingInjector {

    private static final Logger logger = LoggerFactory.getLogger(BindingInjector.class);

    /**
     * Smart injection of bindings value into an object.
     *
     * @param sourceScriptClassLoader The Script class loader initiating the execution
     * @param bindings a bindings maps with value to inject
     * @param objectToInjectInto An object. Its fields will be filled with value from the
     *            bindings if a match is found
     */
    public static void injectBindingsInto(ClassLoader sourceScriptClassLoader, Map<String, Object> bindings,
            Object objectToInjectInto) {
        try {
            injectBindingsInto(sourceScriptClassLoader, bindings, objectToInjectInto, new HashMap<>());
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InstantiationException
                | InvocationTargetException e) {
            logger.error("Cannot inject bindings or libs", e);
        }
    }

    private static void injectBindingsInto(ClassLoader sourceScriptClassLoader, Map<String, Object> bindings,
            Object objectToInjectInto, Map<Class<?>, Object> libAlreadyInstantiated)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<?> clazz = objectToInjectInto.getClass();

        for (Field field : getAllFields(clazz)) {
            if (!field.accessFlags().contains(AccessFlag.FINAL)) { // inject value only in non-final fields
                Object valueToInject = extractBindingValueForElement(sourceScriptClassLoader, bindings, field,
                        libAlreadyInstantiated);
                if (valueToInject != null) {
                    field.setAccessible(true);
                    field.set(objectToInjectInto, valueToInject);
                }
            }
        }
    }

    /**
     * Search what to inject into an element.
     * Find a library (or compute a name to use as a key), then use this key to search for a value in the binding data
     *
     * @param sourceScriptClassLoader The class loader of the script initiating the execution
     * @param bindings a map where to find the data to inject
     * @param annotatedElement the field/parameter element to inject value into
     **/
    public static @Nullable Object extractBindingValueForElement(ClassLoader sourceScriptClassLoader,
            Map<String, Object> bindings, AnnotatedElement annotatedElement) {
        try {
            return extractBindingValueForElement(sourceScriptClassLoader, bindings, annotatedElement, new HashMap<>());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new Java223Exception("Cannot extract binding value for an element", e);
        }
    }

    private static @Nullable Object extractBindingValueForElement(ClassLoader classLoader, Map<String, Object> bindings,
            AnnotatedElement annotatedElement, Map<Class<?>, Object> libAlreadyInstantiated)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<?> fieldType;
        String codeName;
        if (annotatedElement instanceof Parameter parameter) {
            fieldType = parameter.getType();
            codeName = parameter.getName();
        } else if (annotatedElement instanceof Field field) {
            fieldType = field.getType();
            codeName = field.getName();
        } else {
            logger.warn("Cannot check target class for parameter. Only Parameter or Field accepted. Cannot inject.");
            return null;
        }

        // step zero: exclusion cases
        if (fieldType.isArray() || fieldType.isAnnotation()) {
            return null;
        }
        Optional<InjectBinding> injectBindingAnnotation = Optional
                .ofNullable(annotatedElement.getAnnotation(InjectBinding.class));
        if (injectBindingAnnotation.isPresent() && injectBindingAnnotation.get().disable()) {
            return null;
        }

        // first, special case, is the field a library?
        if (containsLibrary(classLoader, fieldType.getName())) { // it's a library
            InjectBinding libraryAnnotation = fieldType.getAnnotation(InjectBinding.class);
            if (libraryAnnotation != null && libraryAnnotation.disable()) { // but it's disabled at class level,
                // so no injection
                return null;
            }
            boolean recursive = libraryAnnotation == null || libraryAnnotation.recursive();
            // has it already been instantiated and stored in the store?
            return getOrInstantiateObject(classLoader, bindings, libAlreadyInstantiated, fieldType, recursive);
        }

        // second. It's not a library, so we will search value in the binding map.
        // 2.a Choose a name to search as a key in the binding map
        // the name can be a path inside the object
        String named;
        if (injectBindingAnnotation.isPresent()
                && !injectBindingAnnotation.get().named().equals(Java223Constants.ANNOTATION_DEFAULT)) {
            named = injectBindingAnnotation.get().named();
        } else {
            named = codeName;
        }
        Queue<String> namePath = new LinkedList<>(Arrays.asList(named.split("\\.")));

        // 2.b, choose where to look: in bindings, or deeper, in a preset :
        Object value = bindings;
        boolean found = false;
        if (injectBindingAnnotation.isPresent()
                && !injectBindingAnnotation.get().preset().equals(Java223Constants.ANNOTATION_DEFAULT)) {
            ScriptExtensionManagerWrapper se = (ScriptExtensionManagerWrapper) bindings.get("scriptExtension");
            if (se != null) {
                Map<String, Object> presetMap = se.importPreset(injectBindingAnnotation.get().preset());
                if (!presetMap.isEmpty()) {
                    value = presetMap;
                } else {
                    logger.warn("Cannot find the preset {} for the named parameter {}",
                            injectBindingAnnotation.get().preset(), named);
                }
            } else {
                logger.warn("Cannot find scriptExtension in bindings. Should not happen");
            }
        }

        // 2.c, browse deep inside the object if there is a path to traverse
        while (!namePath.isEmpty()) {
            if (value == null) {
                logger.debug("Find null value for the path {}", named);
                break;
            }
            String namePart = namePath.poll();
            if (value instanceof Map<?, ?> elementToParseAsMap) {
                value = elementToParseAsMap.get(namePart);
                if (elementToParseAsMap.containsKey(namePart)) {
                    found = true;
                } else {
                    logger.trace("Didn't find an element with the key '{}'. Ignoring (not an error)", namePart);
                }
            } else {
                try {
                    if (namePart != null) {
                        Field targetField = getFieldDeep(value.getClass(), namePart);
                        targetField.setAccessible(true);
                        value = targetField.get(value);
                        found = true;
                    }
                } catch (NoSuchFieldException | SecurityException e) {
                    logger.debug("Cannot map a value to the path {}", named);
                    value = null;
                    found = false;
                    break;
                }
            }
        }

        // third, search for an osgi service
        if (value == null && !found) {
            ServiceGetter serviceGetter = (ServiceGetter) bindings.get(Java223Constants.SERVICE_GETTER);
            if (serviceGetter == null) {
                logger.trace(
                        "Cannot find a service getter in bindings. Probably in a rule action. Skipping service lookup");
            } else {
                value = serviceGetter.getService(fieldType);
                found = value != null;
            }
        }

        // fourth, check if it is mandatory
        if (!found && injectBindingAnnotation.isPresent() && injectBindingAnnotation.get().mandatory()) {
            throw new Java223Exception("There is no value found for parameter/field named " + named
                    + ", but it is mandatory. We cannot inject it");
        } else if (value == null) {
            return null;
        }

        // six, check class compatibility
        if (!fieldType.isAssignableFrom(value.getClass())) {
            logger.warn(
                    "Parameter/field entry {} is of class {} and not assignable to type {}. Did you use a reserved variable name ?",
                    named, value.getClass().getName(), fieldType.getName());
        }
        return value;
    }

    /**
     * Retrieves an existing instance of the specified type from cached bindings or creates a new instance
     * of the specified type if none exists. If a new instance is created, bindings are injected into it.
     * Not used internally, but available for user scripting (utility method taking care of implementation quirk)
     *
     * @param <T> The type of the object to retrieve or instantiate
     * @param classLoader The origin script class loader
     * @param bindings A map containing data that may be injected into the object
     * @param fieldType The class of the object to retrieve or create
     * @return An instance of the specified type
     * @throws Java223Exception If an instance of the specified type cannot be instantiated
     */
    @SuppressWarnings("unused")
    public static <T> T getOrInstantiateObject(ClassLoader classLoader, Map<String, Object> bindings,
            Class<T> fieldType) {
        try {
            return getOrInstantiateObject(classLoader, bindings, new HashMap<>(), fieldType, true);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new Java223Exception("Cannot instantiate " + fieldType.getName(), e);
        }
    }

    private static <T> T getOrInstantiateObject(ClassLoader classLoader, Map<String, Object> bindings,
            Map<Class<?>, Object> libAlreadyInstantiated, Class<T> fieldType, boolean recursive)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object valueToInject = libAlreadyInstantiated.get(fieldType);
        if (valueToInject == null) { // not instantiated, create it
            Constructor<?>[] constructors = fieldType.getDeclaredConstructors();
            // use the empty constructor if available, or the first one
            Constructor<?> constructor = Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst()
                    .orElseGet(() -> constructors[0]);
            @Nullable
            Object[] parameterValues = getParameterValuesFor(classLoader, constructor, bindings,
                    libAlreadyInstantiated);
            valueToInject = constructor.newInstance(parameterValues);
            if (valueToInject != null) { // cannot be null, but null-check thinks so
                // store it to avoid multiple instantiation
                libAlreadyInstantiated.put(fieldType, valueToInject);
                if (recursive) {
                    // and then also use injection into it
                    injectBindingsInto(classLoader, bindings, valueToInject, libAlreadyInstantiated);
                }
            }
        }
        if (valueToInject == null) { // cannot be null, but null-check thinks so
            throw new Java223Exception("Cannot instantiate " + fieldType.getName());
        }
        @SuppressWarnings("unchecked")
        T valueToInject1 = (T) valueToInject;
        return valueToInject1;
    }

    // Check if a classLoader contains a library
    private static boolean containsLibrary(ClassLoader classLoader, String name) {
        // scripts are constructed by the Java223Strategy and by JavaScriptEngine
        // we know that the ClassLoader is a MemoryClassLoader (contains all .java lib + the script)
        // and that the parent is a JarClassLoader (contains all .jar lib).
        // so we ask them if they loaded the class themselves
        var memoryClassLoader = (MemoryClassLoader) classLoader;
        var parentJarClassLoader = (JarClassLoader) Optional.ofNullable(memoryClassLoader.getParent())
                .orElseThrow(() -> new IllegalArgumentException("ClassLoader cannot be null"));
        return parentJarClassLoader.isLoadedClass(name) || memoryClassLoader.isLoadedClass(name);
    }

    /**
     * Find the appropriate parameters value in the binding map for the executable to run.
     *
     * @param classLoader the source script class loader
     * @param executable Method or constructor
     * @param bindings The map used to search the appropriate value to inject
     * @param libAlreadyInstantiated To avoid looping the instantiation of libraries
     * @return An array of parameter values that fits the executable
     * @throws InstantiationException If instantiation of the parameter doesn't work
     * @throws IllegalAccessException If reflexion fails
     * @throws IllegalArgumentException If reflexion fails
     * @throws InvocationTargetException If reflexion fails
     */
    public static @Nullable Object[] getParameterValuesFor(ClassLoader classLoader, Executable executable,
            Map<String, Object> bindings, @Nullable Map<Class<?>, Object> libAlreadyInstantiated)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Parameter[] parameters = executable.getParameters();
        @Nullable
        Object[] parameterValues = new Object[parameters.length];
        Map<Class<?>, Object> libAlreadyInstantiatedLocal = libAlreadyInstantiated != null ? libAlreadyInstantiated
                : new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            parameterValues[i] = extractBindingValueForElement(classLoader, bindings, parameters[i],
                    libAlreadyInstantiatedLocal);
        }
        return parameterValues;
    }

    /**
     * Browse all hierarchy to find a field with the specified name.
     *
     * @param _clazz the class to browse
     * @param fieldName the name of the field to find
     * @return the field
     * @throws NoSuchFieldException if the field cannot be found
     */
    private static Field getFieldDeep(Class<?> _clazz, String fieldName) throws NoSuchFieldException {
        Class<?> clazz = _clazz;
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException();
    }

    private static Set<Field> getAllFields(Class<?> type) {
        Set<Field> fields = new HashSet<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
}
