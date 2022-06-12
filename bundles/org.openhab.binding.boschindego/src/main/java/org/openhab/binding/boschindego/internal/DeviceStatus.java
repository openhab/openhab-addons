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

    private static final Map<Integer, DeviceStatus> STATUS_MAP = Map.ofEntries(
            entry(0, new DeviceStatus("Reading status", false, DeviceCommand.RETURN)),
            entry(257, new DeviceStatus("Charging", false, DeviceCommand.RETURN)),
            entry(258, new DeviceStatus("Docked", true, DeviceCommand.RETURN)),
            entry(259, new DeviceStatus("Docked - Software update", false, DeviceCommand.RETURN)),
            entry(260, new DeviceStatus("Docked", true, DeviceCommand.RETURN)),
            entry(261, new DeviceStatus("Docked", true, DeviceCommand.RETURN)),
            entry(262, new DeviceStatus("Docked - Loading map", false, DeviceCommand.MOW)),
            entry(263, new DeviceStatus("Docked - Saving map", false, DeviceCommand.RETURN)),
            entry(513, new DeviceStatus("Mowing", false, DeviceCommand.MOW)),
            entry(514, new DeviceStatus("Relocalising", false, DeviceCommand.MOW)),
            entry(515, new DeviceStatus("Loading map", false, DeviceCommand.MOW)),
            entry(516, new DeviceStatus("Learning lawn", false, DeviceCommand.MOW)),
            entry(517, new DeviceStatus("Paused", true, DeviceCommand.PAUSE)),
            entry(518, new DeviceStatus("Border cut", false, DeviceCommand.MOW)),
            entry(519, new DeviceStatus("Idle in lawn", true, DeviceCommand.MOW)),
            entry(769, new DeviceStatus("Returning to dock", false, DeviceCommand.RETURN)),
            entry(770, new DeviceStatus("Returning to dock", false, DeviceCommand.RETURN)),
            entry(771, new DeviceStatus("Returning to dock - Battery low", false, DeviceCommand.RETURN)),
            entry(772, new DeviceStatus("Returning to dock - Calendar timeslot ended", false, DeviceCommand.RETURN)),
            entry(773, new DeviceStatus("Returning to dock - Battery temp range", false, DeviceCommand.RETURN)),
            entry(774, new DeviceStatus("Returning to dock", false, DeviceCommand.RETURN)),
            entry(775, new DeviceStatus("Returning to dock - Lawn complete", false, DeviceCommand.RETURN)),
            entry(776, new DeviceStatus("Returning to dock - Relocalising", false, DeviceCommand.RETURN)),
            entry(1025, new DeviceStatus("Diagnostic mode", false, null)),
            entry(1026, new DeviceStatus("End of life", false, null)),
            entry(1281, new DeviceStatus("Software update", false, null)),
            entry(64513, new DeviceStatus("Docked", true, DeviceCommand.RETURN)));

    private String message;

    private boolean isReadyToMow;

    private @Nullable DeviceCommand associatedCommand;

    private DeviceStatus(String message, boolean isReadyToMow, @Nullable DeviceCommand associatedCommand) {
        this.message = message;
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

        return new DeviceStatus(String.format("Unknown status code %d", code), false, command);
    }

    public String getMessage() {
        return message;
    }

    public boolean isReadyToMow() {
        return isReadyToMow;
    }

    public @Nullable DeviceCommand getAssociatedCommand() {
        return associatedCommand;
    }
}
