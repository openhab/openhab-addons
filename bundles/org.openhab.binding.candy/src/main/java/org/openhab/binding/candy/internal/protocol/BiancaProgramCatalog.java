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
package org.openhab.binding.candy.internal.protocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Parses only public program metadata, never account identifiers or appliance credentials.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public final class BiancaProgramCatalog {
    private static final Set<String> PARAMETERS = Set.of("selector_position", "pr_code", "default_temperature",
            "maximum_temperature", "default_spin_speed", "maximum_spin_speed", "default_soil_level",
            "minimum_soil_level", "maximum_soil_level", "available_options", "available_options2", "steam", "dry",
            "default_duration");

    private BiancaProgramCatalog() {
    }

    public record Program(String id, String name, Map<String, String> parameters, Optional<String> startPayload) {
        public Program {
            parameters = Map.copyOf(parameters);
        }

        public String label() {
            String label = name.replaceFirst("^.*PROGRAM_NAME_", "").replace('_', ' ').toLowerCase(Locale.ROOT);
            return label.isEmpty() ? id : Character.toUpperCase(label.charAt(0)) + label.substring(1);
        }
    }

    /** Public catalog metadata, excluding internal command payloads. */
    public record ProgramOption(String id, String label, boolean startable, Map<String, String> parameters) {
        public ProgramOption {
            parameters = Map.copyOf(parameters);
        }
    }

    public static List<Program> parse(JsonElement source) throws CandyProtocolException {
        if (!source.isJsonArray() || source.getAsJsonArray().size() > 200) {
            throw new CandyProtocolException("@text/catalog-error-response");
        }
        List<Program> programs = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (JsonElement element : source.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            JsonObject program = entry.has("program") && entry.get("program").isJsonObject()
                    ? entry.getAsJsonObject("program")
                    : entry;
            String id = text(program, "id");
            String name = text(program, "name");
            if (!id.matches("[A-Za-z0-9_-]{1,128}") || name.isBlank() || name.length() > 128
                    || "true".equalsIgnoreCase(text(program, "hidden"))) {
                continue;
            }
            if (!ids.add(id)) {
                throw new CandyProtocolException("@text/catalog-error-response");
            }
            Map<String, String> parameters = new LinkedHashMap<>();
            JsonElement raw = program.get("command_parameters");
            boolean valid = false;
            if (raw != null && raw.isJsonArray() && raw.getAsJsonArray().size() <= 100) {
                valid = true;
                for (JsonElement parameter : raw.getAsJsonArray()) {
                    if (!parameter.isJsonObject()) {
                        valid = false;
                        break;
                    }
                    JsonObject value = parameter.getAsJsonObject();
                    if (value.has("command_parameter") && value.get("command_parameter").isJsonObject()) {
                        value = value.getAsJsonObject("command_parameter");
                    }
                    String key = text(value, "name");
                    if (PARAMETERS.contains(key)) {
                        String scalar = text(value, "validation");
                        if (scalar.length() > 32 || parameters.putIfAbsent(key, scalar) != null) {
                            valid = false;
                            break;
                        }
                    }
                }
            }
            Optional<String> payload = Optional.empty();
            if (valid) {
                try {
                    payload = Optional.of(BiancaCommands.start(name, parameters));
                } catch (CandyProtocolException e) {
                    // Incomplete and special-purpose entries remain visible but cannot be started.
                }
            }
            programs.add(new Program(id, name, parameters, payload));
        }
        return List.copyOf(programs);
    }

    /** Returns immutable, credential-free values suitable for rule action outputs. */
    public static List<ProgramOption> options(List<Program> programs) {
        return programs.stream().map(program -> new ProgramOption(program.id(), program.label(),
                program.startPayload().isPresent(), program.parameters())).toList();
    }

    public static String json(List<Program> programs) {
        JsonArray result = new JsonArray();
        for (Program program : programs) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", program.id());
            entry.addProperty("label", program.label());
            entry.addProperty("startable", program.startPayload().isPresent());
            JsonObject parameters = new JsonObject();
            program.parameters().forEach(parameters::addProperty);
            entry.add("parameters", parameters);
            result.add(entry);
        }
        return result.toString();
    }

    private static String text(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }
}
