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
package org.openhab.io.mcp.internal.tools;

import static org.openhab.io.mcp.internal.tools.McpToolUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.io.mcp.internal.util.ItemStateFormatter;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for system information and home status overview.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SystemTools {

    private final ItemRegistry itemRegistry;
    private final ThingRegistry thingRegistry;
    private final RuleRegistry ruleRegistry;
    private final McpJsonMapper jsonMapper;

    /**
     * Creates a new {@code SystemTools} instance.
     *
     * @param itemRegistry the registry used to look up items
     * @param thingRegistry the registry used to look up things
     * @param ruleRegistry the registry used to look up automation rules
     * @param jsonMapper the JSON mapper for serializing tool results
     */
    public SystemTools(ItemRegistry itemRegistry, ThingRegistry thingRegistry, RuleRegistry ruleRegistry,
            McpJsonMapper jsonMapper) {
        this.itemRegistry = itemRegistry;
        this.thingRegistry = thingRegistry;
        this.ruleRegistry = ruleRegistry;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code get_system_info} tool schema.
     * This tool provides openHAB system information including version, counts of items, things,
     * and rules, and a list of installed bindings.
     *
     * @return the MCP tool definition for getting system information
     */
    public McpSchema.Tool getSystemInfoTool() {
        return McpSchema.Tool.builder().name("get_system_info").description(
                "Get openHAB system information including version, item count, thing count, rule count, and installed bindings.")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_system_info} call.
     * Gathers the openHAB version, item/thing/rule counts, and installed binding IDs.
     *
     * @param request the incoming tool call request (no arguments expected)
     * @return a result containing the system information
     */
    public CallToolResult handleGetSystemInfo(McpSchema.CallToolRequest request) {
        Map<String, Object> info = new HashMap<>();
        info.put("version", OpenHAB.getVersion());
        info.put("itemCount", StreamSupport.stream(itemRegistry.getItems().spliterator(), false).count());
        info.put("thingCount", thingRegistry.getAll().size());
        info.put("ruleCount", ruleRegistry.getAll().size());

        List<String> bindings = thingRegistry.getAll().stream().map(thing -> thing.getThingTypeUID().getBindingId())
                .distinct().sorted().toList();
        info.put("installedBindings", bindings);

        return textResult(jsonMapper, info);
    }

    /**
     * Returns the {@code get_home_status} tool schema.
     * This tool provides a comprehensive snapshot of the home's current state covering security,
     * lighting, climate, energy, and device health.
     *
     * @return the MCP tool definition for getting home status
     */
    public McpSchema.Tool getHomeStatusTool() {
        return McpSchema.Tool.builder().name("get_home_status").description("""
                Get a comprehensive snapshot of the home's current state. \
                Returns security status (open doors/windows), active lights by room, \
                climate readings (temperature/humidity), device health (offline things), \
                and energy data. Use this when the user asks about the overall status \
                of their home or wants a summary.""")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_home_status} call.
     * Builds a composite overview including security (open contacts), active lights, climate readings,
     * energy data, and offline device information.
     *
     * @param request the incoming tool call request (no arguments expected)
     * @return a result containing the aggregated home status
     */
    public CallToolResult handleGetHomeStatus(McpSchema.CallToolRequest request) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("security", buildSecurityStatus());
        status.put("lighting", buildLightingStatus());
        status.put("climate", buildClimateStatus());
        status.put("energy", buildEnergyStatus());
        status.put("devices", buildDeviceHealth());
        return textResult(jsonMapper, status);
    }

    private Map<String, Object> buildSecurityStatus() {
        Map<String, Object> security = new LinkedHashMap<>();
        List<Map<String, String>> openContacts = new ArrayList<>();
        int totalContacts = 0;

        for (Item item : itemRegistry.getItems()) {
            if (item instanceof GroupItem) {
                continue;
            }
            State state = item.getState();
            if (state instanceof OpenClosedType) {
                totalContacts++;
                if (state == OpenClosedType.OPEN) {
                    openContacts.add(Map.of("name", item.getName(), "label",
                            Objects.requireNonNullElse(item.getLabel(), item.getName())));
                }
            }
        }
        security.put("totalContactSensors", totalContacts);
        security.put("openCount", openContacts.size());
        if (!openContacts.isEmpty()) {
            security.put("openItems", openContacts);
        }
        return security;
    }

    private Map<String, Object> buildLightingStatus() {
        Map<String, Object> lighting = new LinkedHashMap<>();
        List<Map<String, Object>> activeLights = new ArrayList<>();
        int totalLights = 0;

        for (Item item : itemRegistry.getItems()) {
            if (item instanceof GroupItem) {
                continue;
            }
            if (!isLightItem(item)) {
                continue;
            }
            totalLights++;
            State state = item.getState();
            if (state instanceof UnDefType) {
                continue;
            }
            boolean isOn = false;
            if (state instanceof OnOffType) {
                isOn = state == OnOffType.ON;
            } else if (state instanceof PercentType pct) {
                isOn = pct.intValue() > 0;
            }
            if (isOn) {
                Map<String, Object> light = new LinkedHashMap<>();
                light.put("name", item.getName());
                light.put("label", Objects.requireNonNullElse(item.getLabel(), item.getName()));
                light.put("state", ItemStateFormatter.formatState(state));
                List<String> groups = item.getGroupNames();
                if (!groups.isEmpty()) {
                    light.put("location", groups.getFirst());
                }
                activeLights.add(light);
            }
        }
        lighting.put("totalLights", totalLights);
        lighting.put("onCount", activeLights.size());
        if (!activeLights.isEmpty()) {
            lighting.put("activeLights", activeLights);
        }
        return lighting;
    }

    private Map<String, Object> buildClimateStatus() {
        Map<String, Object> climate = new LinkedHashMap<>();
        List<Map<String, String>> temperatures = new ArrayList<>();
        List<Map<String, String>> humidity = new ArrayList<>();

        for (Item item : itemRegistry.getItems()) {
            if (item instanceof GroupItem || item.getState() instanceof UnDefType) {
                continue;
            }
            String label = Objects.requireNonNullElse(item.getLabel(), "").toLowerCase(Locale.ROOT);
            String name = item.getName().toLowerCase(Locale.ROOT);
            boolean hasTemperatureTag = item.hasTag("Temperature");
            boolean hasHumidityTag = item.hasTag("Humidity");

            if (hasTemperatureTag || (isNumberItem(item) && containsAny(name + label, "temp", "temperature"))) {
                temperatures.add(Map.of("name", item.getName(), "label",
                        Objects.requireNonNullElse(item.getLabel(), item.getName()), "state",
                        ItemStateFormatter.formatState(item.getState())));
            }
            if (hasHumidityTag || (isNumberItem(item) && containsAny(name + label, "humid"))) {
                humidity.add(Map.of("name", item.getName(), "label",
                        Objects.requireNonNullElse(item.getLabel(), item.getName()), "state",
                        ItemStateFormatter.formatState(item.getState())));
            }
        }
        if (!temperatures.isEmpty()) {
            climate.put("temperatures", temperatures);
        }
        if (!humidity.isEmpty()) {
            climate.put("humidity", humidity);
        }
        return climate;
    }

    private Map<String, Object> buildEnergyStatus() {
        Map<String, Object> energy = new LinkedHashMap<>();
        List<Map<String, String>> items = new ArrayList<>();

        for (Item item : itemRegistry.getItems()) {
            if (item instanceof GroupItem || item.getState() instanceof UnDefType) {
                continue;
            }
            if (item.hasTag("Energy") || item.hasTag("Power")) {
                items.add(Map.of("name", item.getName(), "label",
                        Objects.requireNonNullElse(item.getLabel(), item.getName()), "state",
                        ItemStateFormatter.formatState(item.getState())));
            }
        }
        if (!items.isEmpty()) {
            energy.put("items", items);
        }
        return energy;
    }

    private Map<String, Object> buildDeviceHealth() {
        Map<String, Object> devices = new LinkedHashMap<>();
        int online = 0;
        int offline = 0;
        List<Map<String, String>> offlineThings = new ArrayList<>();

        for (Thing thing : thingRegistry.getAll()) {
            ThingStatus thingStatus = thing.getStatus();
            if (thingStatus == ThingStatus.ONLINE) {
                online++;
            } else if (thingStatus == ThingStatus.OFFLINE) {
                offline++;
                offlineThings.add(Map.of("uid", thing.getUID().toString(), "label",
                        Objects.requireNonNullElse(thing.getLabel(), thing.getUID().toString()), "detail",
                        Objects.requireNonNullElse(thing.getStatusInfo().getDescription(), "")));
            }
        }
        devices.put("totalThings", thingRegistry.getAll().size());
        devices.put("online", online);
        devices.put("offline", offline);
        if (!offlineThings.isEmpty()) {
            devices.put("offlineThings", offlineThings);
        }
        return devices;
    }

    private static boolean isLightItem(Item item) {
        if (item.hasTag("Light") || item.hasTag("Lightbulb") || item.hasTag("Lighting")) {
            return true;
        }
        String type = item.getType();
        return ("Switch".equals(type) || "Dimmer".equals(type) || "Color".equals(type))
                && containsAny(item.getName().toLowerCase(Locale.ROOT), "light", "lamp", "bulb");
    }

    private static boolean isNumberItem(Item item) {
        return item.getType().startsWith("Number");
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
