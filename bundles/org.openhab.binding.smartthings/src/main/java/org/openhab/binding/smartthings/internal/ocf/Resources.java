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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.measure.Unit;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Cached OCF representations and deliberately limited, verified Samsung appliance controls.
 * Unknown resources remain available as read-only JSON diagnostics.
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
    private static final String WATER = "/water/consumption/vs/0";
    private static final String KIDS_LOCK = "/kidslock/vs/0";
    private static final String SETINFO = "/wm/setinfo/vs/0";
    private final Map<String, JsonObject> resources = new LinkedHashMap<>();

    /** A channel backed by one field, or the complete JSON representation when field is empty. */
    public record Point(String id, String href, String field, String itemType, String label, boolean writable) {
    }

    private record Bounds(BigDecimal minimum, BigDecimal maximum, BigDecimal increment, Unit<Temperature> unit) {
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
        List<Point> result = new ArrayList<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String href = entry.getKey();
            JsonObject rep = entry.getValue();
            // OCF security resources must not expose credentials through diagnostic channels.
            if (isStub(rep) || href.contains("/sec/")) {
                return;
            }
            int count = result.size();
            String modesSuffix = modeSuffix(href);
            if (href.endsWith(POWER) && rep.has("value")) {
                add(result, href, "value", "Switch", "Power",
                        booleanValue(rep.get("value")) != null && powerWritable(prefix(href, POWER)));
            } else if (href.endsWith(POWER_VS) && rep.has(VENDOR + "power")
                    && !standardBooleanUsable(prefix(href, POWER_VS), POWER)) {
                add(result, href, VENDOR + "power", "Switch", "Power",
                        onOff(rep.get(VENDOR + "power")) != null && powerWritable(prefix(href, POWER_VS)));
            } else if (href.endsWith(REMOTE) && rep.has("value")) {
                add(result, href, "value", "Switch", "Remote Control", false);
            } else if (href.endsWith(REMOTE_VS) && rep.has(VENDOR + "remoteControlEnabled")
                    && !standardBooleanUsable(prefix(href, REMOTE_VS), REMOTE)) {
                add(result, href, VENDOR + "remoteControlEnabled", "Switch", "Remote Control", false);
            } else if ((href.endsWith(CURRENT) || href.endsWith(DESIRED)) && rep.has("temperature")) {
                boolean desired = href.endsWith(DESIRED);
                add(result, href, "temperature", "Number:Temperature", desired ? "Desired Temperature" : "Temperature",
                        desired && bounds(href, rep) != null);
            } else if (href.endsWith(ENERGY)) {
                if (rep.has(VENDOR + "instantaneousPower")) {
                    add(result, href, VENDOR + "instantaneousPower", "Number:Power", "Power Consumption", false);
                }
                if (rep.has(VENDOR + "cumulativePower")) {
                    add(result, href, VENDOR + "cumulativePower", "Number", "Cumulative Power (Raw)", false);
                }
            } else if (href.endsWith(WATER) && rep.has(VENDOR + "cumulativeWater")) {
                add(result, href, VENDOR + "cumulativeWater", "Number", "Cumulative Water (Raw)", false);
            } else if (href.endsWith(KIDS_LOCK) && rep.has(VENDOR + "kidsLock")) {
                add(result, href, VENDOR + "kidsLock", "String", "Child Lock", false);
            } else if (href.endsWith(AIRFLOW) && rep.has("speed")) {
                add(result, href, "speed", "Number", "Fan Speed",
                        deviceType(prefix(href, AIRFLOW), "oic.d.airpurifier") && validSpeed(number(rep.get("speed"))));
            } else if (href.endsWith(TEMPERATURES_VS)
                    && deviceType(prefix(href, TEMPERATURES_VS), "oic.d.airconditioner")) {
                JsonObject item = temperatureItem(rep);
                if (item != null) {
                    String base = prefix(href, TEMPERATURES_VS);
                    if (item.has(VENDOR + "current") && !standardTemperatureUsable(base + CURRENT)) {
                        add(result, href, VENDOR + "current", "Number:Temperature", "Temperature", false);
                    }
                    if (item.has(VENDOR + "desired") && !standardTemperatureUsable(base + DESIRED)) {
                        add(result, href, VENDOR + "desired", "Number:Temperature", "Desired Temperature",
                                vendorBounds(item) != null);
                    }
                }
            } else if (modesSuffix != null && rep.has(VENDOR + "modes")) {
                String suffix = modesSuffix;
                String base = prefix(href, suffix);
                boolean airConditioner = deviceType(base, "oic.d.airconditioner");
                String label = href.endsWith(WIND_STRENGTH) ? "Fan Speed"
                        : href.endsWith(WIND_DIRECTION) ? "Swing Mode"
                                : href.endsWith(CONVENIENT) ? "Comfort Mode" : "Operating Mode";
                add(result, href, VENDOR + "modes", "String", label,
                        (airConditioner || MODE.equals(suffix)
                                && (deviceType(base, "oic.d.airpurifier") || deviceType(base, "oic.d.dehumidifier")))
                                && currentMode(rep) != null && supportedModes(rep).contains(currentMode(rep)));
            }
            if (result.size() == count) {
                add(result, href, "", "String", "Resource " + href, false);
            }
        });
        return List.copyOf(result);
    }

    /** Describes the same choices and limits used to validate commands, without guessing device capabilities. */
    synchronized StateDescriptionFragment stateDescription(Point point) {
        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create()
                .withReadOnly(!point.writable()).withOptions(stateOptions(point));
        JsonObject rep = resources.get(point.href());
        if (rep != null && "Number:Temperature".equals(point.itemType())) {
            JsonObject item = point.href().endsWith(TEMPERATURES_VS) ? temperatureItem(rep) : null;
            Unit<Temperature> unit = item == null ? temperatureUnit(rep.get("units"))
                    : temperatureUnit(item.get(VENDOR + "unit"));
            if (unit != null) {
                builder.withPattern("%s " + unit);
            }
            Bounds limits = point.href().endsWith(DESIRED) ? bounds(point.href(), rep)
                    : item != null && point.field().equals(VENDOR + "desired") ? vendorBounds(item) : null;
            if (limits != null) {
                builder.withMinimum(limits.minimum()).withMaximum(limits.maximum()).withStep(limits.increment());
            }
        } else if ("Number:Power".equals(point.itemType())) {
            builder.withPattern("%s W");
        } else if (point.href().endsWith(AIRFLOW) && "speed".equals(point.field())
                && deviceType(prefix(point.href(), AIRFLOW), "oic.d.airpurifier")) {
            builder.withMinimum(BigDecimal.ZERO).withMaximum(BigDecimal.valueOf(4)).withStep(BigDecimal.ONE);
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
        JsonObject rep = resources.get(point.href());
        return rep != null && modeSuffix(point.href()) != null && point.field().equals(VENDOR + "modes")
                ? supportedModes(rep).stream().distinct().map(value -> new StateOption(value, value)).toList()
                : List.of();
    }

    /** Returns UNDEF for malformed or unknown values, without coercing them into valid appliance states. */
    public synchronized State state(Point point) {
        JsonObject rep = resources.get(point.href());
        if (rep == null || isStub(rep)
                || points().stream().noneMatch(p -> p.id().equals(point.id()) && p.href().equals(point.href())
                        && p.field().equals(point.field()) && p.itemType().equals(point.itemType()))) {
            return UnDefType.UNDEF;
        }
        if (point.field().isEmpty()) {
            return new StringType(sanitized(rep).toString());
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
            return watts == null || watts.signum() < 0 ? UnDefType.UNDEF : new QuantityType<>(watts, Units.WATT);
        }
        if ("Number".equals(point.itemType())) {
            BigDecimal numeric = number(value);
            return numeric == null ? UnDefType.UNDEF : new DecimalType(numeric);
        }
        String text = modeSuffix(point.href()) != null ? currentMode(rep) : string(value);
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
        Point current = points().stream().filter(p -> p.equals(point)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown or changed resource channel"));
        if (!current.writable()) {
            throw new IllegalArgumentException("Resource channel is read-only");
        }
        String href = current.href();
        String modesSuffix = modeSuffix(href);
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

    private static void add(List<Point> points, String href, String field, String itemType, String label,
            boolean writable) {
        String source = href + "\0" + field;
        String readable = (href.substring(1) + (field.isEmpty() ? "-json" : "-" + field)).replaceAll("[^A-Za-z0-9_-]",
                "-");
        // Stable hashing avoids renaming channels when another path sanitizes to the same ID.
        String hash = UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)).toString();
        points.add(new Point(readable + "-" + hash, href, field, itemType, label, writable));
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

    private static @Nullable String modeSuffix(String href) {
        for (String suffix : List.of(MODE, WIND_STRENGTH, WIND_DIRECTION, CONVENIENT)) {
            if (href.endsWith(suffix)) {
                return suffix;
            }
        }
        return null;
    }

    private static JsonElement sanitized(JsonElement value) {
        if (value.isJsonObject()) {
            JsonObject result = new JsonObject();
            value.getAsJsonObject().entrySet().forEach(entry -> {
                String key = entry.getKey().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z]", "");
                if (!key.matches(
                        ".*(password|credential|secret|psk|token|privatekey|privatedata|owneruuid|ownerid).*")) {
                    result.add(entry.getKey(), sanitized(entry.getValue()));
                }
            });
            return result;
        }
        if (value.isJsonArray()) {
            JsonArray result = new JsonArray();
            value.getAsJsonArray().forEach(entry -> {
                String href = entry.isJsonObject() ? string(entry.getAsJsonObject().get("href")) : null;
                if (href == null || !href.contains("/sec/")) {
                    result.add(sanitized(entry));
                }
            });
            return result;
        }
        return value.deepCopy();
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
