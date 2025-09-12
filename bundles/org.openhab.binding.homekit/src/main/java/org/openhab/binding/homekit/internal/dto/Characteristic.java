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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.provider.HomekitStorageBasedTypeProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit characteristic DTO.
 * Used to deserialize individual characteristics from the /accessories endpoint of a HomeKit bridge.
 * Each characteristic has a type, instance ID (iid), value, permissions (perms), and format.
 * This class also includes a method to convert the characteristic to an openHAB ChannelType, if possible.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class Characteristic {

    // invariant fields that define a unique characteristic
    public @SerializedName("type") String characteristicId; // e.g. '25' => 'public.hap.characteristic.on'
    public @SerializedName("format") String dataFormat; // e.g. "bool"
    public @SerializedName("unit") String unit; // e.g. "celsius"
    public @SerializedName("maxValue") Double maxValue; // e.g. 100
    public @SerializedName("minValue") Double minValue; // e.g. 0
    public @SerializedName("minStep") Double minStep;
    public @SerializedName("perms") List<String> permissions; // e.g. ["pr", "pw", "ev"]

    // ephemeral fields that may change over time or across instances
    public @SerializedName("iid") Integer instanceId; // e.g. 10
    public @SerializedName("value") String dataValue; // e.g. true
    public @SerializedName("description") String description;

    // configuration information fields
    public @SerializedName("ev") Boolean eventNotification; // e.g. true
    public @SerializedName("maxLen") Double maxLen; // e.g. 64

    /**
     * The hash only includes the invariant fields as needed to define a fully unique characteristic.
     * The instanceId, dataValue and description are excluded as they depend on accessory instance and state.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(characteristicId, dataFormat, unit, minValue, maxValue, minStep, permissions);
    }

    /**
     * Builds a ChannelDefinition and ChannelType based on the characteristic properties.
     * Registers the ChannelType with the provided {@link HomekitStorageBasedTypeProvider}.
     * Returns null if the characteristic cannot be mapped to a channel definition.
     * Examines characteristic type, data format, permissions, and other properties
     * to determine appropriate channel type, item type, tags, category, and attributes.
     *
     * @param typeProvider the HomekitTypeProvider to register the channel type with
     * @return the ChannelDefinition or null if it cannot be mapped
     */
    public @Nullable ChannelDefinition buildAndRegisterChannelDefinition(HomekitStorageBasedTypeProvider typeProvider) {
        CharacteristicType characteristicType;
        try {
            characteristicType = CharacteristicType.from(Integer.parseInt(characteristicId));
        } catch (IllegalArgumentException e) {
            return null;
        }

        DataFormatType dataFormatType;
        try {
            dataFormatType = DataFormatType.from(dataFormat);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Unit<?> unit = null;
        String temp = this.unit;
        if (temp != null) {
            unit = UnitUtils.parseUnit(temp);
        }

        // determine channel type and attributes based on characteristic properties
        boolean isReadOnly = !permissions.contains("pw");
        boolean isString = DataFormatType.STRING == dataFormatType;
        boolean isBoolean = DataFormatType.BOOL == dataFormatType;
        boolean isNumber = !isString && !isBoolean;
        boolean isStateChannel = true;

        String itemType = null;
        String category = null;
        String numberSuffix = null;
        SemanticTag pointTag = null;
        SemanticTag propertyTag = null;

        if (isReadOnly) {
            if (isBoolean) {
                itemType = CoreItemFactory.SWITCH;
                pointTag = Point.STATUS;
                category = "switch";
            } else if (isNumber) {
                itemType = CoreItemFactory.NUMBER;
                pointTag = Point.MEASUREMENT;
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
                itemType = CoreItemFactory.NUMBER;
                pointTag = Point.SETPOINT;
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
                break;

            case AIR_PARTICULATE_DENSITY:
                numberSuffix = "Density";
                propertyTag = Property.PARTICULATE_MATTER;
                break;

            case AIR_PARTICULATE_SIZE:
                numberSuffix = "Length";
                propertyTag = Property.PARTICULATE_MATTER;
                break;

            case AIR_PURIFIER_STATE_CURRENT:
            case AIR_PURIFIER_STATE_TARGET:
                propertyTag = Property.ENABLED;
                break;

            case AIR_QUALITY:
                numberSuffix = "Dimensionless";
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
                pointTag = Point.ALARM;
                propertyTag = Property.CO2;
                category = "co2";
                break;

            case CARBON_DIOXIDE_LEVEL:
            case CARBON_DIOXIDE_PEAK_LEVEL:
                numberSuffix = "Density";
                propertyTag = Property.CO2;
                category = "co2";
                break;

            case CARBON_MONOXIDE_DETECTED:
                pointTag = Point.ALARM;
                propertyTag = Property.CO;
                category = "alarm";
                break;

            case CARBON_MONOXIDE_LEVEL:
            case CARBON_MONOXIDE_PEAK_LEVEL:
                numberSuffix = "Density";
                propertyTag = Property.CO;
                break;

            case CHARGING_STATE:
                propertyTag = Property.ENERGY;
                category = "battery";
                break;

            case COLOR_TEMPERATURE:
                numberSuffix = unit == null ? "Dimensionless" : "Temperature";
                propertyTag = Property.COLOR_TEMPERATURE;
                category = "light";
                break;

            case CONTACT_STATE:
                break;

            case DENSITY_NO2:
                numberSuffix = "Density";
                propertyTag = Property.AIR_QUALITY;
                break;

            case DENSITY_OZONE:
                numberSuffix = "Density";
                propertyTag = Property.OZONE;
                break;

            case DENSITY_PM10:
            case DENSITY_PM2_5:
                numberSuffix = "Density";
                propertyTag = Property.PARTICULATE_MATTER;
                break;

            case DENSITY_SO2:
                numberSuffix = "Density";
                propertyTag = Property.AIR_QUALITY;
                break;

            case DENSITY_VOC:
                numberSuffix = "Density";
                propertyTag = Property.VOC;
                break;

            case DOOR_STATE_CURRENT:
            case DOOR_STATE_TARGET:
                propertyTag = Property.OPEN_STATE;
                break;

            case FAN_STATE_CURRENT:
            case FAN_STATE_TARGET:
                propertyTag = Property.POWER;
                break;

            case FILTER_CHANGE_INDICATION:
            case FILTER_LIFE_LEVEL:
            case FILTER_RESET_INDICATION:
                break;

            case FIRMWARE_REVISION:
            case HARDWARE_REVISION:
                break;

            case HEATER_COOLER_STATE_CURRENT:
            case HEATER_COOLER_STATE_TARGET:
                propertyTag = Property.POWER;
                category = "heating";
                break;

            case HEATING_COOLING_CURRENT:
            case HEATING_COOLING_TARGET:
                propertyTag = Property.MODE;
                category = "heating";
                break;

            case HORIZONTAL_TILT_CURRENT:
            case HORIZONTAL_TILT_TARGET:
                propertyTag = Property.TILT;
                break;

            case HUE:
                numberSuffix = "Dimensionless";
                propertyTag = Property.COLOR;
                itemType = CoreItemFactory.COLOR;
                category = "color";
                break;

            case HUMIDIFIER_DEHUMIDIFIER_STATE_CURRENT:
            case HUMIDIFIER_DEHUMIDIFIER_STATE_TARGET:
                propertyTag = Property.HUMIDITY;
                category = "humidity";
                break;

            case IDENTIFY:
                isStateChannel = false;
                break;

            case IMAGE_MIRROR:
            case IMAGE_ROTATION:
                category = "image";
                break;

            case INPUT_EVENT:
                isStateChannel = false;
                break;

            case IN_USE:
            case IS_CONFIGURED:
                break;

            case LEAK_DETECTED:
                pointTag = Point.ALARM;
                propertyTag = Property.WATER;
                category = "alarm";
                break;

            case LIGHT_LEVEL_CURRENT:
                numberSuffix = "Illuminance";
                propertyTag = Property.ILLUMINANCE;
                break;

            case LOCK_MANAGEMENT_AUTO_SECURE_TIMEOUT:
            case LOCK_MANAGEMENT_CONTROL_POINT:
            case LOCK_MECHANISM_LAST_KNOWN_ACTION:
            case LOCK_PHYSICAL_CONTROLS:
                category = "lock";
                break;

            case LOCK_MECHANISM_CURRENT_STATE:
            case LOCK_MECHANISM_TARGET_STATE:
                propertyTag = Property.LOCK_STATE;
                category = "lock";
                break;

            case LOGS:
            case MANUFACTURER:
            case MODEL:
                break;

            case MOTION_DETECTED:
                propertyTag = Property.MOTION;
                category = "motion";
                break;

            case MUTE:
                propertyTag = Property.SOUND_VOLUME;
                category = "sound";
                break;

            case NAME:
            case NIGHT_VISION:
            case OBSTRUCTION_DETECTED:
                break;

            case OCCUPANCY_DETECTED:
                propertyTag = Property.PRESENCE;
                break;

            case ON:
                propertyTag = Property.POWER;
                category = "switch";
                break;

            case OUTLET_IN_USE:
                break;

            case PAIRING_FEATURES:
            case PAIRING_PAIRINGS:
            case PAIRING_PAIR_SETUP:
            case PAIRING_PAIR_VERIFY:
                break;

            case POSITION_CURRENT:
            case POSITION_HOLD:
            case POSITION_STATE:
            case POSITION_TARGET:
                propertyTag = Property.OPENING;
                break;

            case PROGRAM_MODE:
                propertyTag = Property.MODE;
                break;

            case RELATIVE_HUMIDITY_CURRENT:
            case RELATIVE_HUMIDITY_DEHUMIDIFIER_THRESHOLD:
            case RELATIVE_HUMIDITY_HUMIDIFIER_THRESHOLD:
            case RELATIVE_HUMIDITY_TARGET:
                numberSuffix = "Dimensionless";
                propertyTag = Property.HUMIDITY;
                category = "humidity";
                break;

            case REMAINING_DURATION:
                propertyTag = Property.DURATION;
                break;

            case ROTATION_DIRECTION:
            case ROTATION_SPEED:
                break;

            case SATURATION:
                numberSuffix = "Dimensionless";
                propertyTag = Property.COLOR;
                itemType = CoreItemFactory.COLOR;
                category = "color";
                break;

            case SECURITY_SYSTEM_ALARM_TYPE:
                pointTag = Point.ALARM;
                break;

            case SECURITY_SYSTEM_STATE_CURRENT:
            case SECURITY_SYSTEM_STATE_TARGET:
                propertyTag = Property.ENABLED;
                break;

            case SELECTED_AUDIO_STREAM_CONFIGURATION:
            case SELECTED_RTP_STREAM_CONFIGURATION:
            case SERIAL_NUMBER:
            case SERVICE_LABEL_INDEX:
            case SERVICE_LABEL_NAMESPACE:
            case SETUP_DATA_STREAM_TRANSPORT:
            case SETUP_ENDPOINTS:
                break;

            case SET_DURATION:
                propertyTag = Property.DURATION;
                break;

            case SIRI_INPUT_TYPE:
                break;

            case SLAT_STATE_CURRENT:
                propertyTag = Property.TILT;
                break;

            case SMOKE_DETECTED:
                pointTag = Point.ALARM;
                propertyTag = Property.SMOKE;
                category = "smoke";
                break;

            case STATUS_ACTIVE:
                break;

            case STATUS_FAULT:
                pointTag = Point.ALARM;
                break;

            case STATUS_JAMMED:
                pointTag = Point.ALARM;
                break;

            case STATUS_LO_BATT:
                pointTag = Point.ALARM;
                propertyTag = Property.LOW_BATTERY;
                category = "battery";
                break;

            case STATUS_TAMPERED:
                pointTag = Point.ALARM;
                propertyTag = Property.TAMPERED;
                category = "lock";
                break;

            case STREAMING_STATUS:
                propertyTag = Property.MEDIA_CONTROL;
                break;

            case SUPPORTED_AUDIO_CONFIGURATION:
            case SUPPORTED_DATA_STREAM_TRANSPORT_CONFIGURATION:
            case SUPPORTED_RTP_CONFIGURATION:
            case SUPPORTED_TARGET_CONFIGURATION:
            case SUPPORTED_VIDEO_STREAM_CONFIGURATION:
                break;

            case SWING_MODE:
                propertyTag = Property.AIRFLOW;
                break;

            case TARGET_LIST:
                break;

            case TEMPERATURE_COOLING_THRESHOLD:
            case TEMPERATURE_CURRENT:
            case TEMPERATURE_HEATING_THRESHOLD:
            case TEMPERATURE_TARGET:
                propertyTag = Property.TEMPERATURE;
                category = "temperature";
                break;

            case TEMPERATURE_UNITS:
                category = "temperature";
                break;

            case TILT_CURRENT:
            case TILT_TARGET:
                propertyTag = Property.TILT;
                break;

            case TYPE_SLAT:
            case VALVE_TYPE:
            case VERSION:
                break;

            case VERTICAL_TILT_CURRENT:
            case VERTICAL_TILT_TARGET:
                propertyTag = Property.TILT;
                break;

            case VOLUME:
                propertyTag = Property.SOUND_VOLUME;
                category = "sound";
                break;

            case WATER_LEVEL:
                propertyTag = Property.WATER;
                break;

            case ZOOM_DIGITAL:
            case ZOOM_OPTICAL:
                break;
        }

        if (CoreItemFactory.NUMBER.equals(itemType) && numberSuffix != null) {
            itemType = itemType + ":" + numberSuffix;
        }

        /*
         * different accessories may have the same characteristicId, but their other properties
         * e.g. min, max, step, unit may be different so we must ensure unique channel type UIDs
         */
        ChannelTypeUID uid = new ChannelTypeUID(BINDING_ID, Integer.toHexString(hashCode()));

        ChannelType channelType;
        if (isStateChannel) {
            if (itemType == null) {
                return null;
            }

            // build StateDescriptionFragment if any relevant properties are present
            StateDescriptionFragment stateDescriptionFragment = null;
            if (minValue != null || maxValue != null || minStep != null || temp != null) {
                StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create();
                builder.withReadOnly(isReadOnly);
                if (minValue != null) {
                    builder.withMinimum(BigDecimal.valueOf(minValue));
                }
                if (maxValue != null) {
                    builder.withMaximum(BigDecimal.valueOf(maxValue));
                }
                if (minStep != null) {
                    builder.withStep(BigDecimal.valueOf(minStep));
                }
                if (unit != null) {
                    builder.withPattern("%.0f " + unit.getSymbol());
                }
                stateDescriptionFragment = builder.build();
            }

            StateChannelTypeBuilder builder = ChannelTypeBuilder.state(uid, characteristicType.toString(), itemType);
            if (stateDescriptionFragment != null) {
                builder.withStateDescriptionFragment(stateDescriptionFragment);
            }
            if (category != null) {
                builder.withCategory(category);
            }
            if (pointTag != null) {
                if (propertyTag != null) {
                    builder.withTags(pointTag, propertyTag);
                } else {
                    builder.withTags(pointTag);
                }
            }
            // state channel
            channelType = builder.build();
        } else {
            // trigger channel
            channelType = ChannelTypeBuilder.trigger(uid, characteristicType.toString()).build();
        }

        typeProvider.putChannelType(channelType);

        return new ChannelDefinitionBuilder(Integer.toString(instanceId), uid).withLabel(characteristicType.toString())
                .withDescription(description).build();
    }
}
