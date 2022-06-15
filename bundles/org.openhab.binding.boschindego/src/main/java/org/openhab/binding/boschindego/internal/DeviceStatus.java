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
            entry(0, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(257, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(258, new DeviceStatus(true, DeviceCommand.RETURN)),
            entry(259, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(260, new DeviceStatus(true, DeviceCommand.RETURN)),
            entry(261, new DeviceStatus(true, DeviceCommand.RETURN)),
            entry(262, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(263, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(513, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(514, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(515, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(516, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(517, new DeviceStatus(true, DeviceCommand.PAUSE)),
            entry(518, new DeviceStatus(false, DeviceCommand.MOW)),
            entry(519, new DeviceStatus(true, DeviceCommand.MOW)),
            entry(769, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(770, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(771, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(772, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(773, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(774, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(775, new DeviceStatus(false, DeviceCommand.RETURN)),
            entry(776, new DeviceStatus(false, DeviceCommand.RETURN)), entry(1025, new DeviceStatus(false, null)),
            entry(1026, new DeviceStatus(false, null)), entry(1281, new DeviceStatus(false, null)),
            entry(64513, new DeviceStatus(true, DeviceCommand.RETURN)));

    private boolean isReadyToMow;

    private @Nullable DeviceCommand associatedCommand;

    private DeviceStatus(boolean isReadyToMow, @Nullable DeviceCommand associatedCommand) {
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

        return new DeviceStatus(false, command);
    }

    public boolean isReadyToMow() {
        return isReadyToMow;
    }

    public @Nullable DeviceCommand getAssociatedCommand() {
        return associatedCommand;
    }
}
