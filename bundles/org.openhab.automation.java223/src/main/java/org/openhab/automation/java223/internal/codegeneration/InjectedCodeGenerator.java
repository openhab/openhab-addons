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
package org.openhab.automation.java223.internal.codegeneration;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.voice.VoiceManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate code for imports and default injected field member.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
@Component(service = InjectedCodeGenerator.class)
public class InjectedCodeGenerator {

    private final Logger logger = LoggerFactory.getLogger(InjectedCodeGenerator.class);

    private final String imports;
    private final String importsForSuperClass;
    private final String injectedField;

    private final Map<String, Set<String>> enumByType;

    // We cannot easily find what types to write for those bindings, so we define some exceptions here:
    private static final Map<String, BindingsParsingResult> OVERWRITE_BINDINGS_TYPE = Map.ofEntries(
            getEntry("audio", AudioManager.class), getEntry("voice", VoiceManager.class),
            getEntry("rules", RuleRegistry.class), getEntry("things", ThingRegistry.class),
            getEntry("itemRegistry", ItemRegistry.class), getEntry("ir", ItemRegistry.class),
            new AbstractMap.SimpleEntry<>("items", new BindingsParsingResult("import java.util.Map;",
                    "protected @InjectBinding Map<String, State> items;", ImportIsFor.ALL, null, null)));

    @Activate
    public InjectedCodeGenerator(@Reference ScriptExtensionAccessor scriptExtensionAccessor) {
        Map<String, Object> defaultPresets = scriptExtensionAccessor.findDefaultPresets("");
        List<@Nullable BindingsParsingResult> bindingsParsingResults = parseBindings(defaultPresets);
        this.imports = bindingsParsingResults.stream().filter(Objects::nonNull).map(BindingsParsingResult::importLine)
                .distinct().sorted().collect(Collectors.joining("\n"));
        this.importsForSuperClass = bindingsParsingResults.stream().filter(Objects::nonNull)
                .filter(id -> id.importIsFor == ImportIsFor.ALL).map(BindingsParsingResult::importLine).distinct()
                .sorted().collect(Collectors.joining("\n"));
        this.injectedField = bindingsParsingResults.stream().filter(Objects::nonNull)
                .map(BindingsParsingResult::declaration).filter(Objects::nonNull).filter(Predicate.not(String::isEmpty))
                .distinct().map("    "::concat).sorted().collect(Collectors.joining("\n"));
        this.enumByType = bindingsParsingResults.stream().filter(Objects::nonNull)
                .map(InjectedCodeGenerator::getNonNullEnumTypeAndValue).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.groupingBy(EnumTypeAndValue::enumType,
                        Collectors.mapping(EnumTypeAndValue::enumMember, Collectors.toSet())));
    }

    private static Optional<EnumTypeAndValue> getNonNullEnumTypeAndValue(BindingsParsingResult bpr) {
        String enumType = bpr.enumType;
        String enumMember = bpr.enumMember;
        if (enumType != null && enumMember != null) {
            return Optional.of(new EnumTypeAndValue(enumType, enumMember));
        } else {
            return Optional.empty();
        }
    }

    private static Map.Entry<String, BindingsParsingResult> getEntry(String key, Class<?> clazz) {
        return new AbstractMap.SimpleEntry<>(key, getImportAndDeclaration(key, clazz));
    }

    private static BindingsParsingResult getImportAndDeclaration(String key, Class<?> clazz) {
        return new BindingsParsingResult("import " + clazz.getCanonicalName() + ";",
                "protected @InjectBinding " + clazz.getSimpleName() + " " + key + ";", ImportIsFor.ALL, null, null);
    }

    /**
     * Get all default imports. For the wrapped scripts (one-liner)
     * 
     * @return the imports to write at the top of the wrapped class
     */
    public String getDefaultPresetImportList() {
        return imports;
    }

    /**
     * Get default imports, restricted for the super class (i.e., Java223Script)
     * 
     * @return the imports to write at the top of the generated class
     */
    public String getDefaultPresetImportListForSuperClass() {
        return importsForSuperClass;
    }

    public String getInjectedFieldsDeclaration() {
        return injectedField;
    }

    public Map<String, Set<String>> getEnumByType() {
        return enumByType;
    }

    private List<@Nullable BindingsParsingResult> parseBindings(Map<String, Object> defaultPresets) {
        // create a list of imports for each binding found
        return defaultPresets.entrySet().stream().map(this::generateImportAndDeclaration).distinct().toList();
    }

    /**
     * Generates a Java import statement for a given Class or enum member, with optional enum information.
     * Reject action
     *
     * @param parameter The Class object or an enum member.
     * @return A String representing the would-be import statement, or an empty string if no import is applicable
     *         (e.g., for primitive types, arrays, or classes in the default package). Also with enum information added.
     * @throws IllegalArgumentException if the parameter is null or not a Class or an enum member.
     */
    @Nullable
    private BindingsParsingResult generateImportAndDeclaration(Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();

        switch (value) {
            case Class<?> clazz -> { // the binding value is a class reference. We will generate an import statement,
                // but for wrapped classes only (i.e., one-liner)
                // (example : "import java.time.Duration;" is useless for the Java223Script super class,
                // but it can be useful for user-defined one-liner)
                String canonicalName = clazz.getCanonicalName();
                try {
                    Class.forName(canonicalName, false, getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    logger.debug("Cannot find class {} in classpath, and so cannot use it in the script context",
                            canonicalName);
                    return null; // not directly accessible from scripts (internal classes)
                }

                // Primitive types (e.g., int.class), array types (e.g., String[].class),
                // and classes without a canonical name (e.g., anonymous classes) do not
                // have standard import statements.
                if (clazz.isPrimitive() || clazz.isArray()) {
                    return null;
                }

                Package aPackage = clazz.getPackage();
                String packageName = aPackage != null ? aPackage.getName() : null;

                if (packageName != null && !packageName.isEmpty()) {
                    return new BindingsParsingResult("import " + canonicalName + ";", null,
                            ImportIsFor.WRAPPER_CLASS_ONLY, null, null);
                } else {
                    // Class is in the default package, no import statement needed.
                    return null;
                }
            }
            case Enum<?> enumMember -> { // the binding value is an enum member
                String memberName = enumMember.name();
                String simpleName = enumMember.getDeclaringClass().getSimpleName();

                return new BindingsParsingResult(
                        "import " + enumMember.getDeclaringClass().getCanonicalName() + ";", "protected static final "
                                + simpleName + " " + memberName + " = " + simpleName + "." + memberName + ";",
                        ImportIsFor.ALL, simpleName, memberName);
            }
            default -> { // the binding value is an object
                BindingsParsingResult bindingsParsingResult = OVERWRITE_BINDINGS_TYPE.get(key);
                if (bindingsParsingResult != null) {
                    return bindingsParsingResult;
                }
                Class<?>[] interfaces = value.getClass().getInterfaces();
                if (interfaces.length == 0) { // directly import class name
                    return getImportAndDeclaration(key, value.getClass());
                } else if (interfaces.length == 1) {
                    return getImportAndDeclaration(key, interfaces[0]);
                } else {
                    logger.warn(
                            "Cannot find an appropriate interface for declaring the injected field {} : {}. We pick the first one",
                            key, value.getClass());
                    return getImportAndDeclaration(key, interfaces[0]);
                }
            }
        }
    }

    /**
     * The generated import code can be for
     */
    public enum ImportIsFor {
        ALL,
        // Wrapped classes (i.e., one-liner) will have additional useful import for direct use
        WRAPPER_CLASS_ONLY
    }

    public record BindingsParsingResult(String importLine, @Nullable String declaration, ImportIsFor importIsFor,
            @Nullable String enumType, @Nullable String enumMember) {
    }

    public record EnumTypeAndValue(String enumType, String enumMember) {
    }
}
