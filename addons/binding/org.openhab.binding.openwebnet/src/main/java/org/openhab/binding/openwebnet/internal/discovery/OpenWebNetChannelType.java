/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.exception.InvalidTypeException;

/**
 *
 * @author Antoine Laydier
 *
 */
public enum OpenWebNetChannelType {
    // Scenario
    SCENARIO_CONTROL(2, "Scenario Control", null),
    MULTIFONCTION_SCENARIO_CONTROL(273, "Multifonction Scenario Control", null),
    // Lighting
    ON_OFF_SWITCH(256, "On/Off Switch", OpenWebNetBindingConstants.SWITCH_CHANNEL_TYPE_UID),
    DIMMER_CONTROL(257, "Dimmer Control", null),
    DIMMER_SWITCH(258, "Dimmer Switch", OpenWebNetBindingConstants.BRIGHTNESS_CHANNEL_TYPE_UID),
    SCS_ON_OFF_SWITCH(261, "SCS On/Off Switch", OpenWebNetBindingConstants.SWITCH_CHANNEL_TYPE_UID),
    SCS_DIMMER_CONTROL(262, "SCS Dimmer Control", null),
    SCS_DIMMER_SWITCH(263, "SCS Dimmer Switch", OpenWebNetBindingConstants.BRIGHTNESS_CHANNEL_TYPE_UID),
    WATERPROOF_1_GANG_SWITCH(264, "Waterproof Gang Switch", null),
    ON_OFF_CONTROL(274, "On/Off Control", null),
    AUXILLIARY_ON_OFF_1_GANG_SWITCH(275, "Auxilliary On/Off Gang Switch", null),

    // unmanaged lighting
    SWITCH_MOTION_DETECTOR(259, "Motion Detector", null),
    DAYLIGHT_SENSOR(260, "Daylight Sensor", null),
    MOTION_DETECTOR(267, "Motion Detector", null),
    SWITCH_MOTION_DETECTOR_2(269, "Switch Motion Detector II", null),
    MOTION_DETECTOR_2(270, "Motion Dector II", null),
    TOGGLE_CONTROL(266, "Toggle Control", null),
    AUXILLIARY_TOGGLE_CONTROL(271, "Auxilliary Toggle Control", null),
    SCS_AUXILLIARY_TOGGLE_CONTROL(272, "SCS Auxilliary Toggle Control", null),
    AUTOMATIC_DIMMER_SWITCH(265, "Automatic Dimmer Switch", null),

    // Automation
    SHUTTER_CONTROL(512, "Shutter Control", null),
    SHUTTER_SWITCH(513, "Shutter Switch", null),
    SCS_SHUTTER_CONTROL(514, "SCS Shutter Control", null),
    SCS_SHUTTER_SWITCH(515, "SCS Shutter Switch", null),

    // Interface
    SCS_1_SYSTEM_1_4_GATEWAY(1024, "SCS 1 System 1-4 Gateway", null),
    SCS_2_SYSTEM_1_4_GATEWAY(1025, "SCS 2 System 1-4 Gateway", null),
    NETWORK_REPEATER(1029, "Network Repeater", null),
    OPENWEBNET_INTERFACE(1030, "OpenWebNet Interface", null),
    // Video
    VIDEO_SWITCHER(1536, "Video Switcher", null),
    // Unknown
    UNKNOWN(-1, "Unknown Type of Device", null);

    private static HashMap<Integer, OpenWebNetChannelType> map = new HashMap<Integer, OpenWebNetChannelType>();

    static {
        for (OpenWebNetChannelType ls : OpenWebNetChannelType.values()) {
            map.put(ls.value, ls);
        }
    }
    private int value;
    private @NonNull String text;
    private @Nullable ChannelTypeUID type;

    private OpenWebNetChannelType(int value, @NonNull String text, @Nullable ChannelTypeUID type) {
        this.value = value;
        this.text = text;
        this.type = type;
    }

    public static @NonNull OpenWebNetChannelType getType(int value) {
        @Nullable
        OpenWebNetChannelType out = map.get(value);
        if (out == null) {
            out = UNKNOWN;
        }
        return out;
    }

    private static boolean isLighting(@NonNull OpenWebNetChannelType type) {
        boolean state;
        switch (type) {
            case ON_OFF_SWITCH:
            case ON_OFF_CONTROL:
            case DIMMER_CONTROL:
            case DIMMER_SWITCH:
            case SCS_DIMMER_CONTROL:
            case SCS_DIMMER_SWITCH:
            case SCS_ON_OFF_SWITCH:
            case WATERPROOF_1_GANG_SWITCH:
            case AUXILLIARY_ON_OFF_1_GANG_SWITCH:
                state = true;
                break;
            default:
                state = false;
        }
        return state;
    }

    private static boolean isAutomation(@NonNull OpenWebNetChannelType type) {
        boolean state;
        switch (type) {
            // case SHUTTER_CONTROL:
            case SHUTTER_SWITCH:
                // case SCS_SHUTTER_CONTROL:
            case SCS_SHUTTER_SWITCH:
                state = true;
                break;
            default:
                state = false;
        }
        return state;
    }

    public boolean isLighting() {
        return OpenWebNetChannelType.isLighting(this);
    }

    public boolean isAutomation() {
        return OpenWebNetChannelType.isAutomation(this);
    }

    @Override
    public @NonNull String toString() {
        return text;
    }

    public int getValue() {
        return value;
    }

    public ChannelTypeUID getType() throws InvalidTypeException {
        if (type == null) {
            throw new InvalidTypeException();
        }
        return type;
    }

    public boolean isDimmer() {
        return OpenWebNetBindingConstants.BRIGHTNESS_CHANNEL_TYPE_UID.equals(type);
    }

}