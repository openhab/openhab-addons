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
package org.openhab.binding.solarman.internal.sunsynk;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads complete resolved profiles and plans contiguous reads independently of channel links.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class SunsynkProfile {
    private final Map<String, SunsynkSensor> sensors = new LinkedHashMap<>();

    public SunsynkProfile(String name) throws IOException {
        if (!SolarmanBindingConstants.SUNSYNK_PROFILES.containsValue(name)) {
            throw new IllegalArgumentException("Unknown Sunsynk profile");
        }
        try (InputStream stream = Objects.requireNonNull(
                SunsynkProfile.class.getResourceAsStream("/sunsynk/" + name + ".json"), "Missing profile resource")) {
            loadSensors(stream);
        }
        Optional<InputStream> metadata = Optional
                .ofNullable(SunsynkProfile.class.getResourceAsStream("/sunsynk/metadata/" + name + ".json"));
        if (metadata.isPresent()) {
            try (InputStream stream = metadata.orElseThrow()) {
                loadSensors(stream);
            }
        }
    }

    private void loadSensors(InputStream stream) throws IOException {
        JsonNode array = Objects.requireNonNull(new ObjectMapper().readTree(stream));
        for (JsonNode item : array) {
            SunsynkSensor sensor = new SunsynkSensor(item);
            if (sensors.containsKey(sensor.id())) {
                throw new IOException("Duplicate Sunsynk channel ID: " + sensor.id());
            }
            sensors.put(sensor.id(), sensor);
        }
    }

    public List<SunsynkSensor> sensors() {
        return List.copyOf(sensors.values());
    }

    public List<Request> readPlan() {
        TreeSet<Integer> addresses = new TreeSet<>();
        for (int i = 0; i < 8; i++) {
            addresses.add(i);
        }
        sensors.values().forEach(s -> addresses.addAll(s.registers()));
        List<Request> plan = new ArrayList<>();
        int start = -1;
        int end = -1;
        for (int address : addresses) {
            if (address < 0 || address > 65535) {
                throw new IllegalArgumentException("Register address is outside Modbus range");
            }
            if (start < 0 || address - start >= 125) {
                if (start >= 0) {
                    plan.add(new Request(3, start, end));
                }
                start = address;
            }
            end = address;
        }
        plan.add(new Request(3, start, end));
        for (Request block : plan) {
            if (block.getStart().equals(block.getEnd())) {
                int previous = Objects.requireNonNullElse(addresses.lower(block.getStart()), -1);
                int next = Objects.requireNonNullElse(addresses.higher(block.getEnd()), 65536);
                if (previous >= 0 && block.getStart() - previous < 125) {
                    block.setStart(previous);
                } else if (next <= 65535 && next - block.getStart() < 125) {
                    block.setEnd(next);
                } else {
                    throw new IllegalArgumentException("Profile contains an isolated unsupported read window");
                }
            }
        }
        return plan;
    }

    public Map<String, State> decode(Map<Integer, byte[]> snapshot, ZoneId zone) {
        Map<String, State> result = new LinkedHashMap<>();
        for (SunsynkSensor sensor : sensors.values()) {
            if (!"PVDynamicTotalSensor".equals(sensor.kind())) {
                try {
                    result.put(sensor.id(), sensor.decode(snapshot, zone));
                } catch (IllegalArgumentException | java.time.DateTimeException e) {
                    result.put(sensor.id(), UnDefType.UNDEF);
                }
            }
        }
        for (SunsynkSensor sensor : sensors.values()) {
            if ("PVDynamicTotalSensor".equals(sensor.kind())) {
                BigDecimal total = BigDecimal.ZERO;
                boolean complete = true;
                boolean found = false;
                for (JsonNode source : sensor.definition().path("source_ids")) {
                    if (sensors.containsKey(source.asText())) {
                        found = true;
                        State value = result.get(source.asText());
                        if (value instanceof QuantityType<?> quantity) {
                            total = total.add(quantity.toBigDecimal());
                        } else {
                            complete = false;
                        }
                    }
                }
                result.put(sensor.id(), complete && found ? sensor.numeric(total) : UnDefType.UNDEF);
            }
        }
        return result;
    }
}
