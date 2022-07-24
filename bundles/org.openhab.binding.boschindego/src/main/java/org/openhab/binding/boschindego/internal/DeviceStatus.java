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
package org.openhab.binding.boschindego.internal;

import static java.util.Map.entry;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;

/**
 * {@link DeviceStatus} describes status codes from the device with corresponding
 * ready state and associated command.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DeviceStatus {

    private final static String STATE_PREFIX = "indego.state.";
    private final static String STATE_UNKNOWN = "unknown";

    private static final Map<Integer, DeviceStatus> STATUS_MAP = Map.ofEntries(
            entry(0, new DeviceStatus("reading-status", false, DeviceCommand.RETURN)),
            entry(257, new DeviceStatus("charging", false, DeviceCommand.RETURN)),
            entry(258, new DeviceStatus("docked", true, DeviceCommand.RETURN)),
            entry(259, new DeviceStatus("docked-software-update", false, DeviceCommand.RETURN)),
            entry(260, new DeviceStatus("docked", true, DeviceCommand.RETURN)),
            entry(261, new DeviceStatus("docked", true, DeviceCommand.RETURN)),
            entry(262, new DeviceStatus("docked-loading-map", false, DeviceCommand.MOW)),
            entry(263, new DeviceStatus("docked-saving-map", false, DeviceCommand.RETURN)),
            entry(266, new DeviceStatus("leaving-dock", false, DeviceCommand.MOW)),
            entry(513, new DeviceStatus("mowing", false, DeviceCommand.MOW)),
            entry(514, new DeviceStatus("relocalising", false, DeviceCommand.MOW)),
            entry(515, new DeviceStatus("loading-map", false, DeviceCommand.MOW)),
            entry(516, new DeviceStatus("learning-lawn", false, DeviceCommand.MOW)),
            entry(517, new DeviceStatus("paused", true, DeviceCommand.PAUSE)),
            entry(518, new DeviceStatus("border-cut", false, DeviceCommand.MOW)),
            entry(519, new DeviceStatus("idle-in-lawn", true, DeviceCommand.MOW)),
            entry(523, new DeviceStatus("spotmow", false, DeviceCommand.MOW)),
            entry(769, new DeviceStatus("returning-to-dock", false, DeviceCommand.RETURN)),
            entry(770, new DeviceStatus("returning-to-dock", false, DeviceCommand.RETURN)),
            entry(771, new DeviceStatus("returning-to-dock-battery-low", false, DeviceCommand.RETURN)),
            entry(772, new DeviceStatus("returning-to-dock-calendar-timeslot-ended", false, DeviceCommand.RETURN)),
            entry(773, new DeviceStatus("returning-to-dock-battery-temp-range", false, DeviceCommand.RETURN)),
            entry(774, new DeviceStatus("returning-to-dock", false, DeviceCommand.RETURN)),
            entry(775, new DeviceStatus("returning-to-dock-lawn-complete", false, DeviceCommand.RETURN)),
            entry(776, new DeviceStatus("returning-to-dock-relocalising", false, DeviceCommand.RETURN)),
            entry(1025, new DeviceStatus("diagnostic-mode", false, null)),
            entry(1026, new DeviceStatus("end-of-life", false, null)),
            entry(1281, new DeviceStatus("software-update", false, null)),
            entry(1537, new DeviceStatus("energy-save-mode", true, DeviceCommand.RETURN)),
            entry(64513, new DeviceStatus("docked", true, DeviceCommand.RETURN)));

    private String textKey;

    private boolean isReadyToMow;

    private @Nullable DeviceCommand associatedCommand;

    private DeviceStatus(String textKey, boolean isReadyToMow, @Nullable DeviceCommand associatedCommand) {
        this.textKey = textKey;
        this.isReadyToMow = isReadyToMow;
        this.associatedCommand = associatedCommand;
    }

    /**
     * Returns a {@link DeviceStatus} instance describing the status code.
     * 
     * @param code the status code
     * @return the {@link DeviceStatus} providing additional context for the code
     */
    public static DeviceStatus fromCode(int code) {
        DeviceStatus status = STATUS_MAP.get(code);
        if (status != null) {
            return status;
        }

        DeviceCommand command = null;
        switch (code & 0xff00) {
            case 0x100:
                command = DeviceCommand.RETURN;
                break;
            case 0x200:
                command = DeviceCommand.MOW;
                break;
            case 0x300:
                command = DeviceCommand.RETURN;
                break;
        }

        return new DeviceStatus(String.valueOf(code), false, command);
    }

    /**
     * Returns a localized description for this {@link DeviceStatus}.
     * 
     * @param translationProvider
     * @return localized status description
     */
    public String getMessage(BoschIndegoTranslationProvider translationProvider) {
        String textualState = translationProvider.getText(STATE_PREFIX + textKey);
        if (textualState == null) {
            textualState = String.format(translationProvider.getText(STATE_PREFIX + STATE_UNKNOWN, textKey), textKey);
        }
        return textualState;
    }

    public boolean isReadyToMow() {
        return isReadyToMow;
    }

    public @Nullable DeviceCommand getAssociatedCommand() {
        return associatedCommand;
    }
}
