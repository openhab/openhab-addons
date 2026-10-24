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
package org.openhab.binding.smartthings.internal.ocf;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Cached OCF representations and deliberately limited, verified Samsung appliance controls.
 * Only recognized appliance capabilities are exposed as channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class Resources {
    private static final String VENDOR = "x.com.samsung.da.";
    private static final String POWER = "/power/0";
    private static final String POWER_VS = "/power/vs/0";
    private static final String REMOTE = "/remotectrl/0";
    private static final String REMOTE_VS = "/remotectrl/vs/0";
    private static final String CURRENT = "/temperature/current/0";
    private static final String DESIRED = "/temperature/desired/0";
    private static final String TEMPERATURE_CONTROL = "/temperature/control/vs/0";
    private static final String AIRFLOW = "/airflow/0";
    private static final String MODE = "/mode/vs/0";
    private static final String TEMPERATURES_VS = "/temperatures/vs/0";
    private static final String WIND_STRENGTH = "/wind/strength/vs/0";
    private static final String WIND_DIRECTION = "/wind/direction/vs/0";
    private static final String CONVENIENT = "/mode/convenient/vs/0";
    private static final String ENERGY = "/energy/consumption/vs/0";
    private static final String OPERATIONAL_STATE = "/operational/state/0";
    private static final String DRYER_OPERATIONAL_STATE = "/operational/state/vs/0";
    private static final String DRYER_COURSE = "/st/dryercourse/vs/0";
    private static final String DRYER_SETTINGS = "/washer/vs/0";
    private static final String WATER = "/water/consumption/vs/0";
    private static final String KIDS_LOCK = "/kidslock/vs/0";
    private static final String SETINFO = "/wm/setinfo/vs/0";
    private static final String DRYER_MODE_FIELD = VENDOR + "st.dryerMode";
    private static final String DRY_LEVEL_FIELD = VENDOR + "dryLevel";
    private static final String SUPPORTED_DRY_LEVEL_FIELD = VENDOR + "supportedDryLevel";
    private static final String WRINKLE_PREVENT_FIELD = VENDOR + "wrinklePrevent";
    private static final String DELAY_END_FIELD = VENDOR + "delayEndTime";
    private static final String CYCLE_STATE_FIELD = VENDOR + "state";
    private static final BigDecimal MAX_DELAY_END_SECONDS = BigDecimal.valueOf(24 * 60 * 60);
    private static final BigDecimal DELAY_END_STEP_SECONDS = BigDecimal.valueOf(30 * 60);
    private static final Map<String, String> DRYER_COURSES = Map.ofEntries(Map.entry("16", "Cotton"),
            Map.entry("18", "Synthetics"), Map.entry("19", "Delicates"), Map.entry("1A", "Wool"),
            Map.entry("1B", "Bedding"), Map.entry("1C", "Shirts"), Map.entry("1D", "Towels"),
            Map.entry("1E", "Outdoor"), Map.entry("1F", "Mixed Load"), Map.entry("20", "Iron Dry"),
            Map.entry("23", "Quick Dry 35"), Map.entry("24", "Cool Air"), Map.entry("25", "Warm Air"),
            Map.entry("27", "Time Dry"));
    private static final List<ResourceHandler> RESOURCE_HANDLERS = List.of(
            new ResourceHandler(POWER, ResourceKind.POWER, Set.of("power")),
            new ResourceHandler(POWER_VS, ResourceKind.VENDOR_POWER, Set.of("power")),
            new ResourceHandler(REMOTE, ResourceKind.REMOTE, Set.of("remote-control")),
            new ResourceHandler(REMOTE_VS, ResourceKind.VENDOR_REMOTE, Set.of("remote-control")),
            new ResourceHandler(CURRENT, ResourceKind.TEMPERATURE, Set.of("current-temperature")),
            new ResourceHandler(DESIRED, ResourceKind.TEMPERATURE, Set.of("target-temperature")),
            new ResourceHandler(TEMPERATURES_VS, ResourceKind.VENDOR_TEMPERATURES,
                    Set.of("current-temperature", "target-temperature")),
            new ResourceHandler(ENERGY, ResourceKind.ENERGY, Set.of("power-consumption", "cumulative-energy")),
            new ResourceHandler(OPERATIONAL_STATE, ResourceKind.OPERATIONAL_STATE,
                    Set.of("machine-state", "job-state", "remaining-time", "progress")),
            new ResourceHandler(DRYER_OPERATIONAL_STATE, ResourceKind.DRYER_OPERATIONAL_STATE,
                    Set.of("cycle-control", "delay-end-time", "remaining-time", "progress")),
            new ResourceHandler(DRYER_COURSE, ResourceKind.DRYER_COURSE, Set.of("dryer-mode")),
            new ResourceHandler(DRYER_SETTINGS, ResourceKind.DRYER_SETTINGS, Set.of("dry-level", "wrinkle-prevent")),
            new ResourceHandler(WATER, ResourceKind.WATER, Set.of("water-consumption")),
            new ResourceHandler(KIDS_LOCK, ResourceKind.KIDS_LOCK, Set.of("kidslock")),
            new ResourceHandler(AIRFLOW, ResourceKind.AIRFLOW, Set.of("airflow")));
    private static final List<ModeResource> MODE_RESOURCES = List.of(
            new ModeResource(MODE, "mode", "Operating Mode",
                    Set.of("oic.d.airconditioner", "oic.d.airpurifier", "oic.d.dehumidifier")),
            new ModeResource(WIND_STRENGTH, "wind-strength", "Fan Speed", Set.of("oic.d.airconditioner")),
            new ModeResource(WIND_DIRECTION, "wind-direction", "Swing Mode", Set.of("oic.d.airconditioner")),
            new ModeResource(CONVENIENT, "mode-convenient", "Comfort Mode", Set.of("oic.d.airconditioner")));
    private final Logger logger = LoggerFactory.getLogger(Resources.class);
    private final Map<String, JsonObject> resources = new LinkedHashMap<>();

    /** A channel backed by one field, or the complete JSON representation when field is empty. */
    public record Point(String id, String href, String field, String itemType, String label, String description,
            boolean writable, boolean advanced) {
        public Point(String id, String href, String field, String itemType, String label, String description,
                boolean writable) {
            this(id, href, field, itemType, label, description, writable, false);
        }
    }

    private record Bounds(BigDecimal minimum, BigDecimal maximum, BigDecimal increment, Unit<Temperature> unit) {
    }

    private enum ResourceKind {
        POWER,
        VENDOR_POWER,
        REMOTE,
        VENDOR_REMOTE,
        TEMPERATURE,
        VENDOR_TEMPERATURES,
        ENERGY,
        OPERATIONAL_STATE,
        DRYER_OPERATIONAL_STATE,
        DRYER_COURSE,
        DRYER_SETTINGS,
        WATER,
        KIDS_LOCK,
        AIRFLOW
    }

    private record ResourceHandler(String suffix, ResourceKind kind, Set<String> channelIds) {
    }

    private record ModeResource(String suffix, String channelId, String label, Set<String> writableDeviceTypes) {
    }

    public Resources() {
    }

    /**
     * Applies a single representation, collection links, or a complete OCF batch envelope.
     * Missing fields in partial representations retain their previous values; explicit nulls invalidate them.
     *
     * @throws IOException if the envelope or a resource path is malformed; the cache is not modified on failure
     */
    public synchronized void update(String href, JsonElement payload) throws IOException {
        validateHref(href);
        Map<String, JsonObject> updates = new LinkedHashMap<>();
        if (payload.isJsonArray()) {
            for (JsonElement entry : payload.getAsJsonArray()) {
                if (!entry.isJsonObject()) {
                    throw new IOException("An OCF batch entry must be an object");
                }
                JsonObject object = entry.getAsJsonObject();
                if (object.has("href")) {
                    readLink(object, updates);
                } else if (object.isEmpty() || object.has("links") || object.has("rt") || object.has("if")) {
                    readLinks(object, updates);
                } else {
                    throw new IOException("An OCF batch entry has no resource path");
                }
            }
        } else if (payload.isJsonObject()) {
            JsonObject object = payload.getAsJsonObject();
            if (object.has("rep") && object.has("href")) {
                readLink(object, updates);
            } else {
                stage(href, object, updates);
                readLinks(object, updates);
            }
        } else {
            throw new IOException("An OCF representation must be an object or batch array");
        }
        updates.forEach((path, representation) -> merge(path, representation, resources));
    }

    /** Returns independent copies, including discovered but not yet populated href-only stubs. */
    public synchronized Map<String, JsonObject> snapshot() {
        Map<String, JsonObject> copy = new LinkedHashMap<>();
        resources.forEach((href, representation) -> copy.put(href, representation.deepCopy()));
        return copy;
    }

    /** Returns current channels; capability flags can change their writability between updates. */
    public synchronized List<Point> points() {
        return points(false);
    }

    synchronized List<Point> points(boolean logIgnoredChannels) {
        return points(null, logIgnoredChannels);
    }

    /** Returns only channels declared by a concrete appliance Thing. */
    synchronized List<Point> points(Set<String> channelIds) {
        return points(channelIds, false);
    }

    private List<Point> points(@Nullable Set<String> channelIds, boolean logIgnoredChannels) {
        List<Point> result = new ArrayList<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String href = entry.getKey();
            JsonObject rep = entry.getValue();
            // Concrete appliance types only inspect resources capable of backing an XML-declared channel.
            if (!mayContainChannel(href, channelIds)) {
                return;
            }
            // OCF security resources must never be exposed through channels.
            if (isStub(rep) || href.contains("/sec/")) {
                return;
            }
            ResourceHandler handler = resourceHandler(href);
            if (handler != null) {
                switch (handler.kind()) {
                    case POWER -> addPowerPoint(result, channelIds, href, rep);
                    case VENDOR_POWER -> addVendorPowerPoint(result, channelIds, href, rep, logIgnoredChannels);
                    case REMOTE -> addRemotePoint(result, channelIds, href, rep);
                    case VENDOR_REMOTE -> addVendorRemotePoint(result, channelIds, href, rep, logIgnoredChannels);
                    case TEMPERATURE -> addTemperaturePoint(result, channelIds, href, rep);
                    case VENDOR_TEMPERATURES ->
                        addVendorTemperaturePoints(result, channelIds, href, rep, logIgnoredChannels);
                    case ENERGY -> addEnergyPoints(result, channelIds, href, rep);
                    case OPERATIONAL_STATE -> addOperationalStatePoints(result, channelIds, href, rep);
                    case DRYER_OPERATIONAL_STATE -> addDryerOperationalStatePoints(result, channelIds, href, rep);
                    case DRYER_COURSE -> addDryerCoursePoint(result, channelIds, href, rep);
                    case DRYER_SETTINGS -> addDryerSettingPoints(result, channelIds, href, rep);
                    case WATER -> addWaterPoint(result, channelIds, href, rep);
                    case KIDS_LOCK -> addKidsLockPoint(result, channelIds, href, rep);
                    case AIRFLOW -> addAirflowPoint(result, channelIds, href, rep);
                }
            } else {
                ModeResource mode = modeResource(href);
                if (mode != null && rep.has(VENDOR + "modes")) {
                    String base = prefix(href, mode.suffix());
                    String currentMode = currentMode(rep);
                    boolean writable = mode.writableDeviceTypes().stream().anyMatch(type -> deviceType(base, type))
                            && currentMode != null && supportedModes(rep).contains(currentMode);
                    add(result, channelIds, href, VENDOR + "modes", "String", mode.label(), writable);
                }
            }
        });

        Map<String, List<Point>> pointsById = result.stream()
                .collect(Collectors.groupingBy(Point::id, LinkedHashMap::new, Collectors.toList()));
        List<Point> uniquePoints = new ArrayList<>();
        for (List<Point> points : pointsById.values()) {
            boolean omitRaw = points.size() > 1 && points.stream().anyMatch(point -> !point.label().endsWith("(Raw)"));
            List<Point> retained;
            if (omitRaw) {
                for (Point point : points) {
                    if (point.label().endsWith("(Raw)")) {
                        logIgnoredChannel(logIgnoredChannels, point.href(), point.field(), point.label(),
                                "a non-raw channel uses the same channel identifier");
                    }
                }
                retained = points.stream().filter(point -> !point.label().endsWith("(Raw)")).toList();
            } else {
                retained = points;
            }
            for (Point point : retained) {
                uniquePoints.add(retained.size() == 1 ? point
                        : new Point(fallbackId(point.href(), point.field()), point.href(), point.field(),
                                point.itemType(), point.label(), point.description(), point.writable(),
                                point.advanced()));
            }
        }
        Set<String> usedIds = new HashSet<>();
        return uniquePoints.stream().map(point -> {
            String id = point.id();
            for (int suffix = 2; !usedIds.add(id); suffix++) {
                id = point.id() + "-" + suffix;
            }
            return id.equals(point.id()) ? point
                    : new Point(id, point.href(), point.field(), point.itemType(), point.label(), point.description(),
                            point.writable(), point.advanced());
        }).toList();
    }

    private void addPowerPoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has("value")) {
            add(points, channelIds, href, "value", "Switch", "Power",
                    booleanValue(rep.get("value")) != null && powerWritable(prefix(href, POWER)));
        }
    }

    private void addVendorPowerPoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep,
            boolean logIgnoredChannels) {
        if (!rep.has(VENDOR + "power")) {
            return;
        }
        String base = prefix(href, POWER_VS);
        if (!standardBooleanUsable(base, POWER)) {
            add(points, channelIds, href, VENDOR + "power", "Switch", "Power",
                    onOff(rep.get(VENDOR + "power")) != null && powerWritable(base));
        } else {
            logIgnoredChannel(logIgnoredChannels, href, VENDOR + "power", "Power",
                    "a usable standard power resource is available");
        }
    }

    private void addRemotePoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has("value")) {
            add(points, channelIds, href, "value", "Switch", "Remote Control", false, true);
        }
    }

    private void addVendorRemotePoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep,
            boolean logIgnoredChannels) {
        if (!rep.has(VENDOR + "remoteControlEnabled")) {
            return;
        }
        if (!standardBooleanUsable(prefix(href, REMOTE_VS), REMOTE)) {
            add(points, channelIds, href, VENDOR + "remoteControlEnabled", "Switch", "Remote Control", false, true);
        } else {
            logIgnoredChannel(logIgnoredChannels, href, VENDOR + "remoteControlEnabled", "Remote Control",
                    "a usable standard remote-control resource is available");
        }
    }

    private void addTemperaturePoint(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep) {
        if (!rep.has("temperature")) {
            return;
        }
        boolean desired = href.endsWith(DESIRED);
        if (standardTemperatureUsable(href) || !hasVendorTemperatureFallback(href, desired)) {
            add(points, channelIds, href, "temperature", "Number:Temperature",
                    desired ? "Target Temperature" : "Current Temperature", desired && bounds(href, rep) != null);
        }
    }

    private void addVendorTemperaturePoints(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep, boolean logIgnoredChannels) {
        String base = prefix(href, TEMPERATURES_VS);
        if (!deviceType(base, "oic.d.airconditioner")) {
            return;
        }
        JsonObject item = temperatureItem(rep);
        if (item == null) {
            return;
        }
        addVendorTemperaturePoint(points, channelIds, href, item, base, false, logIgnoredChannels);
        addVendorTemperaturePoint(points, channelIds, href, item, base, true, logIgnoredChannels);
    }

    private void addVendorTemperaturePoint(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject item, String base, boolean desired, boolean logIgnoredChannels) {
        String field = VENDOR + (desired ? "desired" : "current");
        if (!item.has(field)) {
            return;
        }
        String standardResource = base + (desired ? DESIRED : CURRENT);
        if (standardTemperatureUsable(standardResource)) {
            logIgnoredChannel(logIgnoredChannels, href, field, desired ? "Desired Temperature" : "Temperature",
                    "a usable standard " + (desired ? "desired" : "current") + "-temperature resource is available");
            return;
        }
        String label = desired ? "Target Temperature" : "Current Temperature";
        add(points, channelIds, href, field, "Number:Temperature", label, desired && vendorBounds(item) != null);
    }

    private void addEnergyPoints(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has(VENDOR + "instantaneousPower")) {
            String field = VENDOR + "instantaneousPower";
            String label = "Power Consumption";
            add(points, channelIds, new Point("power-consumption", href, field, "Number:Power", label,
                    description(href, field, label), false));
        }
        if (rep.has(VENDOR + "cumulativePower")) {
            String field = VENDOR + "cumulativePower";
            String label = "Cumulative Energy";
            add(points, channelIds, new Point("cumulative-energy", href, field, "Number:Energy", label,
                    description(href, field, label), false));
        }
    }

    private void addOperationalStatePoints(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep) {
        for (String field : List.of("currentMachineState", "currentJobState")) {
            if (rep.has(field)) {
                add(points, channelIds, href, field, "String",
                        field.equals("currentMachineState") ? "Machine State" : "Job State", false);
            }
        }
        if (rep.has("remainingTime")) {
            add(points, channelIds, href, "remainingTime", "Number:Time", "Remaining Time", false);
        }
        if (rep.has("progressPercentage")) {
            add(points, channelIds, href, "progressPercentage", "Number:Dimensionless", "Progress", false);
        }
    }

    private void addDryerOperationalStatePoints(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep) {
        if (rep.has(CYCLE_STATE_FIELD)) {
            add(points, channelIds,
                    new Point("cycle-control", href, CYCLE_STATE_FIELD, "String", "Cycle Control",
                            "Set the dryer cycle state to Run, Pause, or Ready.",
                            List.of("Run", "Pause", "Ready").contains(string(rep.get(CYCLE_STATE_FIELD)))
                                    && remoteEnabled(prefix(href, DRYER_OPERATIONAL_STATE))));
        }
        if (rep.has(DELAY_END_FIELD)) {
            add(points, channelIds,
                    new Point("delay-end-time", href, DELAY_END_FIELD, "Number:Time", "Delay End Time",
                            "Delay-end time from 0 to 24 hours in 30-minute increments.",
                            validDelayEnd(durationSeconds(string(rep.get(DELAY_END_FIELD))))
                                    && remoteEnabled(prefix(href, DRYER_OPERATIONAL_STATE)),
                            true));
        }
        if (rep.has(VENDOR + "remainingTime")) {
            add(points, channelIds, new Point("remaining-time", href, VENDOR + "remainingTime", "Number:Time",
                    "Remaining Time", "Remaining cycle duration in seconds.", false));
        }
        if (rep.has(VENDOR + "progressPercentage")) {
            add(points, channelIds, new Point("progress", href, VENDOR + "progressPercentage", "Number:Dimensionless",
                    "Progress", "Cycle progress from 0 to 100 percent.", false));
        }
    }

    private void addDryerCoursePoint(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep) {
        String encoded = string(rep.get(DRYER_MODE_FIELD));
        String course = decodeDryerCourse(encoded);
        if (course != null) {
            add(points, channelIds, new Point("dryer-mode", href, DRYER_MODE_FIELD, "String", "Dryer Mode",
                    "Current dryer course.", false));
        }
    }

    private void addDryerSettingPoints(List<Point> points, @Nullable Set<String> channelIds, String href,
            JsonObject rep) {
        if (rep.has(DRY_LEVEL_FIELD)) {
            List<String> supported = supportedDryLevels(rep);
            add(points, channelIds,
                    new Point("dry-level", href, DRY_LEVEL_FIELD, "String", "Dry Level",
                            "Dry level setting. Commands must be one of the live supportedDryLevel values.",
                            supported.contains(string(rep.get(DRY_LEVEL_FIELD))) && !supported.isEmpty()
                                    && dryerSettingWritable(href)));
        }
        if (rep.has(WRINKLE_PREVENT_FIELD)) {
            String value = string(rep.get(WRINKLE_PREVENT_FIELD));
            add(points, channelIds,
                    new Point("wrinkle-prevent", href, WRINKLE_PREVENT_FIELD, "String", "Wrinkle Prevention",
                            "Enable or disable wrinkle prevention.",
                            List.of("On", "Off").contains(value) && dryerSettingWritable(href), true));
        }
    }

    private void addWaterPoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has(VENDOR + "cumulativeWater")) {
            add(points, channelIds, href, VENDOR + "cumulativeWater", "Number", "Cumulative Water (Raw)", false, true);
        }
    }

    private void addKidsLockPoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has(VENDOR + "kidsLock")) {
            add(points, channelIds, href, VENDOR + "kidsLock", "String", "Child Lock", false, true);
        }
    }

    private void addAirflowPoint(List<Point> points, @Nullable Set<String> channelIds, String href, JsonObject rep) {
        if (rep.has("speed")) {
            add(points, channelIds, href, "speed", "Number", "Fan Speed",
                    deviceType(prefix(href, AIRFLOW), "oic.d.airpurifier") && validSpeed(number(rep.get("speed"))));
        }
    }

    /** Describes the same choices and limits used to validate commands, without guessing device capabilities. */
    synchronized StateDescriptionFragment stateDescription(Point point) {
        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create()
                .withReadOnly(!point.writable()).withOptions(stateOptions(point));
        JsonObject rep = resources.get(point.href());
        if (rep != null && "Number:Temperature".equals(point.itemType())) {
            JsonObject item = point.href().endsWith(TEMPERATURES_VS) ? temperatureItem(rep) : null;
            Bounds limits = point.href().endsWith(DESIRED) ? bounds(point.href(), rep)
                    : item != null && point.field().equals(VENDOR + "desired") ? vendorBounds(item) : null;
            if (limits != null) {
                builder.withMinimum(limits.minimum()).withMaximum(limits.maximum()).withStep(limits.increment());
            }
        } else if (point.href().endsWith(AIRFLOW) && "speed".equals(point.field())
                && deviceType(prefix(point.href(), AIRFLOW), "oic.d.airpurifier")) {
            builder.withMinimum(BigDecimal.ZERO).withMaximum(BigDecimal.valueOf(4)).withStep(BigDecimal.ONE);
        } else if (point.href().endsWith(DRYER_OPERATIONAL_STATE) && DELAY_END_FIELD.equals(point.field())) {
            builder.withMinimum(BigDecimal.ZERO).withMaximum(MAX_DELAY_END_SECONDS).withStep(DELAY_END_STEP_SECONDS);
        }
        return builder.build();
    }

    synchronized List<CommandOption> commandOptions(Point point) {
        return point.writable()
                ? stateOptions(point).stream().map(option -> new CommandOption(option.getValue(), option.getLabel()))
                        .toList()
                : List.of();
    }

    private List<StateOption> stateOptions(Point point) {
        if ("Switch".equals(point.itemType())) {
            return List.of(new StateOption("ON", "On"), new StateOption("OFF", "Off"));
        }
        if (point.href().endsWith(AIRFLOW) && "speed".equals(point.field())
                && deviceType(prefix(point.href(), AIRFLOW), "oic.d.airpurifier")) {
            return List.of("0", "1", "2", "3", "4").stream().map(value -> new StateOption(value, value)).toList();
        }
        if (point.href().endsWith(DRYER_OPERATIONAL_STATE) && CYCLE_STATE_FIELD.equals(point.field())) {
            return List.of("Run", "Pause", "Ready").stream().map(value -> new StateOption(value, value)).toList();
        }
        if (point.href().endsWith(DRYER_SETTINGS) && DRY_LEVEL_FIELD.equals(point.field())) {
            JsonObject rep = resources.get(point.href());
            return rep == null ? List.of()
                    : supportedDryLevels(rep).stream().map(value -> new StateOption(value, value)).toList();
        }
        if (point.href().endsWith(DRYER_SETTINGS) && WRINKLE_PREVENT_FIELD.equals(point.field())) {
            return List.of("On", "Off").stream().map(value -> new StateOption(value, value)).toList();
        }
        JsonObject rep = resources.get(point.href());
        return rep != null && modeResource(point.href()) != null && point.field().equals(VENDOR + "modes")
                ? supportedModes(rep).stream().distinct().map(value -> new StateOption(value, value)).toList()
                : List.of();
    }

    private static boolean sameIdentity(Point first, Point second) {
        return first.id().equals(second.id()) && first.href().equals(second.href())
                && first.field().equals(second.field()) && first.itemType().equals(second.itemType());
    }

    /** Returns UNDEF for malformed or unknown values, without coercing them into valid appliance states. */
    public synchronized State state(Point point) {
        JsonObject rep = resources.get(point.href());
        if (rep == null || isStub(rep) || points().stream().noneMatch(p -> sameIdentity(p, point))) {
            return UnDefType.UNDEF;
        }
        JsonObject temperatureRep = point.href().endsWith(TEMPERATURES_VS) ? temperatureItem(rep) : rep;
        JsonElement value = temperatureRep == null ? null : temperatureRep.get(point.field());
        if ("Switch".equals(point.itemType())) {
            Boolean enabled = point.href().endsWith(POWER_VS) ? onOff(value)
                    : point.href().endsWith(REMOTE_VS) ? vendorBoolean(value) : booleanValue(value);
            return enabled == null ? UnDefType.UNDEF : OnOffType.from(enabled);
        }
        if ("Number:Temperature".equals(point.itemType())) {
            BigDecimal temperature = number(value);
            Unit<Temperature> unit = temperatureRep == null ? null
                    : temperatureUnit(
                            temperatureRep.get(point.href().endsWith(TEMPERATURES_VS) ? VENDOR + "unit" : "units"));
            return temperature == null || unit == null ? UnDefType.UNDEF : new QuantityType<>(temperature, unit);
        }
        if ("Number:Power".equals(point.itemType())) {
            BigDecimal watts = number(value);
            return watts == null ? UnDefType.UNDEF : new QuantityType<>(watts, Units.WATT);
        }
        if ("Number:Energy".equals(point.itemType())) {
            BigDecimal energy = number(value);
            Unit<Energy> unit = energyUnit(rep.get(VENDOR + "cumulativeUnit"));
            return energy == null || unit == null ? UnDefType.UNDEF : new QuantityType<>(energy, unit);
        }
        if ("Number:Time".equals(point.itemType())) {
            BigDecimal seconds = durationSeconds(string(value));
            return seconds == null ? UnDefType.UNDEF : new QuantityType<>(seconds, Units.SECOND);
        }
        if ("Number:Dimensionless".equals(point.itemType())) {
            BigDecimal percent = number(value);
            return percent == null || percent.signum() < 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0
                    ? UnDefType.UNDEF
                    : new QuantityType<>(percent, Units.PERCENT);
        }
        if ("Number".equals(point.itemType())) {
            BigDecimal numeric = number(value);
            return numeric == null ? UnDefType.UNDEF : new DecimalType(numeric);
        }
        String text = point.href().endsWith(DRYER_COURSE) && DRYER_MODE_FIELD.equals(point.field())
                ? decodeDryerCourse(string(value))
                : modeResource(point.href()) != null ? currentMode(rep) : string(value);
        return text == null ? UnDefType.UNDEF : new StringType(text);
    }

    /**
     * Builds a minimal POST representation, revalidating live capabilities and Remote Control.
     * This method does not optimistically change the cached device state.
     *
     * @throws IllegalArgumentException if the channel, command, capability, or remote-control state does not allow
     *             writing
     */
    public synchronized JsonObject command(Point point, Command command) throws IllegalArgumentException {
        Point current = points().stream().filter(p -> sameIdentity(p, point)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown or changed resource channel"));
        if (!current.writable()) {
            throw new IllegalArgumentException("Resource channel is read-only");
        }
        String href = current.href();
        if (href.endsWith(DRYER_OPERATIONAL_STATE) || href.endsWith(DRYER_SETTINGS)) {
            return dryerCommand(current, command);
        }
        ModeResource mode = modeResource(href);
        String modesSuffix = mode == null ? null : mode.suffix();
        String suffix = href.endsWith(POWER) ? POWER
                : href.endsWith(POWER_VS) ? POWER_VS
                        : href.endsWith(DESIRED) ? DESIRED
                                : href.endsWith(TEMPERATURES_VS) ? TEMPERATURES_VS
                                        : href.endsWith(AIRFLOW) ? AIRFLOW : modesSuffix == null ? MODE : modesSuffix;
        if (!remoteEnabled(prefix(href, suffix))) {
            throw new IllegalArgumentException("Remote Control is not explicitly enabled");
        }
        JsonObject result = new JsonObject();
        if ((POWER.equals(suffix) || POWER_VS.equals(suffix)) && command instanceof OnOffType onOff) {
            if (POWER.equals(suffix)) {
                result.addProperty("value", onOff == OnOffType.ON);
            } else {
                result.addProperty(VENDOR + "power", onOff == OnOffType.ON ? "On" : "Off");
            }
        } else if (DESIRED.equals(suffix) || TEMPERATURES_VS.equals(suffix)) {
            JsonObject rep = resources.get(href);
            JsonObject item = rep == null || !TEMPERATURES_VS.equals(suffix) ? null : temperatureItem(rep);
            Bounds limits = rep == null ? null : item == null ? bounds(href, rep) : vendorBounds(item);
            if (limits == null) {
                throw new IllegalArgumentException("No valid device temperature bounds and increment");
            }
            BigDecimal value;
            if (command instanceof QuantityType<?> quantity) {
                QuantityType<?> converted = quantity.toUnit(limits.unit());
                if (converted == null) {
                    throw new IllegalArgumentException("Command is not a compatible temperature");
                }
                value = converted.toBigDecimal();
                if (!quantity.getUnit().equals(limits.unit())) {
                    BigDecimal steps = value.subtract(limits.minimum()).divide(limits.increment(), 0,
                            RoundingMode.HALF_UP);
                    BigDecimal aligned = limits.minimum().add(steps.multiply(limits.increment()));
                    // Unit converters can leave floating-point residue at an otherwise exact advertised step.
                    if (value.subtract(aligned).abs()
                            .compareTo(limits.increment().multiply(new BigDecimal("0.000000001"))) <= 0) {
                        value = aligned;
                    }
                }
            } else if (command instanceof DecimalType decimal) {
                value = decimal.toBigDecimal();
            } else {
                throw new IllegalArgumentException("Expected a temperature command");
            }
            if (value.compareTo(limits.minimum()) < 0 || value.compareTo(limits.maximum()) > 0
                    || value.subtract(limits.minimum()).remainder(limits.increment()).signum() != 0) {
                throw new IllegalArgumentException("Temperature is outside the advertised range or increment");
            }
            if (item == null) {
                result.addProperty("temperature", value);
            } else {
                JsonObject update = new JsonObject();
                update.add(VENDOR + "id", item.get(VENDOR + "id").deepCopy());
                update.addProperty(VENDOR + "desired", value.toPlainString());
                JsonArray items = new JsonArray();
                items.add(update);
                result.add(VENDOR + "items", items);
            }
        } else if (AIRFLOW.equals(suffix) && command instanceof DecimalType decimal
                && !(command instanceof QuantityType<?>) && validSpeed(decimal.toBigDecimal())) {
            result.addProperty("speed", decimal.intValue());
        } else if (modesSuffix != null && command instanceof StringType text) {
            JsonObject rep = resources.get(href);
            if (rep == null || !supportedModes(rep).contains(text.toString())) {
                throw new IllegalArgumentException("Mode is not advertised by this appliance");
            }
            if (rep.get(VENDOR + "modes").isJsonArray()) {
                JsonArray modes = new JsonArray();
                modes.add(text.toString());
                result.add(VENDOR + "modes", modes);
            } else {
                result.addProperty(VENDOR + "modes", text.toString());
            }
        } else {
            throw new IllegalArgumentException("Command is not supported by this resource");
        }
        return result;
    }

    private JsonObject dryerCommand(Point point, Command command) {
        JsonObject result = new JsonObject();
        String href = point.href();
        if (href.endsWith(DRYER_OPERATIONAL_STATE)) {
            if (!remoteEnabled(prefix(href, DRYER_OPERATIONAL_STATE))) {
                throw new IllegalArgumentException("Remote Control is not explicitly enabled");
            }
            if (CYCLE_STATE_FIELD.equals(point.field()) && command instanceof StringType text
                    && List.of("Run", "Pause", "Ready").contains(text.toString())) {
                result.addProperty(CYCLE_STATE_FIELD, text.toString());
            } else if (DELAY_END_FIELD.equals(point.field())) {
                QuantityType<?> converted = command instanceof QuantityType<?> quantity ? quantity.toUnit(Units.SECOND)
                        : null;
                BigDecimal seconds = converted != null ? converted.toBigDecimal()
                        : command instanceof DecimalType decimal ? decimal.toBigDecimal() : null;
                if (seconds == null || !validDelayEnd(seconds)) {
                    throw new IllegalArgumentException("Delay end must be 0 to 24 hours in 30-minute increments");
                }
                long totalMinutes = seconds.longValueExact() / 60;
                long hours = totalMinutes / 60;
                long minutes = totalMinutes % 60;
                result.addProperty(DELAY_END_FIELD, String.format("%02d:%02d:00", hours, minutes));
            } else {
                throw new IllegalArgumentException("Command is not supported by this dryer resource");
            }
            return result;
        }
        if (!dryerSettingWritable(href)) {
            throw new IllegalArgumentException("Dryer setting is not writable without Remote Control");
        }
        if (DRY_LEVEL_FIELD.equals(point.field()) && command instanceof StringType text) {
            JsonObject rep = resources.get(href);
            if (rep == null || !supportedDryLevels(rep).contains(text.toString())) {
                throw new IllegalArgumentException("Dry level is not advertised as supported by this dryer");
            }
            result.addProperty(DRY_LEVEL_FIELD, text.toString());
        } else if (WRINKLE_PREVENT_FIELD.equals(point.field()) && command instanceof StringType text
                && List.of("On", "Off").contains(text.toString())) {
            result.addProperty(WRINKLE_PREVENT_FIELD, text.toString());
        } else {
            throw new IllegalArgumentException("Command is not supported by this dryer setting");
        }
        return result;
    }

    /** Explicit remote-control gates fail closed; air conditioners need not advertise this optional capability. */
    public synchronized boolean remoteControlEnabled() {
        if (resources.containsKey(REMOTE) || resources.containsKey(REMOTE_VS)) {
            return remoteEnabled("");
        }
        List<String> prefixes = resources.keySet().stream().filter(h -> h.endsWith(REMOTE) || h.endsWith(REMOTE_VS))
                .map(h -> prefix(h, h.endsWith(REMOTE) ? REMOTE : REMOTE_VS)).distinct().toList();
        return prefixes.isEmpty() ? remoteEnabled("") : prefixes.stream().allMatch(this::remoteEnabled);
    }

    private static void validateHref(String href) throws IOException {
        if (!href.matches("/(?:[A-Za-z0-9._~-]+/)*[A-Za-z0-9._~-]+")) {
            throw new IOException("Invalid OCF resource path");
        }
        for (String segment : href.substring(1).split("/")) {
            if (".".equals(segment) || "..".equals(segment)) {
                throw new IOException("Invalid OCF resource path segment");
            }
        }
    }

    private static void readLinks(JsonObject object, Map<String, JsonObject> updates) throws IOException {
        JsonElement links = object.get("links");
        if (links == null) {
            return;
        }
        if (!links.isJsonArray()) {
            throw new IOException("OCF collection links must be an array");
        }
        for (JsonElement link : links.getAsJsonArray()) {
            if (!link.isJsonObject()) {
                throw new IOException("An OCF collection link must be an object");
            }
            readLink(link.getAsJsonObject(), updates);
        }
    }

    private static void readLink(JsonObject link, Map<String, JsonObject> updates) throws IOException {
        String href = string(link.get("href"));
        if (href == null) {
            throw new IOException("An OCF resource link must contain a string path");
        }
        validateHref(href);
        JsonElement representation = link.get("rep");
        if (representation == null) {
            JsonObject stub = new JsonObject();
            stub.addProperty("href", href);
            stage(href, stub, updates);
        } else if (representation.isJsonObject()) {
            JsonObject rep = representation.getAsJsonObject();
            stage(href, rep, updates);
            readLinks(rep, updates);
        } else {
            throw new IOException("An OCF linked representation must be an object");
        }
    }

    private static void stage(String href, JsonObject rep, Map<String, JsonObject> updates) throws IOException {
        if (rep.has("href")) {
            String repHref = string(rep.get("href"));
            if (repHref == null) {
                throw new IOException("An OCF representation path must be a string");
            }
            validateHref(repHref);
            if (!href.equals(repHref)) {
                throw new IOException("An OCF representation path does not match its resource");
            }
        }
        if (!href.contains("/sec/")) {
            merge(href, rep, updates);
        }
    }

    private static void merge(String href, JsonObject rep, Map<String, JsonObject> target) {
        JsonObject existing = target.get(href);
        if (existing == null || isStub(existing)) {
            target.put(href, rep.deepCopy());
        } else if (!isStub(rep)) {
            mergeObject(existing, rep);
        }
    }

    private static void mergeObject(JsonObject existing, JsonObject update) {
        update.entrySet().forEach(entry -> {
            JsonElement old = existing.get(entry.getKey());
            JsonElement value = entry.getValue();
            if (old != null && old.isJsonObject() && value.isJsonObject()) {
                mergeObject(old.getAsJsonObject(), value.getAsJsonObject());
            } else {
                existing.add(entry.getKey(), value.deepCopy());
            }
        });
    }

    private static boolean isStub(JsonObject rep) {
        return rep.size() == 1 && rep.has("href");
    }

    private static boolean mayContainChannel(String href, @Nullable Set<String> channelIds) {
        if (channelIds == null) {
            return true;
        }
        ResourceHandler handler = resourceHandler(href);
        if (handler != null) {
            return handler.channelIds().stream().anyMatch(channelIds::contains);
        }
        ModeResource mode = modeResource(href);
        return mode != null && channelIds.contains(mode.channelId());
    }

    private static @Nullable ResourceHandler resourceHandler(String href) {
        return RESOURCE_HANDLERS.stream().filter(handler -> href.endsWith(handler.suffix())).findFirst().orElse(null);
    }

    private static @Nullable ModeResource modeResource(String href) {
        return MODE_RESOURCES.stream().filter(mode -> href.endsWith(mode.suffix()))
                .max((first, second) -> Integer.compare(first.suffix().length(), second.suffix().length()))
                .orElse(null);
    }

    private void add(List<Point> points, @Nullable Set<String> channelIds, String href, String field, String itemType,
            String label, boolean writable) {
        add(points, channelIds, href, field, itemType, label, writable, false);
    }

    private void add(List<Point> points, @Nullable Set<String> channelIds, String href, String field, String itemType,
            String label, boolean writable, boolean advanced) {
        String resource = resourceId(href);
        String id = href.endsWith(TEMPERATURES_VS) ? combinedTemperatureId(href, field)
                : href.endsWith(OPERATIONAL_STATE) ? operationalStateId(field) : channelId(resource);
        add(points, channelIds,
                new Point(id, href, field, itemType, label, description(href, field, label), writable, advanced));
    }

    private static void add(List<Point> points, @Nullable Set<String> channelIds, Point point) {
        if (channelIds == null || channelIds.contains(point.id())) {
            points.add(point);
        }
    }

    private static String resourceId(String href) {
        return href.substring(1).replaceFirst("/vs/0$", "").replaceFirst("/0$", "");
    }

    private void logIgnoredChannel(boolean enabled, String href, String field, String label, String reason) {
        if (enabled) {
            logger.debug("Not adding potential channel '{}' from resource {} field {}: {}", label, href, field, reason);
        }
    }

    private static String combinedTemperatureId(String href, String field) {
        String temperatureResource = (VENDOR + "current").equals(field) ? CURRENT : DESIRED;
        return channelId(resourceId(prefix(href, TEMPERATURES_VS) + temperatureResource));
    }

    private static String operationalStateId(String field) {
        return switch (field) {
            case "currentMachineState" -> "machine-state";
            case "currentJobState" -> "job-state";
            case "remainingTime" -> "remaining-time";
            case "progressPercentage" -> "progress";
            default -> channelId(resourceId(OPERATIONAL_STATE) + "-" + field);
        };
    }

    private static String fallbackId(String href, String field) {
        String fieldName = field.substring(field.lastIndexOf('.') + 1);
        return readableId(resourceId(href) + "-" + fieldName);
    }

    private static String channelId(String resource) {
        String id = readableId(resource);
        if (id.endsWith("remotectrl")) {
            return id.substring(0, id.length() - "remotectrl".length()) + "remote-control";
        }
        for (String[] rename : List.of(new String[] { "temperature-current", "current-temperature" },
                new String[] { "temperature-desired", "target-temperature" })) {
            if (id.endsWith(rename[0])) {
                return id.substring(0, id.length() - rename[0].length()) + rename[1];
            }
        }
        return id;
    }

    private static String readableId(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]+", "-").replaceAll("-{2,}", "-").replaceAll("(^-|-$)", "");
    }

    private String description(String href, String field, String label) {
        JsonObject rep = resources.get(href);
        if (rep != null && modeResource(href) != null) {
            List<String> modes = supportedModes(rep);
            if (!modes.isEmpty()) {
                JsonElement names = rep.get(VENDOR + "modesName");
                if (names != null && names.isJsonArray() && names.getAsJsonArray().size() == modes.size()) {
                    List<String> displayNames = new ArrayList<>();
                    for (JsonElement name : names.getAsJsonArray()) {
                        String text = string(name);
                        if (text == null || text.isBlank()) {
                            displayNames.clear();
                            break;
                        }
                        displayNames.add(text);
                    }
                    if (displayNames.size() == modes.size()) {
                        List<String> options = new ArrayList<>();
                        for (int index = 0; index < modes.size(); index++) {
                            options.add(modes.get(index) + " (" + displayNames.get(index) + ")");
                        }
                        return label + " reported by the appliance. Supported command values (display names): "
                                + String.join(", ", options) + ".";
                    }
                }
                return label + " reported by the appliance. Supported command values: " + String.join(", ", modes)
                        + ".";
            }
            return label + " reported by the appliance. No supported command values were advertised.";
        }
        if (href.endsWith(AIRFLOW) && "speed".equals(field)) {
            return label
                    + " reported by the appliance. Valid commands for supported air purifiers are integers from 0 to 4.";
        }
        Bounds limits = null;
        if (rep != null && href.endsWith(DESIRED)) {
            limits = bounds(href, rep);
        } else if (rep != null && href.endsWith(TEMPERATURES_VS) && (VENDOR + "desired").equals(field)) {
            JsonObject item = temperatureItem(rep);
            limits = item == null ? null : vendorBounds(item);
        }
        if (limits != null) {
            return label + " setpoint from " + limits.minimum() + " to " + limits.maximum() + " "
                    + limits.unit().getSymbol() + " in increments of " + limits.increment() + ".";
        }
        return label + " reported by the appliance.";
    }

    private static String prefix(String href, String suffix) {
        return href.substring(0, href.length() - suffix.length());
    }

    private boolean standardBooleanUsable(String base, String suffix) {
        JsonObject rep = resources.get(base + suffix);
        return rep != null && booleanValue(rep.get("value")) != null;
    }

    private boolean remoteEnabled(String base) {
        JsonObject standard = resources.get(base + REMOTE);
        Boolean enabled = standard == null ? null : booleanValue(standard.get("value"));
        if (enabled != null) {
            return enabled;
        }
        JsonObject vendor = resources.get(base + REMOTE_VS);
        enabled = vendor == null ? null : vendorBoolean(vendor.get(VENDOR + "remoteControlEnabled"));
        if (enabled != null) {
            return enabled;
        }
        if (standard != null || vendor != null) {
            return false;
        }
        if (!base.isEmpty() && (resources.containsKey(REMOTE) || resources.containsKey(REMOTE_VS))) {
            return remoteEnabled("");
        }
        return deviceType(base, "oic.d.airconditioner");
    }

    private boolean dryerSettingWritable(String href) {
        String base = prefix(href, DRYER_SETTINGS);
        return remoteEnabled(base) || modelSettingWithoutSmartControl(base);
    }

    private boolean modelSettingWithoutSmartControl(String base) {
        for (String path : List.of(base + SETINFO, SETINFO)) {
            JsonObject rep = resources.get(path);
            if (rep != null) {
                JsonElement flag = rep.has(VENDOR + "isModelSettingWithoutSC")
                        ? rep.get(VENDOR + "isModelSettingWithoutSC")
                        : rep.get("isModelSettingWithoutSC");
                if (Boolean.TRUE.equals(vendorBoolean(flag)) || Boolean.TRUE.equals(booleanValue(flag))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean powerWritable(String base) {
        for (String path : List.of(base + SETINFO, SETINFO)) {
            JsonObject rep = resources.get(path);
            if (rep != null) {
                JsonElement flag = rep.has(VENDOR + "isModelSettingPowerOnOff")
                        ? rep.get(VENDOR + "isModelSettingPowerOnOff")
                        : rep.get("isModelSettingPowerOnOff");
                if (Boolean.FALSE.equals(vendorBoolean(flag)) || Boolean.FALSE.equals(booleanValue(flag))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean deviceType(String base, String type) {
        JsonObject identity = resources.get(base + "/oic/d");
        if (identity == null && !base.isEmpty()) {
            identity = resources.get("/oic/d");
        }
        if (identity == null) {
            return false;
        }
        JsonElement types = identity.get("rt");
        if (types != null && types.isJsonArray()) {
            for (JsonElement candidate : types.getAsJsonArray()) {
                if (type.equals(string(candidate))) {
                    return true;
                }
            }
        }
        return false;
    }

    private @Nullable Bounds bounds(String href, JsonObject rep) {
        Unit<Temperature> unit = temperatureUnit(rep.get("units"));
        JsonElement range = rep.get("range");
        if (unit == null || range == null || !range.isJsonArray() || range.getAsJsonArray().size() != 2
                || number(rep.get("temperature")) == null) {
            return null;
        }
        BigDecimal minimum = number(range.getAsJsonArray().get(0));
        BigDecimal maximum = number(range.getAsJsonArray().get(1));
        BigDecimal increment = number(rep.has("increment") ? rep.get("increment") : rep.get("step"));
        if (increment == null && !rep.has("increment") && !rep.has("step")) {
            String base = prefix(href, DESIRED);
            JsonObject control = resources.get(base + TEMPERATURE_CONTROL);
            if (control == null) {
                JsonObject vendor = resources.get(base + TEMPERATURES_VS);
                JsonObject item = vendor == null ? null : temperatureItem(vendor);
                if (item != null && unit.equals(temperatureUnit(item.get(VENDOR + "unit")))) {
                    increment = number(item.get(VENDOR + "increment"));
                }
            } else {
                Unit<Temperature> controlUnit = temperatureUnit(control.get("units"));
                if (control.has("units") && (controlUnit == null || !controlUnit.equals(unit))) {
                    return null;
                }
                increment = number(
                        control.has("increment") ? control.get("increment") : control.get(VENDOR + "increment"));
            }
        }
        if (minimum == null || maximum == null || increment == null || increment.signum() <= 0
                || minimum.compareTo(maximum) >= 0 || increment.compareTo(maximum.subtract(minimum)) > 0) {
            return null;
        }
        return new Bounds(minimum, maximum, increment, unit);
    }

    private boolean standardTemperatureUsable(String href) {
        JsonObject rep = resources.get(href);
        return rep != null && number(rep.get("temperature")) != null && temperatureUnit(rep.get("units")) != null;
    }

    private boolean hasVendorTemperatureFallback(String href, boolean desired) {
        JsonObject rep = resources.get(prefix(href, desired ? DESIRED : CURRENT) + TEMPERATURES_VS);
        JsonObject item = rep == null ? null : temperatureItem(rep);
        return item != null && item.has(VENDOR + (desired ? "desired" : "current"));
    }

    private static @Nullable JsonObject temperatureItem(JsonObject rep) {
        JsonElement items = rep.get(VENDOR + "items");
        if (items == null || !items.isJsonArray() || items.getAsJsonArray().size() != 1
                || !items.getAsJsonArray().get(0).isJsonObject()) {
            return null;
        }
        JsonObject item = items.getAsJsonArray().get(0).getAsJsonObject();
        return "0".equals(string(item.get(VENDOR + "id"))) ? item : null;
    }

    private static @Nullable Bounds vendorBounds(JsonObject item) {
        BigDecimal minimum = number(item.get(VENDOR + "minimum"));
        BigDecimal maximum = number(item.get(VENDOR + "maximum"));
        BigDecimal increment = number(item.get(VENDOR + "increment"));
        Unit<Temperature> unit = temperatureUnit(item.get(VENDOR + "unit"));
        if (minimum == null || maximum == null || increment == null || unit == null
                || number(item.get(VENDOR + "desired")) == null || increment.signum() <= 0
                || minimum.compareTo(maximum) >= 0 || increment.compareTo(maximum.subtract(minimum)) > 0) {
            return null;
        }
        return new Bounds(minimum, maximum, increment, unit);
    }

    private static boolean validSpeed(@Nullable BigDecimal value) {
        return value != null && value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(4)) <= 0
                && value.stripTrailingZeros().scale() <= 0;
    }

    private static @Nullable String currentMode(JsonObject rep) {
        JsonElement modes = rep.get(VENDOR + "modes");
        return modes != null && modes.isJsonArray()
                ? modes.getAsJsonArray().size() == 1 ? string(modes.getAsJsonArray().get(0)) : null
                : string(modes);
    }

    private static @Nullable String decodeDryerCourse(@Nullable String encoded) {
        if (encoded == null || !encoded.startsWith("Table_03_Course_")) {
            return encoded;
        }
        String code = encoded.substring("Table_03_Course_".length()).toUpperCase(java.util.Locale.ROOT);
        return DRYER_COURSES.getOrDefault(code, encoded);
    }

    private static List<String> supportedDryLevels(JsonObject rep) {
        JsonElement levels = rep.get(SUPPORTED_DRY_LEVEL_FIELD);
        if (levels == null || !levels.isJsonArray() || levels.getAsJsonArray().isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonElement level : levels.getAsJsonArray()) {
            String value = string(level);
            if (value == null || value.isBlank()) {
                return List.of();
            }
            result.add(value);
        }
        return result.stream().distinct().toList();
    }

    private static boolean validDelayEnd(@Nullable BigDecimal seconds) {
        return seconds != null && seconds.signum() >= 0 && seconds.compareTo(MAX_DELAY_END_SECONDS) <= 0
                && seconds.stripTrailingZeros().scale() <= 0 && seconds.remainder(DELAY_END_STEP_SECONDS).signum() == 0;
    }

    private static List<String> supportedModes(JsonObject rep) {
        List<String> result = new ArrayList<>();
        JsonElement modes = rep.get(VENDOR + "supportedModes");
        if (modes != null && modes.isJsonArray()) {
            for (JsonElement value : modes.getAsJsonArray()) {
                String mode = string(value);
                if (mode == null || mode.isBlank()) {
                    return List.of();
                }
                result.add(mode);
            }
        }
        return result;
    }

    private static @Nullable Unit<Energy> energyUnit(@Nullable JsonElement value) {
        String unit = string(value);
        if (unit == null) {
            return null;
        }
        return switch (unit) {
            case "Wh" -> Units.WATT_HOUR;
            case "kWh" -> Units.KILOWATT_HOUR;
            case "MWh" -> Units.MEGAWATT_HOUR;
            default -> null;
        };
    }

    private static @Nullable BigDecimal durationSeconds(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String[] parts = value.split(":", -1);
        if (parts.length != 3 || !parts[0].matches("[0-9]{2,9}") || !parts[1].matches("[0-5][0-9]")
                || !parts[2].matches("[0-5][0-9]")) {
            return null;
        }
        return new BigDecimal(parts[0]).multiply(BigDecimal.valueOf(3600))
                .add(new BigDecimal(parts[1]).multiply(BigDecimal.valueOf(60))).add(new BigDecimal(parts[2]));
    }

    private static @Nullable Unit<Temperature> temperatureUnit(@Nullable JsonElement value) {
        String unit = string(value);
        if (unit == null) {
            return null;
        }
        return switch (unit) {
            case "C", "°C", "Celsius", "celsius" -> SIUnits.CELSIUS;
            case "F", "°F", "Fahrenheit", "fahrenheit" -> ImperialUnits.FAHRENHEIT;
            default -> null;
        };
    }

    private static @Nullable Boolean booleanValue(@Nullable JsonElement value) {
        return value instanceof JsonPrimitive primitive && primitive.isBoolean() ? primitive.getAsBoolean() : null;
    }

    private static @Nullable Boolean vendorBoolean(@Nullable JsonElement value) {
        String text = string(value);
        return "true".equalsIgnoreCase(text) ? Boolean.TRUE : "false".equalsIgnoreCase(text) ? Boolean.FALSE : null;
    }

    private static @Nullable Boolean onOff(@Nullable JsonElement value) {
        String text = string(value);
        return "On".equals(text) ? Boolean.TRUE : "Off".equals(text) ? Boolean.FALSE : null;
    }

    private static @Nullable String string(@Nullable JsonElement value) {
        return value instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString() : null;
    }

    private static @Nullable BigDecimal number(@Nullable JsonElement value) {
        if (value instanceof JsonPrimitive primitive && (primitive.isNumber() || primitive.isString())) {
            try {
                String text = primitive.getAsString();
                if (text.length() > 256) {
                    return null;
                }
                BigDecimal numeric = new BigDecimal(text);
                // Bound exponent expansion before range and step arithmetic on externally supplied numbers.
                return numeric.precision() <= 100 && numeric.scale() >= -100 && numeric.scale() <= 100 ? numeric : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
