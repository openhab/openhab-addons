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
package org.openhab.binding.zwavejs.internal.conversion;

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;
import static org.openhab.binding.zwavejs.internal.CommandClassConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.ColorUtil;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseMetadata} class represents basic metadata information for a Z-Wave node.
 * It contains various properties and methods to handle metadata and state information.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public abstract class BaseMetadata {

    private Logger logger = LoggerFactory.getLogger(BaseMetadata.class);
    private static final String DEFAULT_LABEL = "Unknown Label";
    private static final Map<String, String> UNIT_REPLACEMENTS = Map.of("lux", "lx", //
            "Lux", "lx", //
            "KwH", "kWh", //
            "minutes", "min", //
            "Minutes", "min", //
            "seconds", "s", //
            "Seconds", "s", //
            "°(C/F)", "", // special case where Zwave JS sends °F/C as unit, but is actually dimensionless
            "°F/C", "", // special case where Zwave JS sends °F/C as unit, but is actually dimensionless
            "%rH", "%"); // Z-Wave JS uses %rH to represent relative humidity, but openHAB expects the standard % unit.

    private static final Map<String, String> CHANNEL_ID_PROPERTY_NAME_REPLACEMENTS = Map.of("currentValue", "value", //
            "targetValue", "value", "currentColor", "color", "targetColor", "color", //
            "targetMode", "mode", "currentMode", "mode"); //
    private static final List<Integer> COMMAND_CLASSES_ADVANCED = List.of(44, 117);
    private static final List<Integer> SWITCH_STATES_OFF_CLOSED = List.of(-1, 0, 23);

    public final int nodeId;
    public final String id;
    public final String label;
    public final boolean writable;
    public final String itemType;
    public final boolean isAdvanced;
    public final int commandClassId;
    public final int endpoint;
    public final Double factor;

    public @Nullable String description;
    public @Nullable String unitSymbol;
    public @Nullable Unit<?> unit;
    public @Nullable Object readProperty;
    public @Nullable Object writeProperty;
    public @Nullable Map<String, String> optionList;
    public @Nullable String commandClassName;
    public @Nullable String propertyKeyName;
    public @Nullable Object propertyKey;

    public final Object value;
    protected final @Nullable Integer min;
    protected @Nullable Long max;

    protected BaseMetadata(int nodeId, Value value) {
        this.nodeId = nodeId;
        this.commandClassName = value.commandClassName;
        this.commandClassId = value.commandClass;
        this.endpoint = value.endpoint;
        this.propertyKey = value.propertyKey;
        this.writable = value.metadata.writeable;
        this.min = value.metadata.min;
        this.max = value.metadata.max;
        this.id = generateChannelId(value);

        this.label = normalizeLabel(value.metadata.label, value.endpoint, value.propertyName);
        this.description = value.metadata.description != null ? value.metadata.description : null;
        this.unitSymbol = normalizeUnit(value.metadata.unit, value.value);
        this.factor = determineFactor(value.metadata.unit);
        this.unit = UnitUtils.parseUnit(this.unitSymbol);
        this.itemType = itemTypeFromMetadata(value.metadata.type, value.value, value.commandClass,
                value.metadata.states);
        if (unitSymbol != null && unit == null) {
            logger.warn("Node {}, unable to parse unitSymbol '{}', please file a bug report", nodeId, unitSymbol);
        }
        this.optionList = value.metadata.states;
        this.value = value.value;
        this.isAdvanced = isAdvanced(value.commandClass, value.propertyName, value.propertyKey);

        if (writable) {
            this.writeProperty = value.property;
        } else {
            this.readProperty = value.property;
        }
    }

    public BaseMetadata(int nodeId, Event data) {
        this.nodeId = nodeId;
        this.id = generateId(data);
        this.value = data.args.newValue;

        this.min = null;
        this.max = null;
        this.commandClassId = 0;
        this.endpoint = 0;
        this.writable = false;
        this.label = DEFAULT_LABEL;
        this.itemType = CoreItemFactory.STRING;
        this.isAdvanced = false;
        this.factor = 1.0;
    }

    /*
     * Determines the factor based on the unit string.
     *
     * @param unitString the unit string
     *
     * @return the factor
     */
    protected Double determineFactor(@Nullable String unitString) {
        if (unitString == null && value instanceof Map<?, ?> treeMap) {
            if (treeMap.containsKey("unit")) {
                unitString = (String) treeMap.get("unit");
            }
        }
        if (unitString == null) {
            return 1.0;
        }

        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+|[^0-9]+");
        Matcher matcher = pattern.matcher(unitString.trim());

        String[] splitted = matcher.results().map(m -> m.group()).toArray(String[]::new);
        if (splitted.length < 2) {
            return 1.0;
        }

        try {
            return Double.parseDouble(splitted[0]);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    protected boolean isAdvanced(int commandClassId, String propertyName, @Nullable Object propertyKey) {
        return COMMAND_CLASSES_ADVANCED.contains(commandClassId);
    }

    /*
     * Normalizes the label based on the provided parameters.
     *
     * @param label the label
     *
     * @param endpoint the endpoint
     *
     * @param propertyName the property name
     *
     * @return the normalized label
     */
    private String normalizeLabel(@Nullable String label, int endpoint, String propertyName) {
        String output = "";
        if (label == null || label.isBlank()) {
            return propertyName;
        }
        output = label.replaceAll("\s\\[.*\\]", "");
        output = capitalize(output);
        if (endpoint > 0) {
            output = String.format("EP%s %s", endpoint, output);
        }
        return output;
    }

    /*
     * Capitalizes the input string by splitting camelCase words.
     *
     * @param input the input string
     *
     * @return the capitalized string
     */
    private String capitalize(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return DEFAULT_LABEL;
        }

        return Objects
                .requireNonNullElse(
                        Arrays.stream(StringUtils.splitByCharacterType(input)).filter(f -> !f.isBlank())
                                .map(word -> StringUtils.capitalize(word)).collect(Collectors.joining(" ")),
                        DEFAULT_LABEL)
                .replace(" - ", "-").replace("( ", "(").replace(" )", ")");
    }

    private String normalizeString(@Nullable Object input) {
        if (input instanceof Number numberInput) {
            return "-" + numberInput.toString();
        } else if (input instanceof String strInput) {
            return "-" + strInput.trim().toLowerCase().replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", "");
        }
        return "";
    }

    private String generateId(String commandClassName, int endpoint, @Nullable String propertyName,
            @Nullable Object propertyKey) {
        String id = normalizeString(commandClassName).replaceFirst("-", "");
        String[] splitted;
        if (propertyName != null && !propertyName.contains("unknown")) {
            propertyName = CHANNEL_ID_PROPERTY_NAME_REPLACEMENTS.getOrDefault(propertyName, propertyName);
            splitted = StringUtils.splitByCharacterType(propertyName);
            List<String> result = Arrays.asList(splitted).stream().filter(s -> s.matches("^[a-zA-Z0-9]+$")).toList();
            if (!result.isEmpty()) {
                id += normalizeString(String.join("-", result));
            }
        }
        if (propertyKey != null) {
            id += normalizeString(propertyKey);
        }
        if (endpoint > 0) {
            id += "-" + endpoint;
            return id;
        }

        return id;
    }

    private String generateId(Event event) {
        return generateId(event.args.commandClassName, event.args.endpoint, event.args.propertyName,
                event.args.propertyKey);
    }

    private String generateChannelId(Value value) {
        return generateId(value.commandClassName, value.endpoint, value.propertyName, value.propertyKey);
    }

    /*
     * Converts the given value to a State object based on the item type and unit.
     *
     * @param value the value to convert
     *
     * @param itemType the item type
     *
     * @param unit the unit of the value
     *
     * @param inverted whether the value should be inverted
     *
     * @param factor the factor to apply to the value
     *
     * @return the converted State object, or UnDefType.NULL if the value is null
     */
    protected @Nullable State toState(@Nullable Object value, String itemType, @Nullable Unit<?> unit, boolean inverted,
            Double factor) {
        if (value == null) {
            return UnDefType.NULL;
        }

        if (value instanceof Map<?, ?> treeMap) {
            if (treeMap.containsKey("value")) {
                value = Objects.requireNonNull(treeMap.get("value"));
            }
        }

        String itemTypeSplitted[] = itemType.split(":");
        switch (itemTypeSplitted[0]) {
            case CoreItemFactory.NUMBER:
                return handleNumberType(value, unit, factor);
            case CoreItemFactory.DIMMER:
                return handleDimmerType(value, inverted);
            case CoreItemFactory.SWITCH:
                return handleSwitchType(value, inverted);
            case CoreItemFactory.COLOR:
                return handleColorType(value);
            case CoreItemFactory.STRING:
                return StringType.valueOf(Objects.requireNonNull(value).toString());
            default:
                logger.warn("Node {}, unexpected item type: {}, please file a bug report", nodeId, itemType);
                return UnDefType.UNDEF;
        }
    }

    private @Nullable State handleNumberType(Object value, @Nullable Unit<?> unit, Double factor) {
        if (!(value instanceof Number numberVal)) {
            if (value instanceof String strVal) {
                if ("unknown".equalsIgnoreCase(strVal)) {
                    return UnDefType.UNDEF;
                }
            }
            logger.warn("Node {}, unexpected value type for number: {}, please file a bug report", nodeId,
                    value.getClass().getName());
            return UnDefType.UNDEF;
        }

        numberVal = factor == 1.0 ? numberVal : numberVal.doubleValue() * factor;
        if (unit != null) {
            return new QuantityType<>(numberVal, unit);
        } else {
            return new DecimalType(numberVal);
        }
    }

    private @Nullable State handleDimmerType(Object value, boolean inverted) {
        if (value instanceof Number numberValue) {
            try {
                return new PercentType(inverted ? 100 - numberValue.intValue() : numberValue.intValue());
            } catch (IllegalArgumentException e) {
                logger.warn("Node {}, invalid PercentType value provided: {}", nodeId, numberValue);
                return UnDefType.UNDEF;
            }
        } else {
            logger.warn("Node {}, unexpected value type for dimmer: {}, please file a bug report", nodeId,
                    value.getClass().getName());
            return UnDefType.UNDEF;
        }
    }

    private @Nullable State handleSwitchType(Object value, boolean inverted) {
        if (value instanceof Number numberValue) {
            boolean offOrClosedState = SWITCH_STATES_OFF_CLOSED.contains(numberValue.intValue());
            return OnOffType.from(inverted ? offOrClosedState : !offOrClosedState);
        }
        if (!(value instanceof Boolean boolVal)) {
            logger.warn("Node {}, unexpected value type for switch: {}, please file a bug report", nodeId,
                    value.getClass().getName());
            return UnDefType.UNDEF;
        }

        return inverted ? OnOffType.from(!boolVal) : OnOffType.from(boolVal);
    }

    private @Nullable State handleColorType(Object value) {
        if (value instanceof String colorStr) {
            try {
                colorStr = colorStr.startsWith("#") ? colorStr : "#" + colorStr;
                int red = Integer.valueOf(colorStr.substring(1, 3), 16);
                int green = Integer.valueOf(colorStr.substring(3, 5), 16);
                int blue = Integer.valueOf(colorStr.substring(5, 7), 16);
                return HSBType.fromRGB(red, green, blue);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                logger.warn("Node {}, invalid color string provided: {}", nodeId, colorStr, e);
                return UnDefType.UNDEF;
            }
        } else if (value instanceof Map<?, ?> map && isColorMap(map)) {
            int red = map.get(RED) instanceof Number n ? n.intValue() : -1;
            int green = map.get(GREEN) instanceof Number n ? n.intValue() : -1;
            int blue = map.get(BLUE) instanceof Number n ? n.intValue() : -1;
            int warm = map.get(WARM_WHITE) instanceof Number n ? n.intValue() : -1;
            int cold = map.get(COLD_WHITE) instanceof Number n ? n.intValue() : -1;
            if (red >= 0 && green >= 0 && blue >= 0 && warm <= 0 && cold <= 0) {
                return HSBType.fromRGB(red, green, blue);
            } else if (warm > 0 && cold > 0) {
                return ColorUtil.xyToHsb(ColorUtil.kelvinToXY(6500 - (4000 * warm / (warm + cold))));
            } else if (warm > 0) {
                return ColorUtil.xyToHsb(ColorUtil.kelvinToXY(6500 - (4000 * warm / 255)));
            } else if (cold > 0) {
                return ColorUtil.xyToHsb(ColorUtil.kelvinToXY(2500 + (4000 * cold / 255)));
            }
            return UnDefType.UNDEF;
        } else {
            logger.warn("Node {}, unexpected value type for color: {}, please file a bug report", nodeId,
                    value.getClass().getName());
            return UnDefType.UNDEF;
        }
    }

    /*
     * Corrects the metadata type based on the provided value, command class name, and optional list of options.
     *
     * @param type The original metadata type.
     *
     * @param value The value to determine the type from if the original type is ANY.
     *
     * @param commandClassName The name of the command class.
     *
     * @param optionList An optional list of options that may influence the type correction.
     *
     * @return The corrected metadata type.
     */
    protected MetadataType correctedType(MetadataType type, @Nullable Object value, int commandClass,
            @Nullable Map<String, String> optionList) {
        switch (type) {
            case ANY:
                return determineTypeFromValue(value, commandClass);
            case DURATION:
                return MetadataType.NUMBER;
            case NUMBER:
                if (COMMAND_CLASS_ALARM == commandClass && optionList != null && optionList.size() == 2) {
                    return MetadataType.BOOLEAN;
                }
            default:
                return type;
        }
    }

    /*
     * Determines the metadata type based on the provided value and command class.
     *
     * @param value The value to determine the metadata type from. Can be null.
     *
     * @param commandClass The Z-Wave command class identifier used for additional type determination in specific cases.
     *
     * @return The determined metadata type.
     */
    private MetadataType determineTypeFromValue(@Nullable Object value, int commandClass) {
        if (value instanceof Number) {
            return MetadataType.NUMBER;
        } else if (value instanceof Boolean) {
            return MetadataType.BOOLEAN;
        } else if (value instanceof Map<?, ?> treeMap) {
            if (isColorMap(treeMap)) {
                return MetadataType.COLOR;
            } else if (treeMap.containsKey("value")) {
                return determineTypeFromValue(treeMap.get("value"), commandClass);
            }
        } else if (value instanceof String) {
            return MetadataType.STRING;
        }

        if (commandClass == COMMAND_CLASS_DOOR_LOCK) {
            if (value instanceof ArrayList) {
                return MetadataType.STRING;
            }
            return MetadataType.BOOLEAN; // Notification CC can be boolean or string, but we default to boolean
        }

        logger.warn("Node {}, unexpected value type: {}, please file a bug report", nodeId,
                value != null ? value.getClass().getName() : "null");
        return MetadataType.STRING;
    }

    /*
     * Checks if the given map represents an RGB Color or Color Temperature map.
     * The map should have either all the three keys "red", "green", and "blue",
     * and/or one or two of the keys "warmWhite" and "coldWhite".
     *
     * @param map the map to check
     *
     * @return true if the map represents an RGB color map, false otherwise
     */
    private boolean isColorMap(Map<?, ?> map) {
        return (map.containsKey("red") && map.containsKey("green") && map.containsKey("blue"))
                || map.containsKey("warmWhite") || map.containsKey("coldWhite");
    }

    protected String itemTypeFromMetadata(MetadataType type, @Nullable Object value, int commandClass,
            @Nullable Map<String, String> optionList) {
        type = correctedType(type, value, commandClass, optionList);

        switch (type) {
            case NUMBER:
                Unit<?> unit = this.unit;
                if (unit != null) {
                    String dimension = UnitUtils.getDimensionName(unit);
                    if (dimension == null) {
                        logger.warn("Node {}. Could not parse '{}' as a unit, fallback to 'Number' itemType", nodeId,
                                unitSymbol);
                        return CoreItemFactory.NUMBER;
                    }
                    return CoreItemFactory.NUMBER + ":" + dimension;
                }

                return CoreItemFactory.NUMBER;
            case BOOLEAN:
                // switch (or contact ?)
                return CoreItemFactory.SWITCH;
            case COLOR:
                return CoreItemFactory.COLOR;
            case STRING:
            case STRING_ARRAY:
                return CoreItemFactory.STRING;
            default:
                logger.warn(
                        "Node {}. Unable to determine item type based on metadata.type: {}, fallback to 'String' please file a bug report",
                        nodeId, type);
                return CoreItemFactory.STRING;
        }
    }

    protected @Nullable StateDescriptionFragment createStatePattern(boolean writeable, @Nullable Integer min,
            @Nullable Long max, @Nullable Integer step, @Nullable Object value) {
        String pattern = null;
        String itemTypeSplitted[] = itemType.split(":");
        switch (itemTypeSplitted[0]) {
            case CoreItemFactory.NUMBER:
                String numberFormat = "%d";
                if (value instanceof Double) {
                    // TODO: how to properly determine the decimals?
                    numberFormat = "%.2f";
                }
                if (itemTypeSplitted.length > 1) {
                    pattern = numberFormat + " %unit%";
                } else {
                    pattern = numberFormat;
                }
                break;
            case CoreItemFactory.DIMMER:
                pattern = "%1d %%";
                break;
            case CoreItemFactory.COLOR:
                break;
            case CoreItemFactory.STRING:
            case CoreItemFactory.SWITCH:
            default:
                return null;
        }

        var fragment = StateDescriptionFragmentBuilder.create();
        if (pattern != null) {
            fragment.withPattern(pattern);
        }
        fragment.withReadOnly(!writeable);
        if (min != null) {
            fragment.withMinimum(BigDecimal.valueOf(min));
        }
        if (max != null) {
            fragment.withMaximum(BigDecimal.valueOf(max));
        }
        Map<String, String> optionList = this.optionList;
        if (optionList != null) {
            List<StateOption> options = new ArrayList<>();
            optionList.forEach((k, v) -> options.add(new StateOption(k, v)));
            fragment.withOptions(options);
        }
        if (step != null && step > 0) {
            fragment.withStep(BigDecimal.valueOf(step));
        }
        return fragment.build();
    }

    protected @Nullable String normalizeUnit(@Nullable String unitString, @Nullable Object value) {
        if (unitString == null && value instanceof Map<?, ?> treeMap) {
            if (treeMap.containsKey("unit")) {
                unitString = (String) treeMap.get("unit");
            }
        }
        if (unitString == null) {
            return null;
        }
        unitString = unitString.trim();
        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+|[^0-9]+");
        Matcher matcher = pattern.matcher(unitString);
        String[] splitted = matcher.results().map(m -> m.group()).toArray(String[]::new);
        String lastPart = splitted.length > 0 ? splitted[splitted.length - 1].trim() : unitString;
        String output = Objects
                .requireNonNull(UNIT_REPLACEMENTS.getOrDefault(lastPart, Objects.requireNonNull(lastPart)));

        return !output.isBlank() ? output : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseMetadata [");
        sb.append(", nodeId=" + nodeId);
        sb.append(", Id=" + id);
        sb.append(", label=" + label);
        sb.append(", description=" + description);
        sb.append(", unitSymbol=" + unitSymbol);
        sb.append(", value=" + value);
        sb.append(", itemType=" + itemType);
        sb.append(", writable=" + writable);
        sb.append(", propertyKey=" + propertyKey);
        sb.append(", readProperty=" + readProperty);
        sb.append(", writeProperty=" + writeProperty);
        sb.append(", itemType=" + itemType);
        sb.append("]");
        return sb.toString();
    }
}
