/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link OutputChannelEnum} lists all available digitalSTROM-device output
 * channels:
 * 
 * <pre>
     | ID  | Description                           | Channel Name                                                         | Min | Max   | Unit                 |
     | --- | ------------------------------------- | -------------------------------------------------------------------- | --- | ----- | -------------------- |
     | 1   | Light Brightness                      | brightness                                                           | 0   | 100   | percent              |
     | 2   | Colored Light Hue                     | hue                                                                  | 0   | 360   | degrees              |
     | 3   | Colored Light Saturation              | saturation                                                           | 0   | 100   | percent              |
     | 4   | Color Temperature                     | colortemp                                                            | 100 | 1000  | mired                |
     | 5   | Light CIE Color Model x               | x                                                                    | 0   | 10000 | scaled to 0.0 to 1.0 |
     | 6   | Light CIE Color Model y               | y                                                                    | 0   | 10000 | scaled to 0.0 to 1.0 |
     | 7   | Shade Position Outside (blinds)       | shadePositionOutside                                                 | 0   | 100   | percent              |
     | 8   | Shade Position Outside (curtains)     | shadePositionIndoor                                                  | 0   | 100   | percent              |
     | 9   | Shade Opening Angle Outside (blinds)  | shadeOpeningAngleOutside                                             | 0   | 100   | percent              |
     | 10  | Shade Opening Angle Indoor (curtains) | shadeOpeningAngleIndoor                                              | 0   | 100   | percent              |
     | 11  | Transparency (e.g. smart glass)       | transparency                                                         | 0   | 100   | percent              |
     | 12  | Air Flow Intensity                    | airFlowIntensity                                                     | 0   | 100   | percent              |
     | 13  | Air Flow Direction                    | airFlowDirection - 0=both(undefined), 1=supply, (in),2=exhaust (out) | 0   | 2     | specific             |
     | 14  | Flap Opening Angle                    | airFlapPosition                                                      | 0   | 100   | percent              |
     | 15  | Ventilation Louver Position           | airLouverPosition                                                    | 0   | 100   | percent              |
     | 16  | Heating Power                         | heatingPower                                                         | 0   | 100   | percent              |
     | 17  | Cooling Capacity                      | coolingCapacity                                                      | 0   | 100   | percent              |
     | 18  | Audio Volume (loudness)               | audioVolume                                                          | 0   | 100   | percent              |
     | 19  | Power State                           | powerState - 0=powerOff, 1=powerOn, 2=forcedOff, 3=standby           | 0   | 2     | specific             |
     | 20  | Ventilation swing mode                | airLouverAuto - 0=not active, 1=active                               | 0   | 1     | specific             |
     | 21  | Ventilation auto intensity            | airFlowAuto - 0=not active, 1=active                                 | 0   | 1     | specific             |
     | 22  | Water Temperature                     | waterTemperature                                                     | 0   | 150   | celsius              |
     | 23  | Water Flow Rate                       | waterFlow                                                            | 0   | 100   | percent              |
     | 24  | Power Level                           | powerLevel                                                           | 0   | 100   | percent              |
 * </pre>
 *
 * @author Rouven Schürch - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf</a> (Version 1.4/1.6),
 *      chapter
 *      9.1 (Output Channel Types), Table 6: Output channel types
 */
public enum OutputChannelEnum {

    BRIGHTNESS(1, "brightness"),
    HUE(2, "hue"),
    SATURATION(3, "saturation"),
    COLORTEMP(4, "colortemp"),
    X(5, "x"),
    Y(6, "y"),
    SHADE_POSITION_OUTSIDE(7, "shadePositionOutside"),
    SHADE_POSITION_INDOOR(8, "shadePositionIndoor"),
    SHADE_OPENING_ANGLE_OUTSIDE(9, "shadeOpeningAngleOutside"),
    SHADE_OPENING_ANGLE_INDOOR(10, "shadeOpeningAngleIndoor"),
    TRANSPARENCY(11, "transparency"),
    AIR_FLOW_INTENSITY(12, "airFlowIntensity"),
    AIR_FLOW_DIRECTION(13, "airFlowDirection"),
    AIR_FLAP_POSITION(14, "airFlapPosition"),
    AIR_LOUVER_POSITION(15, "airLouverPosition"),
    HEATING_POWER(16, "heatingPower"),
    COOLING_CAPACITY(17, "coolingCapacity"),
    AUDIO_VOLUME(18, "audioVolume"),
    POWER_STATE(19, "powerState"),
    AIR_LOUVER_AUTO(20, "airLouverAuto"),
    AIR_FLOW_AUTO(21, "airFlowAuto"),
    WATER_TEMPERATURE(22, "waterTemperature"),
    WATER_FLOW(23, "waterFlow"),
    POWER_LEVEL(24, "powerLevel");

    private final int channelId;

    private final String name;

    static final Map<Integer, OutputChannelEnum> OUTPUT_CHANNELS = new HashMap<>();

    static {
        for (OutputChannelEnum channels : OutputChannelEnum.values()) {
            OUTPUT_CHANNELS.put(channels.getChannelId(), channels);
        }
    }

    /**
     * Returns true, if the output channel id is contained in digitalSTROM,
     * otherwise false.
     *
     * @param channelID to be checked
     * @return true, if contains, otherwise false
     */
    public static boolean containsChannel(Integer channelID) {
        return OUTPUT_CHANNELS.keySet().contains(channelID);
    }

    /**
     * Returns the {@link OutputChannelEnum} for the given channelID, otherwise
     * null.
     *
     * @param channelID of the {@link OutputChannelEnum}
     * @return OutputChannelEnum or null
     */
    public static OutputChannelEnum getChannel(Integer channelID) {
        return OUTPUT_CHANNELS.get(channelID);
    }

    private OutputChannelEnum(int channelId, String name) {
        this.channelId = channelId;
        this.name = name;
    }

    /**
     * Returns the id of this {@link OutputChannelEnum} object.
     *
     * @return mode id
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * 
     * @return the name of this {@link OutputChannelEnum} object.
     */
    public String getName() {
        return name;
    }

    public static boolean isShadeChannel(OutputChannelEnum outputChannelEnum) {
        return outputChannelEnum == OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR
                || outputChannelEnum == OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE
                || outputChannelEnum == OutputChannelEnum.SHADE_POSITION_INDOOR
                || outputChannelEnum == OutputChannelEnum.SHADE_POSITION_OUTSIDE;
    }
}
