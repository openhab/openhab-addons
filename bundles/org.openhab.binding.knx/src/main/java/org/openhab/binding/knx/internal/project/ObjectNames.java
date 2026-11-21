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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.ProgramFile.AllocatorEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ArgumentEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectRefEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ParameterEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ParameterRefEntry;

/**
 * The name of a group object as ETS shows it: the program's text, translated, with its placeholders filled.
 * {@code {{n}}} and {@code {{n:default}}} take the value of the object's text parameter, {@code {{name}}} the
 * module argument with that name. Placeholders nest and are filled from the inside. When one cannot be filled,
 * the name is the text stored on the instance, else the text without that placeholder.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ObjectNames {

    /** Rounds of filling; each round fills the innermost placeholders, so this is the deepest nesting. */
    static final int MAX_ROUNDS = 8;
    /** Characters of a name while it is filled; a value that grows it beyond this is not filled. */
    static final int MAX_LENGTH = 1024;
    /** The highest object number of a device. */
    static final int MAX_OBJECT_NUMBER = 65535;

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^{}]*)\\}\\}");
    private static final Pattern PARAMETER = Pattern.compile("(\\d+)(?::(.*))?", Pattern.DOTALL);
    private static final Pattern ALLOCATOR = Pattern.compile("(MD-\\d+_)?L-\\d+");
    // group 2: the parameter and its reference after the module definition part
    private static final Pattern MODULE_PARAMETER = Pattern.compile("(MD-\\d+.*?)_(P-.+)");
    private static final Pattern INDEX = Pattern.compile("\\d+");

    private final KnxDevice device;
    private final ProgramFile program;
    private final ComObjectRefEntry reference;
    private final @Nullable String moduleInstanceId;
    private boolean unfilled;

    private ObjectNames(KnxDevice device, ProgramFile program, ComObjectRefEntry reference,
            @Nullable String moduleInstanceId) {
        this.device = device;
        this.program = program;
        this.reference = reference;
        this.moduleInstanceId = moduleInstanceId;
    }

    /** Whether a module argument value names an allocator ({@code L-n} or {@code MD-n_L-n}). */
    static boolean isAllocator(String value) {
        return ALLOCATOR.matcher(value).matches();
    }

    /** The name of an object whose definition was found. */
    static String name(KnxDevice device, KnxObjectInstance instance, ProgramFile program, String referenceId,
            ComObjectRefEntry reference, ComObjectEntry object) {
        String own = nonEmpty(instance.text());
        String text;
        String element;
        String byReference = nonEmpty(reference.text());
        String byObject = nonEmpty(object.text());
        if (byReference != null) {
            text = byReference;
            element = referenceId;
        } else if (byObject != null) {
            text = byObject;
            element = reference.refId();
        } else {
            return own == null ? "" : own;
        }
        String translated = nonEmpty(program.translation(ProgramFile.TEXT, element));
        ObjectNames names = new ObjectNames(device, program, reference,
                GroupObjects.moduleInstanceId(instance.refId()));
        String name = names.fill(translated == null ? text : translated);
        return names.unfilled && own != null ? own : name;
    }

    /** The name of an object whose definition was not found: the text stored on the instance. */
    static String nameWithoutDefinition(KnxObjectInstance instance) {
        String own = nonEmpty(instance.text());
        return own == null ? "" : own;
    }

    /** A function text of the program in the chosen language. */
    static String translated(ProgramFile program, String text, String elementId) {
        String translated = nonEmpty(program.translation(ProgramFile.FUNCTION_TEXT, elementId));
        return translated == null ? text : translated;
    }

    private String fill(String text) {
        String current = text;
        for (int round = 0; round < MAX_ROUNDS; round++) {
            Matcher placeholder = PLACEHOLDER.matcher(current);
            if (!placeholder.find()) {
                return current;
            }
            StringBuilder next = new StringBuilder();
            do {
                String value = value(placeholder.group(1));
                if (value == null) {
                    unfilled = true;
                    value = "";
                }
                placeholder.appendReplacement(next, Matcher.quoteReplacement(value));
            } while (placeholder.find());
            placeholder.appendTail(next);
            if (next.length() > MAX_LENGTH) {
                unfilled = true;
                return removePlaceholders(current);
            }
            current = next.toString();
        }
        if (PLACEHOLDER.matcher(current).find()) {
            unfilled = true;
            return removePlaceholders(current);
        }
        return current;
    }

    private static String removePlaceholders(String text) {
        String current = text;
        String removed = PLACEHOLDER.matcher(current).replaceAll("");
        while (!removed.equals(current)) {
            current = removed;
            removed = PLACEHOLDER.matcher(current).replaceAll("");
        }
        return current;
    }

    /** The value of one placeholder's content, {@code null} when it cannot be filled. */
    private @Nullable String value(String content) {
        Matcher parameter = PARAMETER.matcher(content);
        if (parameter.matches()) {
            String value = parameterValue();
            String fallback = parameter.group(2);
            return (value == null || value.isEmpty()) && fallback != null ? fallback : value;
        }
        return content.isEmpty() ? null : argumentValue(content);
    }

    /** The device's value of the text parameter, else the parameter reference's, else the parameter's. */
    private @Nullable String parameterValue() {
        String textParameter = nonEmpty(reference.textParameterRefId());
        if (textParameter == null) {
            return null;
        }
        String key = deviceKey(textParameter);
        if (key != null) {
            for (KnxParameterValue value : device.parameters()) {
                if (key.equals(value.refId()) && value.value() != null) {
                    return value.value();
                }
            }
        }
        ParameterRefEntry parameterRef = program.parameterRefs.get(textParameter);
        if (parameterRef == null) {
            return null;
        }
        if (parameterRef.value() != null) {
            return parameterRef.value();
        }
        ParameterEntry parameter = program.parameters.get(parameterRef.refId());
        return parameter == null ? null : parameter.value();
    }

    /**
     * The key of the device's value: the text parameter itself, or inside a module the program id, the module
     * instance and the parameter reference; {@code null} when a module's parameter belongs to no module instance.
     */
    private @Nullable String deviceKey(String textParameter) {
        String prefix = program.id() + "_";
        if (!textParameter.startsWith(prefix)) {
            return textParameter;
        }
        Matcher module = MODULE_PARAMETER.matcher(textParameter.substring(prefix.length()));
        if (!module.matches()) {
            return textParameter;
        }
        String instance = moduleInstanceId;
        return instance == null ? null : prefix + instance + "_" + module.group(2);
    }

    /**
     * The device's object number of an object inside a module: the whole number the {@code BaseNumber} argument
     * has for the object's module instance, or for the nearest parent that has it, plus the object's own
     * number. {@code baseNumber} names the argument by its id in the program, with or without the program id in
     * front. {@code null} when it cannot be worked out: the object is in no module, no such argument, no value,
     * a value that is no whole number, a nested instance's allocator, or a sum outside 0 to 65535.
     */
    static @Nullable Integer deviceNumber(KnxDevice device, KnxObjectInstance instance, ProgramFile program,
            ComObjectRefEntry reference, ComObjectEntry object) {
        Integer own = object.number();
        String base = object.baseNumber();
        if (own == null || base == null) {
            return null;
        }
        ObjectNames names = new ObjectNames(device, program, reference,
                GroupObjects.moduleInstanceId(instance.refId()));
        Long value = ProgramReader.wholeNumber(names.argumentValueById(base.trim()));
        if (value == null) {
            return null;
        }
        if (value < 0 || value > MAX_OBJECT_NUMBER) {
            return null;
        }
        long sum = value + own;
        return sum <= MAX_OBJECT_NUMBER ? Integer.valueOf((int) sum) : null;
    }

    /** The argument with this name: of the object's module instance first, then of its parents. */
    private @Nullable String argumentValue(String name) {
        return argumentValue(name, null);
    }

    /** The argument whose definition has this id, with or without the program id in front. */
    private @Nullable String argumentValueById(String id) {
        return argumentValue(null, id);
    }

    private @Nullable String argumentValue(@Nullable String name, @Nullable String id) {
        String instanceId = moduleInstanceId;
        if (instanceId == null) {
            return null;
        }
        List<KnxModuleInstance> chain = new ArrayList<>();
        for (KnxModuleInstance module : device.modules()) {
            if (instanceId.equals(module.id()) || instanceId.startsWith(module.id() + "_")) {
                chain.add(module);
            }
        }
        chain.sort(Comparator.comparingInt((KnxModuleInstance module) -> module.id().length()).reversed());
        int end = instanceId.indexOf('_');
        String moduleDef = end < 0 ? instanceId : instanceId.substring(0, end);
        for (KnxModuleInstance module : chain) {
            for (KnxModuleArgument argument : module.arguments()) {
                String key = definitionKey(argument.refId(), moduleDef);
                ArgumentEntry definition = key == null ? null : program.arguments.get(key);
                if (key != null && definition != null && matches(name, id, key, definition)) {
                    return argumentValue(argument, definition, module);
                }
            }
        }
        return null;
    }

    /** Whether the definition has the name asked for, or else the id, with or without the program id. */
    private boolean matches(@Nullable String name, @Nullable String id, String key, ArgumentEntry definition) {
        if (name != null) {
            return name.equals(definition.name());
        }
        return id != null && (id.equals(key) || (program.id() + "_" + id).equals(key));
    }

    /**
     * The key in the program of an argument's definition: {@code <appId>_<refId>}, else, for the short form of a
     * submodule argument, the first of the module definition whose id ends in {@code _<refId>}.
     */
    private @Nullable String definitionKey(String refId, String moduleDef) {
        String exact = program.id() + "_" + refId;
        if (program.arguments.get(exact) != null) {
            return exact;
        }
        String inModule = program.id() + "_" + moduleDef + "_";
        for (Map.Entry<String, ArgumentEntry> entry : program.arguments.entries()) {
            String id = entry.getKey();
            if (id.startsWith(inModule) && id.endsWith("_" + refId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * A plain value as it is; an allocator id as {@code Start + (k - 1) * Allocates}, with k the number after
     * the last {@code _MI-} of the module instance. A nested submodule instance ({@code MI-n-n-n}) has no such
     * number, so its allocator values are not computed.
     */
    private @Nullable String argumentValue(KnxModuleArgument argument, ArgumentEntry definition,
            KnxModuleInstance module) {
        String value = argument.value();
        if (value == null || !isAllocator(value)) {
            return value;
        }
        AllocatorEntry allocator = program.allocators.get(program.id() + "_" + value);
        Long start = allocator == null ? null : allocator.start();
        Long step = ProgramReader.wholeNumber(definition.allocates());
        int at = module.id().lastIndexOf("_MI-");
        String index = at < 0 ? "" : module.id().substring(at + "_MI-".length());
        if (start == null || step == null || !INDEX.matcher(index).matches()) {
            return null;
        }
        try {
            long k = Long.parseLong(index);
            return String.valueOf(Math.addExact(start, Math.multiplyExact(k - 1, step)));
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }

    private static @Nullable String nonEmpty(@Nullable String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
