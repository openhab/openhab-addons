/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.providers;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DsChannelTypeProvider} implements the {@link ChannelTypeProvider}
 * generates all supported {@link Channel}'s for digitalSTROM.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
@Component(service = ChannelTypeProvider.class)
public class DsChannelTypeProvider extends BaseDsI18n implements ChannelTypeProvider {

    // channelID building (effect group type + (nothing || SEPERATOR + item type ||
    // SEPERATOR + extended item type) e.g.
    // light_switch, shade or shade_angle
    // channel effect group type
    public static final String LIGHT = "light"; // and tag
    public static final String SHADE = "shade"; // and tag
    public static final String HEATING = "heating"; // and tag
    public static final String GENERAL = "general";
    public static final String SCENE = "scene";
    // channel extended item type
    public static final String WIPE = "wipe";
    public static final String ANGLE = "angle";
    public static final String STAGE = "stage"; // pre stageses e.g. 2+STAGE_SWITCH
    public static final String TEMPERATURE_CONTROLLED = "temperature_controlled";

    // item types
    public static final String DIMMER = "Dimmer";
    public static final String SWITCH = "Switch";
    public static final String ROLLERSHUTTER = "Rollershutter";
    public static final String STRING = "String";
    public static final String NUMBER = "Number";

    public static final String TOTAL_PRE = "total";
    public static final String BINARY_INPUT_PRE = "binary_input";
    public static final String OPTION = "opt";

    // tags
    public static final String GE = "GE";
    public static final String GR = "GR";
    public static final String BL = "BL";
    public static final String SW = "SW";
    public static final String DS = "DS";
    public static final String JOKER = "JOKER";

    // categories
    public static final String CATEGORY_BLINDES = "Blinds";
    public static final String CATEGORY_DIMMABLE_LIGHT = "DimmableLight";
    public static final String CATEGORY_CARBONE_DIOXIDE = "CarbonDioxide";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_BRIGHTNESS = "Brightness";
    public static final String CATEGORY_LIGHT = "Light";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SOUND_VOLUME = "SoundVolume";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_DOOR = "Door";
    public static final String CATEGORY_WINDOW = "Window";
    public static final String CATEGORY_GARAGE_DOOR = "GarageDoor";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_MOTION = "Motion";

    /**
     * Returns the output channel type id as {@link String} for the given
     * {@link ApplicationGroup.Color} and {@link OutputModeEnum} or null, if no
     * channel type exists for the given {@link ApplicationGroup.Color} and
     * {@link OutputModeEnum}.
     *
     * @param functionalGroup of the {@link Device}
     * @param outputMode of the {@link Device}
     * @return the output channel type id or null
     */
    public static String getOutputChannelTypeID(ApplicationGroup.Color functionalGroup, OutputModeEnum outputMode,
            List<OutputChannelEnum> outputChannels) {
        if (functionalGroup != null && outputMode != null) {
            String channelPreID = GENERAL;

            switch (functionalGroup) {
                case YELLOW:
                    channelPreID = LIGHT;
                    break;
                case GREY:
                    if (outputChannels != null && (outputChannels.contains(OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR)
                            || outputChannels.contains(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE))) {
                        return buildIdentifier(SHADE, ANGLE);
                    } else {
                        return buildIdentifier(SHADE);
                    }
                case BLUE:
                    channelPreID = HEATING;
                    if (OutputModeEnum.outputModeIsTemperationControlled(outputMode)) {
                        return buildIdentifier(channelPreID, TEMPERATURE_CONTROLLED);
                    }
                default:
                    break;
            }

            if (OutputModeEnum.outputModeIsSwitch(outputMode)) {
                return buildIdentifier(channelPreID, SWITCH);
            }
            if (OutputModeEnum.outputModeIsDimmable(outputMode)) {
                return buildIdentifier(channelPreID, DIMMER);
            }
            if (!channelPreID.equals(HEATING)) {
                if (outputMode.equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)) {
                    return buildIdentifier(channelPreID, "2", STAGE);
                }
                if (outputMode.equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)) {
                    return buildIdentifier(channelPreID, "3", STAGE);
                }
            }
        }
        return null;
    }

    public static String getMeteringChannelID(MeteringTypeEnum type, MeteringUnitsEnum unit, boolean isTotal) {
        if (isTotal) {
            return buildIdentifier(TOTAL_PRE, type, unit);
        } else {
            return buildIdentifier(type, unit);
        }
    }

    public static MeteringTypeEnum getMeteringType(String channelID) {
        // check metering channel
        String[] meteringChannelSplit = channelID.split(SEPERATOR);
        if (meteringChannelSplit.length > 1) {
            short offset = 0;
            // if total_
            if (meteringChannelSplit.length == 3) {
                offset = 1;
            }
            try {
                // check through IllegalArgumentException, if channel is metering
                return MeteringTypeEnum.valueOf(meteringChannelSplit[0 + offset].toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private static final List<String> SUPPORTED_OUTPUT_CHANNEL_TYPES = new ArrayList<>();

    /**
     * Returns true, if the given channel type id is an output channel.
     *
     * @param channelTypeID to check
     * @return true, if channel type id is output channel
     */
    public static boolean isOutputChannel(String channelTypeID) {
        return SUPPORTED_OUTPUT_CHANNEL_TYPES.contains(channelTypeID);
    }

    @Activate
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference
    @Override
    protected void setTranslationProvider(TranslationProvider translationProvider) {
        super.setTranslationProvider(translationProvider);
    }

    @Override
    protected void unsetTranslationProvider(TranslationProvider translationProvider) {
        super.unsetTranslationProvider(translationProvider);
    }

    @Override
    protected void init() {
        String channelIDpre = GENERAL;
        for (short i = 0; i < 3; i++) {
            if (i == 1) {
                channelIDpre = LIGHT;
            }
            if (i == 2) {
                channelIDpre = HEATING;
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, TEMPERATURE_CONTROLLED));
            }
            SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, SWITCH));
            SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, DIMMER));
            if (i < 2) {
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, "2", STAGE));
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, "3", STAGE));
            }
        }
        channelIDpre = SHADE;
        SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre);
        SUPPORTED_OUTPUT_CHANNEL_TYPES.add(buildIdentifier(channelIDpre, ANGLE));
        SUPPORTED_OUTPUT_CHANNEL_TYPES.add(SCENE);
    }

    private String getSensorCategory(SensorEnum sensorType) {
        switch (sensorType) {
            case ACTIVE_POWER:
            case ELECTRIC_METER:
            case OUTPUT_CURRENT:
            case OUTPUT_CURRENT_H:
            case POWER_CONSUMPTION:
                return CATEGORY_ENERGY;
            case AIR_PRESSURE:
                return CATEGORY_PRESSURE;
            case CARBON_DIOXIDE:
                return CATEGORY_CARBONE_DIOXIDE;
            case PRECIPITATION:
                return CATEGORY_RAIN;
            case RELATIVE_HUMIDITY_INDOORS:
            case RELATIVE_HUMIDITY_OUTDOORS:
                return CATEGORY_HUMIDITY;
            case ROOM_TEMPERATURE_CONTROL_VARIABLE:
                break;
            case ROOM_TEMPERATURE_SET_POINT:
                break;
            case TEMPERATURE_INDOORS:
            case TEMPERATURE_OUTDOORS:
                return CATEGORY_TEMPERATURE;
            case WIND_DIRECTION:
            case WIND_SPEED:
                return CATEGORY_WIND;
            case SOUND_PRESSURE_LEVEL:
                return CATEGORY_SOUND_VOLUME;
            case BRIGHTNESS_INDOORS:
            case BRIGHTNESS_OUTDOORS:
                return CATEGORY_BRIGHTNESS;
            default:
                break;

        }
        return null;
    }

    private String getBinaryInputCategory(DeviceBinarayInputEnum binaryInputType) {
        switch (binaryInputType) {
            case BATTERY_STATUS_IS_LOW:
                return CATEGORY_BATTERY;
            case SUN_RADIATION:
            case SUN_PROTECTION:
            case TWILIGHT:
            case BRIGHTNESS:
                return CATEGORY_BRIGHTNESS;
            case HEATING_OPERATION_ON_OFF:
            case CHANGE_OVER_HEATING_COOLING:
            case TEMPERATION_BELOW_LIMIT:
                return CATEGORY_TEMPERATURE;
            case DOOR_IS_OPEN:
                return CATEGORY_DOOR;
            case GARAGE_DOOR_IS_OPEN:
                return CATEGORY_GARAGE_DOOR;
            case PRESENCE:
            case PRESENCE_IN_DARKNESS:
            case MOTION:
            case MOTION_IN_DARKNESS:
                return CATEGORY_MOTION;
            case RAIN:
                return CATEGORY_RAIN;
            case SMOKE:
                return CATEGORY_SMOKE;
            case WINDOW_IS_OPEN:
            case WINDOW_IS_TILTED:
                return CATEGORY_WINDOW;
            case WIND_STRENGHT_ABOVE_LIMIT:
                return CATEGORY_WIND;
            case FROST:
                return CATEGORY_ALARM;
            default:
                break;

        }
        return null;
    }

    private StateDescriptionFragment getSensorStateDescription(SensorEnum sensorType) {
        // the digitalSTROM resolution for temperature in kelvin is not correct but
        // sensor-events and cached values are
        // shown in °C so we will use this unit for temperature sensors
        String unitShortCut = sensorType.getUnitShortcut();
        if ("%".equals(unitShortCut)) {
            unitShortCut = "%%";
        }
        if (sensorType.toString().contains("TEMPERATURE")) {
            unitShortCut = "°C";
        }
        return StateDescriptionFragmentBuilder.create().withPattern(sensorType.getPattern() + " " + unitShortCut)
                .withReadOnly(true).build();
    }

    private String getStageChannelOption(String type, String option) {
        return buildIdentifier(type, STAGE, OPTION, option);
    }

    private StateDescriptionFragment getStageDescription(String channelID, Locale locale) {
        if (channelID.contains(STAGE.toLowerCase())) {
            List<StateOption> stateOptions = new ArrayList<>();
            if (channelID.contains(LIGHT)) {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON), locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON, getText(
                            getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON),
                            locale)));
                }
            } else {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                        getText(getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF),
                                locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON, getText(
                        getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                        getText(getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON),
                                locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON, getText(
                            getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON),
                            locale)));
                }
            }
            return StateDescriptionFragmentBuilder.create().withReadOnly(false).withOptions(stateOptions).build();
        }
        if (channelID.contains(TEMPERATURE_CONTROLLED)) {
            return StateDescriptionFragmentBuilder.create().withMinimum(new BigDecimal(0))
                    .withMaximum(new BigDecimal(50)).withStep(new BigDecimal(0.1)).withPattern("%.1f °C")
                    .withReadOnly(false).build();
        }
        return null;
    }

    private String getCategory(String channelID) {
        if (channelID.contains(LIGHT)) {
            if (channelID.contains(DIMMER.toLowerCase())) {
                return CATEGORY_DIMMABLE_LIGHT;
            }
            return CATEGORY_LIGHT;
        }
        if (channelID.contains(SHADE)) {
            if (channelID.contains(ANGLE.toLowerCase())) {
                return CATEGORY_BLINDES;
            }
            return ROLLERSHUTTER;
        }
        if (channelID.contains(TEMPERATURE_CONTROLLED)) {
            return CATEGORY_TEMPERATURE;
        }
        return null;
    }

    private Set<String> getTags(String channelID, Locale locale) {
        if (channelID.contains(LIGHT)) {
            return new HashSet<>(Arrays.asList(getText(GE, locale), getText(DS, locale), getText(LIGHT, locale)));
        }
        if (channelID.contains(GENERAL)) {
            return new HashSet<>(Arrays.asList(getText(SW, locale), getText(DS, locale), getText(JOKER, locale)));
        }
        if (channelID.contains(SHADE)) {
            return new HashSet<>(Arrays.asList(getText(GR, locale), getText(DS, locale), getText("SHADE", locale)));
        }
        if (channelID.contains(SCENE)) {
            return new HashSet<>(Arrays.asList(getText(SCENE, locale), getText(DS, locale)));
        }
        if (channelID.contains(HEATING)) {
            return new HashSet<>(Arrays.asList(getText(BL, locale), getText(DS, locale), getText(HEATING, locale)));
        }
        return null;
    }

    private Set<String> getSimpleTags(String channelID, Locale locale) {
        return new HashSet<>(Arrays.asList(getText(channelID, locale), getText(channelID, locale)));
    }

    /**
     * Returns the supported item type for the given channel type id or null, if the
     * channel type does not exist.
     *
     * @param channelTypeID of the channel
     * @return item type or null
     */
    public static String getItemType(String channelTypeID) {
        if (channelTypeID != null) {
            if (stringContains(channelTypeID, STAGE)) {
                return STRING;
            }
            if (stringContains(channelTypeID, SWITCH) || stringContains(channelTypeID, SCENE)
                    || stringContains(channelTypeID, WIPE) || stringContains(channelTypeID, BINARY_INPUT_PRE)) {
                return SWITCH;
            }
            if (stringContains(channelTypeID, DIMMER) || stringContains(channelTypeID, ANGLE)) {
                return DIMMER;
            }
            if (stringContains(channelTypeID, TEMPERATURE_CONTROLLED)) {
                return NUMBER;
            }
            if (channelTypeID.contains(SHADE)) {
                return ROLLERSHUTTER;
            }
        }
        return null;
    }

    private static boolean stringContains(String string, String compare) {
        return string.toLowerCase().contains(compare.toLowerCase());
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> channelTypeList = new LinkedList<>();
        for (String channelTypeId : SUPPORTED_OUTPUT_CHANNEL_TYPES) {
            channelTypeList.add(
                    getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId), locale));
        }
        for (SensorEnum sensorType : SensorEnum.values()) {
            channelTypeList.add(getChannelType(
                    new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, buildIdentifier(sensorType)), locale));
        }
        for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(meteringType, MeteringUnitsEnum.WH)), locale));
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(TOTAL_PRE, meteringType, MeteringUnitsEnum.WH)), locale));
        }
        for (DeviceBinarayInputEnum binaryInput : DeviceBinarayInputEnum.values()) {
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(BINARY_INPUT_PRE, binaryInput)), locale));
        }
        return channelTypeList;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (channelTypeUID.getBindingId().equals(DigitalSTROMBindingConstants.BINDING_ID)) {
            String channelID = channelTypeUID.getId();
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelTypeUID.getId().toUpperCase());
                return ChannelTypeBuilder.state(channelTypeUID, getLabelText(channelID, locale), NUMBER)
                        .withDescription(getDescText(channelID, locale)).withCategory(getSensorCategory(sensorType))
                        .withTags(getSimpleTags(channelID, locale))
                        .withStateDescriptionFragment(getSensorStateDescription(sensorType)).build();
            } catch (IllegalArgumentException e) {
                if (SUPPORTED_OUTPUT_CHANNEL_TYPES.contains(channelID)) {
                    return ChannelTypeBuilder
                            .state(channelTypeUID, getLabelText(channelID, locale), getItemType(channelID))
                            .withDescription(getDescText(channelID, locale)).withCategory(getCategory(channelID))
                            .withTags(getTags(channelID, locale))
                            .withStateDescriptionFragment(getStageDescription(channelID, locale)).build();
                }
                MeteringTypeEnum meteringType = getMeteringType(channelID);
                if (meteringType != null) {
                    String pattern = "%.3f kWh";

                    if (MeteringTypeEnum.CONSUMPTION.equals(meteringType)) {
                        pattern = "%d W";
                    }

                    return ChannelTypeBuilder.state(channelTypeUID, getLabelText(channelID, locale), NUMBER)
                            .withDescription(getDescText(channelID, locale)).withCategory(CATEGORY_ENERGY)
                            .withTags(
                                    new HashSet<>(Arrays.asList(getLabelText(channelID, locale), getText(DS, locale))))
                            .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withPattern(pattern)
                                    .withReadOnly(true).build())
                            .build();
                }
                try {
                    DeviceBinarayInputEnum binarayInputType = DeviceBinarayInputEnum
                            .valueOf(channelTypeUID.getId().replaceAll(BINARY_INPUT_PRE + SEPERATOR, "").toUpperCase());
                    return ChannelTypeBuilder
                            .state(channelTypeUID, getLabelText(channelID, locale), getItemType(channelID))
                            .withDescription(getDescText(channelID, locale))
                            .withCategory(getBinaryInputCategory(binarayInputType))
                            .withTags(getSimpleTags(channelTypeUID.getId(), locale)).withStateDescriptionFragment(
                                    StateDescriptionFragmentBuilder.create().withReadOnly(true).build())
                            .build();
                } catch (IllegalArgumentException e1) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link ChannelGroupTypeUID} for the given {@link SensorEnum}.
     *
     * @param sensorType (must not be null)
     * @return the channel type uid
     */
    public static ChannelTypeUID getSensorChannelUID(SensorEnum sensorType) {
        return new ChannelTypeUID(BINDING_ID, buildIdentifier(sensorType));
    }

    /**
     * Returns the {@link ChannelGroupTypeUID} for the given
     * {@link DeviceBinarayInputEnum}.
     *
     * @param binaryInputType (must not be null)
     * @return the channel type uid
     */
    public static ChannelTypeUID getBinaryInputChannelUID(DeviceBinarayInputEnum binaryInputType) {
        return new ChannelTypeUID(BINDING_ID, buildIdentifier(BINARY_INPUT_PRE, binaryInputType));
    }
}
