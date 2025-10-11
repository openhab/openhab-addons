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
package org.openhab.binding.homekit.internal.dto;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

/**
 * HomeKit characteristic DTO.
 * Used to deserialize individual characteristics from the /accessories endpoint of a HomeKit bridge.
 * Each characteristic has a type, instance ID (iid), value, permissions (perms), and format.
 * This class also includes a method to convert the characteristic to an openHAB ChannelType, if possible.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Characteristic {
    public @NonNullByDefault({}) String type; // 25 = public.hap.characteristic.on
    public @NonNullByDefault({}) String format; // e.g. "bool"
    public @NonNullByDefault({}) List<String> perms; // e.g. ["pr", "pw", "ev"]
    public @NonNullByDefault({}) Integer iid; // e.g. 10
    public @NonNullByDefault({}) String unit; // e.g. "celsius" or "percentage"
    public @NonNullByDefault({}) Double maxValue; // e.g. 100
    public @NonNullByDefault({}) Double minValue; // e.g. 0
    public @NonNullByDefault({}) Double minStep;
    public @NonNullByDefault({}) JsonElement value; // e.g. true, 23, "Some String"
    public @NonNullByDefault({}) String description;
    public @NonNullByDefault({}) Boolean ev; // e.g. true
    public @NonNullByDefault({}) Integer aid; // e.g. 10
    public @NonNullByDefault({}) @SerializedName("valid-values") List<Integer> validValues;
    public @NonNullByDefault({}) @SerializedName("valid-values-range") List<Integer> validValuesRange;

    /**
     * Builds a ChannelType and a ChannelDefinition based on the characteristic properties.
     * Registers the ChannelType with the provided HomekitTypeProvider.
     * Returns a ChannelDefinition that is specific instance of ChannelType.
     * Returns null if the characteristic cannot be mapped to a channel definition.
     * Examines characteristic type, data format, permissions, and other properties
     * to determine appropriate channel type, item type, tags, category, and attributes.
     *
     * @param thingUID the ThingUID to associate the ChannelDefinition with
     * @param typeProvider the HomekitTypeProvider to register the channel type with
     * @return the ChannelDefinition or null if it cannot be mapped
     */
    public @Nullable ChannelDefinition buildAndRegisterChannelDefinition(ThingUID thingUID,
            HomekitTypeProvider typeProvider) {
        CharacteristicType characteristicType = getCharacteristicType();
        if (characteristicType == null) {
            return null;
        }

        DataFormatType dataFormatType;
        try {
            dataFormatType = DataFormatType.from(format);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // determine channel type and attributes based on characteristic properties
        boolean isReadOnly = !perms.contains("pw");
        boolean isString = DataFormatType.STRING == dataFormatType;
        boolean isBoolean = DataFormatType.BOOL == dataFormatType;
        boolean isNumber = !isString && !isBoolean;
        boolean isNumberWithSuffix = false;
        boolean isStateChannel = true;
        boolean isPercentage = "percentage".equals(unit);

        String uom = unit == null ? null : switch (unit) {
            case "celsius" -> "°C";
            case "percentage" -> "%";
            case "arcdegrees" -> "°";
            case "lux" -> "lx"; // lux
            case "seconds" -> "s";
            default -> unit; // may be null or a custom unit
        };

        String boolType = null;
        if ("bool".equals(format) && value != null && value.isJsonPrimitive()) {
            // some characteristics have "bool" with non-boolean value types e.g. numbers 0,1 or strings "true","false"
            JsonPrimitive prim = value.getAsJsonPrimitive();
            if (prim.isNumber()) {
                boolType = "number";
            }
            if (prim.isString()) {
                boolType = "string";
            }
        }

        String itemType = null;
        String category = null;
        String numberSuffix = null;
        SemanticTag pointTag = null;
        SemanticTag propertyTag = null;

        if (isReadOnly) {
            if (isBoolean) {
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.STATUS;
                category = "switch";
            } else if (isNumber) {
                itemType = isPercentage ? CoreItemFactory.DIMMER : CoreItemFactory.NUMBER;
                pointTag = isPercentage ? Point.STATUS : Point.MEASUREMENT;
            } else if (isString) {
                itemType = CoreItemFactory.STRING;
                pointTag = Point.STATUS;
            }
        } else {
            if (isBoolean) {
                itemType = CoreItemFactory.SWITCH;
                pointTag = Point.SWITCH;
                category = "switch";
            } else if (isNumber) {
                itemType = isPercentage ? CoreItemFactory.DIMMER : CoreItemFactory.NUMBER;
                pointTag = isPercentage ? Point.CONTROL : Point.SETPOINT;
            } else if (isString) {
                itemType = CoreItemFactory.STRING;
                pointTag = Point.CONTROL;
            }
        }

        switch (characteristicType) {
            case ACCESSORY_PROPERTIES:
            case ACTIVE:
            case ACTIVE_IDENTIFIER:
            case ADMINISTRATOR_ONLY_ACCESS:
                itemType = null;
                break;

            case AIR_PARTICULATE_DENSITY:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.PARTICULATE_MATTER;
                break;

            case AIR_PARTICULATE_SIZE:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case AIR_PURIFIER_STATE_CURRENT:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                break;

            case AIR_PURIFIER_STATE_TARGET:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.ENABLED;
                break;

            case AIR_QUALITY:
                // TODO numeric enum 5 states
                propertyTag = Property.AIR_QUALITY;
                break;

            case AUDIO_FEEDBACK:
                break;

            case BATTERY_LEVEL:
                numberSuffix = "Dimensionless";
                propertyTag = Property.ENERGY;
                category = "battery";
                break;

            case BRIGHTNESS:
                itemType = CoreItemFactory.DIMMER;
                propertyTag = Property.BRIGHTNESS;
                category = "light";
                break;

            case BUTTON_EVENT:
                isStateChannel = false;
                break;

            case CARBON_DIOXIDE_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.CO2;
                category = "co2";
                break;

            case CARBON_DIOXIDE_LEVEL:
            case CARBON_DIOXIDE_PEAK_LEVEL:
                uom = "ppm";
                numberSuffix = "Density";
                propertyTag = Property.CO2;
                category = "co2";
                break;

            case CARBON_MONOXIDE_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.CO;
                category = "alarm";
                break;

            case CARBON_MONOXIDE_LEVEL:
            case CARBON_MONOXIDE_PEAK_LEVEL:
                uom = "ppm";
                numberSuffix = "Density";
                propertyTag = Property.CO;
                break;

            case CHARGING_STATE:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                category = "battery";
                break;

            case COLOR_TEMPERATURE:
                uom = "mired";
                numberSuffix = "Temperature";
                propertyTag = Property.COLOR_TEMPERATURE;
                category = "light";
                break;

            case CONTACT_STATE:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.STATUS;
                category = "switch";
                break;

            case DENSITY_NO2:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.AIR_QUALITY;
                break;

            case DENSITY_OZONE:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.OZONE;
                break;

            case DENSITY_PM10:
            case DENSITY_PM2_5:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.PARTICULATE_MATTER;
                break;

            case DENSITY_SO2:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.AIR_QUALITY;
                break;

            case DENSITY_VOC:
                uom = "µg/m³";
                numberSuffix = "Density";
                propertyTag = Property.VOC;
                break;

            case DOOR_STATE_CURRENT:
                // TODO numeric enum 5 states
                propertyTag = Property.OPEN_STATE;
                break;

            case DOOR_STATE_TARGET:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.OPEN_STATE;
                break;

            case FAN_STATE_CURRENT:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                break;

            case FAN_STATE_TARGET:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.MODE;
                break;

            case FILTER_CHANGE_INDICATION:
                itemType = CoreItemFactory.CONTACT;
                break;

            case FILTER_LIFE_LEVEL:
                itemType = CoreItemFactory.NUMBER;
                uom = "%";
                numberSuffix = "Dimensionless";
                break;

            case FILTER_RESET_INDICATION:
                itemType = CoreItemFactory.SWITCH;
                break;

            case FIRMWARE_REVISION:
            case HARDWARE_REVISION:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case HEATER_COOLER_STATE_CURRENT:
            case HEATER_COOLER_STATE_TARGET:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                category = "heating";
                break;

            case HEATING_COOLING_CURRENT:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                category = "heating";
                break;

            case HEATING_COOLING_TARGET:
                // TODO numeric enum 4 states
                propertyTag = Property.MODE;
                category = "heating";
                break;

            case HORIZONTAL_TILT_CURRENT:
            case HORIZONTAL_TILT_TARGET:
                numberSuffix = "Angle";
                propertyTag = Property.TILT;
                break;

            case HUE:
                numberSuffix = "Angle";
                propertyTag = Property.COLOR;
                itemType = CoreItemFactory.COLOR;
                category = "color";
                break;

            case HUMIDIFIER_DEHUMIDIFIER_STATE_CURRENT:
                // TODO numeric enum 4 states
                propertyTag = Property.MODE;
                category = "humidity";
                break;

            case HUMIDIFIER_DEHUMIDIFIER_STATE_TARGET:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                category = "humidity";
                break;

            case IDENTIFY:
                itemType = null;
                break;

            case IMAGE_MIRROR:
                itemType = CoreItemFactory.SWITCH;
                category = "image";
                break;

            case IMAGE_ROTATION:
                // TODO numeric enum 4 states
                propertyTag = Property.MODE;
                category = "image";
                break;

            case INPUT_EVENT:
                // TODO numeric enum 3 states
                isStateChannel = false;
                break;

            case IN_USE:
            case IS_CONFIGURED:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case LEAK_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.WATER;
                category = "alarm";
                break;

            case LIGHT_LEVEL_CURRENT:
                numberSuffix = "Illuminance";
                propertyTag = Property.ILLUMINANCE;
                break;

            case LOCK_MANAGEMENT_AUTO_SECURE_TIMEOUT:
                numberSuffix = "Duration";
                break;

            case LOCK_MANAGEMENT_CONTROL_POINT:
                // TODO tlv8
                itemType = null;
                break;

            case LOCK_MECHANISM_LAST_KNOWN_ACTION:
                // TODO numeric enum 9 states
                break;

            case LOCK_PHYSICAL_CONTROLS:
                itemType = CoreItemFactory.SWITCH;
                category = "lock";
                break;

            case LOCK_MECHANISM_CURRENT_STATE:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.LOCK_STATE;
                category = "lock";
                break;

            case LOCK_MECHANISM_TARGET_STATE:
                // TODO numeric enum 4 states
                propertyTag = Property.LOCK_STATE;
                category = "lock";
                break;

            case LOGS:
                itemType = null;
                break;

            case MANUFACTURER:
            case MODEL:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case MOTION_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                propertyTag = Property.MOTION;
                category = "motion";
                break;

            case MUTE:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.SOUND_VOLUME;
                category = "sound";
                break;

            case NAME:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case NIGHT_VISION:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.ENABLED;
                break;

            case OBSTRUCTION_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                break;

            case OCCUPANCY_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                propertyTag = Property.PRESENCE;
                break;

            case ON:
                propertyTag = Property.POWER;
                category = "switch";
                break;

            case OUTLET_IN_USE:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case PAIRING_FEATURES:
            case PAIRING_PAIRINGS:
            case PAIRING_PAIR_SETUP:
            case PAIRING_PAIR_VERIFY:
                itemType = null;
                break;

            case POSITION_HOLD:
                itemType = CoreItemFactory.SWITCH;
                break;

            case POSITION_CURRENT:
                propertyTag = Property.OPENING;
                break;

            case POSITION_STATE:
                // TODO numeric enum 3 states
                propertyTag = Property.OPENING;
                break;

            case POSITION_TARGET:
                itemType = CoreItemFactory.DIMMER;
                propertyTag = Property.OPENING;
                break;

            case PROGRAM_MODE:
                // TODO numeric enum 3 states
                propertyTag = Property.MODE;
                break;

            case RELATIVE_HUMIDITY_DEHUMIDIFIER_THRESHOLD:
            case RELATIVE_HUMIDITY_HUMIDIFIER_THRESHOLD:
            case RELATIVE_HUMIDITY_CURRENT:
            case RELATIVE_HUMIDITY_TARGET:
                itemType = CoreItemFactory.NUMBER;
                numberSuffix = "Dimensionless";
                propertyTag = Property.HUMIDITY;
                category = "humidity";
                break;

            case REMAINING_DURATION:
                uom = "s";
                numberSuffix = "Duration";
                propertyTag = Property.DURATION;
                break;

            case ROTATION_DIRECTION:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.MODE;
                break;

            case ROTATION_SPEED:
                itemType = CoreItemFactory.DIMMER;
                propertyTag = Property.SPEED;
                break;

            case SATURATION:
                itemType = CoreItemFactory.DIMMER;
                propertyTag = Property.COLOR;
                category = "color";
                break;

            case SECURITY_SYSTEM_ALARM_TYPE:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                break;

            case SECURITY_SYSTEM_STATE_CURRENT:
            case SECURITY_SYSTEM_STATE_TARGET:
                // TODO numeric enum 4 states
                propertyTag = Property.MODE;
                break;

            case SELECTED_AUDIO_STREAM_CONFIGURATION:
            case SELECTED_RTP_STREAM_CONFIGURATION:
            case SERVICE_LABEL_INDEX:
            case SERVICE_LABEL_NAMESPACE:
            case SETUP_DATA_STREAM_TRANSPORT:
            case SETUP_ENDPOINTS:
                itemType = null;
                break;

            case SERIAL_NUMBER:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case SET_DURATION:
                uom = "s";
                numberSuffix = "Duration";
                propertyTag = Property.DURATION;
                break;

            case SIRI_INPUT_TYPE:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case SLAT_STATE_CURRENT:
                // TODO numeric enum 3 states
                propertyTag = Property.TILT;
                break;

            case SMOKE_DETECTED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.SMOKE;
                category = "smoke";
                break;

            case STATUS_ACTIVE:
                itemType = CoreItemFactory.CONTACT;
                propertyTag = Property.MODE;
                break;

            case STATUS_FAULT:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                break;

            case STATUS_JAMMED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.OPENING;
                break;

            case STATUS_LO_BATT:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.LOW_BATTERY;
                category = "battery";
                break;

            case STATUS_TAMPERED:
                itemType = CoreItemFactory.CONTACT;
                pointTag = Point.ALARM;
                propertyTag = Property.TAMPERED;
                break;

            case STREAMING_STATUS:
                propertyTag = Property.MEDIA_CONTROL;
                break;

            case SUPPORTED_AUDIO_CONFIGURATION:
            case SUPPORTED_DATA_STREAM_TRANSPORT_CONFIGURATION:
            case SUPPORTED_RTP_CONFIGURATION:
            case SUPPORTED_TARGET_CONFIGURATION:
            case SUPPORTED_VIDEO_STREAM_CONFIGURATION:
                itemType = null;
                break;

            case SWING_MODE:
                itemType = CoreItemFactory.SWITCH;
                propertyTag = Property.AIRFLOW;
                break;

            case TARGET_LIST:
                itemType = null;
                break;

            case TEMPERATURE_COOLING_THRESHOLD:
            case TEMPERATURE_CURRENT:
            case TEMPERATURE_HEATING_THRESHOLD:
            case TEMPERATURE_TARGET:
                propertyTag = Property.TEMPERATURE;
                category = "temperature";
                break;

            case TEMPERATURE_UNITS:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case TILT_CURRENT:
            case TILT_TARGET:
                numberSuffix = "Angle";
                propertyTag = Property.TILT;
                break;

            case TYPE_SLAT:
            case VALVE_TYPE:
            case VERSION:
                itemType = FAKE_PROPERTY_CHANNEL;
                break;

            case VERTICAL_TILT_CURRENT:
            case VERTICAL_TILT_TARGET:
                numberSuffix = "Angle";
                propertyTag = Property.TILT;
                break;

            case VOLUME:
                itemType = CoreItemFactory.DIMMER;
                propertyTag = Property.SOUND_VOLUME;
                category = "sound";
                break;

            case WATER_LEVEL:
                numberSuffix = "Dimensionless";
                propertyTag = Property.WATER;
                break;

            case ZOOM_DIGITAL:
            case ZOOM_OPTICAL:
                itemType = null;
                break;
        }

        if (CoreItemFactory.NUMBER.equals(itemType) && numberSuffix != null) {
            itemType = itemType + ":" + numberSuffix;
            isNumberWithSuffix = true;
        }

        /*
         * ================ CREATE FAKE PROPERTY CHANNEL =================
         *
         * create and return fake property channel for characteristics that
         * are not mapped to a real channel
         *
         */
        if (FAKE_PROPERTY_CHANNEL.equals(itemType)) {
            if (value != null && value.isJsonPrimitive()) {
                // create fake property channels for characteristics that contain only static information
                return new ChannelDefinitionBuilder(characteristicType.toCamelCase(), FAKE_PROPERTY_CHANNEL_TYPE_UID)
                        .withLabel(value.getAsString()).build();
            }
            return null;
        }

        /*
         * ================ CREATE AND PERSIST THE CHANNEL TYPE =================
         *
         * NOTE: different accessories may have the same characteristicType, but
         * their other properties e.g. min, max, step, unit may be different, so
         * we create and persist a unique channel type ID for each characteristic
         * instance
         */
        String channelTypeId = CHANNEL_TYPE_ID_FMT.formatted(characteristicType.getOpenhabType());
        if (thingUID.getBridgeIds().isEmpty()) {
            channelTypeId += thingUID.getId();
        } else {
            channelTypeId += thingUID.getBridgeIds().getFirst() + "-" + thingUID.getId();
        }
        ChannelTypeUID channelTypeUid = new ChannelTypeUID(BINDING_ID, channelTypeId);
        String channelTypeLabel = characteristicType.toString();

        if (!isStateChannel) {
            typeProvider.putChannelType(ChannelTypeBuilder.trigger(channelTypeUid, channelTypeLabel).build());

        } else {
            if (itemType == null) {
                return null;
            }

            // build state description fragment
            StateDescriptionFragmentBuilder fragBldr = StateDescriptionFragmentBuilder.create()
                    .withReadOnly(isReadOnly);
            if (isNumber) {
                Optional.ofNullable(minValue).map(v -> BigDecimal.valueOf(v)).ifPresent(b -> fragBldr.withMinimum(b));
                Optional.ofNullable(maxValue).map(v -> BigDecimal.valueOf(v)).ifPresent(b -> fragBldr.withMaximum(b));
                Optional.ofNullable(minStep).map(v -> BigDecimal.valueOf(v)).ifPresent(b -> fragBldr.withStep(b));

                if (isPercentage) {
                    fragBldr.withPattern("%.0f %%");
                } else if (uom != null) {
                    fragBldr.withPattern("%.1f " + uom);
                }

                if (validValues != null && !validValues.isEmpty()) {
                    List<StateOption> options = validValues.stream().map(v -> v.toString())
                            .map(s -> new StateOption(s, s)).toList();
                    fragBldr.withOptions(options);
                } else
                //
                if (validValuesRange != null && validValuesRange.size() == 2) {
                    int min = validValuesRange.stream().mapToInt(Integer::intValue).min().orElse(0); // size check above
                    int max = validValuesRange.stream().mapToInt(Integer::intValue).max().orElse(0); // ditto
                    int step = minStep != null ? minStep.intValue() : 1;
                    List<StateOption> options = new ArrayList<>();
                    for (int i = min; i <= max; i += step) {
                        String s = Integer.toString(i);
                        options.add(new StateOption(s, s));
                    }
                    fragBldr.withOptions(options);
                }
            }
            StateDescriptionFragment stateDescriptionFragment = fragBldr.build();

            // build channel type
            StateChannelTypeBuilder chanTypBldr = ChannelTypeBuilder.state(channelTypeUid, channelTypeLabel, itemType)
                    .withStateDescriptionFragment(stateDescriptionFragment);
            Optional.ofNullable(category).ifPresent(c -> chanTypBldr.withCategory(c));
            if (isNumberWithSuffix && uom != null) {
                chanTypBldr.withUnitHint(uom);
            }
            if (pointTag != null) {
                if (propertyTag != null) {
                    chanTypBldr.withTags(pointTag, propertyTag);
                } else {
                    chanTypBldr.withTags(pointTag);
                }
            }

            // persist the (state) channel TYPE
            typeProvider.putChannelType(chanTypBldr.build());
        }

        /*
         * ================ CREATE AND RETURN CHANNEL DEFINITION =================
         *
         * The channel definition contains additional information beyond the what is
         * in the channel type e.g. channel id, label, iid, format, boolType, etc.
         * so we create and return a channel definition containing this information.
         */
        Map<String, String> props = new HashMap<>();
        Optional.ofNullable(iid).map(v -> v.toString()).ifPresent(s -> props.put(PROPERTY_IID, s));
        Optional.ofNullable(format).ifPresent(s -> props.put(PROPERTY_FORMAT, s));
        Optional.ofNullable(boolType).ifPresent(s -> props.put(PROPERTY_BOOL_TYPE, s));

        // Optional.ofNullable(minValue).map(v -> v.toString()).ifPresent(s -> props.put(PROPERTY_MIN_VALUE, s));
        // Optional.ofNullable(maxValue).map(v -> v.toString()).ifPresent(s -> props.put(PROPERTY_MAX_VALUE, s));
        // Optional.ofNullable(minStep).map(v -> v.toString()).ifPresent(s -> props.put(PROPERTY_MIN_STEP, s));
        // Optional.ofNullable(uom).ifPresent(s -> props.put(PROPERTY_UNIT, s));
        // Optional.ofNullable(perms).map(l -> String.join(",", l)).ifPresent(s -> props.put(PROPERTY_PERMS, s));
        // Optional.ofNullable(ev).map(b -> b.toString()).ifPresent(s -> props.put(PROPERTY_EV, s));

        return new ChannelDefinitionBuilder(characteristicType.getOpenhabType(), channelTypeUid).withProperties(props)
                .withLabel(getChannelInstanceLabel()).build();
    }

    /*
     * Returns the 'description' field if it is present. Otherwise returns the Characteristic type in Title Case.
     */
    private String getChannelInstanceLabel() {
        return description != null && !description.isBlank() ? description
                : Objects.requireNonNull(getCharacteristicType()).toString();
    }

    public @Nullable CharacteristicType getCharacteristicType() {
        try {
            // convert "00000113-0000-1000-8000-0026BB765291" to "00000113"
            String firstPart = type.split("-")[0];
            return CharacteristicType.from(Integer.parseInt(firstPart, 16));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return getCharacteristicType() instanceof CharacteristicType ct ? ct.getType() : "Unknown";
    }
}
