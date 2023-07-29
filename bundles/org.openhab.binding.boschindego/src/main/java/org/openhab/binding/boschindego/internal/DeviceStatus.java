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
package org.openhab.binding.boschindego.internal;

import static java.util.Map.entry;

import java.util.EnumSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;

/**
 * {@link DeviceStatus} describes status codes from the device with corresponding
 * characteristics and associated command.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DeviceStatus {

    private static final String STATE_PREFIX = "indego.state.";
    private static final String STATE_UNKNOWN = "unknown";

    private static final Map<Integer, DeviceStatus> STATUS_MAP = Map.ofEntries(
            entry(0, new DeviceStatus("reading-status", EnumSet.noneOf(DeviceStateAttribute.class),
                    DeviceCommand.RETURN)),
            entry(257,
                    new DeviceStatus("charging", EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.CHARGING),
                            DeviceCommand.RETURN)),
            entry(258, new DeviceStatus("docked",
                    EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.READY_TO_MOW), DeviceCommand.RETURN)),
            entry(259,
                    new DeviceStatus("docked-software-update", EnumSet.of(DeviceStateAttribute.DOCKED),
                            DeviceCommand.RETURN)),
            entry(260, new DeviceStatus("docked",
                    EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.READY_TO_MOW), DeviceCommand.RETURN)),
            entry(261, new DeviceStatus("docked",
                    EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.READY_TO_MOW), DeviceCommand.RETURN)),
            entry(262,
                    new DeviceStatus("docked-loading-map",
                            EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(263, new DeviceStatus("docked-saving-map",
                    EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.ACTIVE), DeviceCommand.RETURN)),
            entry(266,
                    new DeviceStatus("leaving-dock",
                            EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(513, new DeviceStatus("mowing", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(514, new DeviceStatus("relocalising", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(515, new DeviceStatus("loading-map", EnumSet.noneOf(DeviceStateAttribute.class), DeviceCommand.MOW)),
            entry(516, new DeviceStatus("learning-lawn", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(517, new DeviceStatus("paused", EnumSet.of(DeviceStateAttribute.READY_TO_MOW), DeviceCommand.PAUSE)),
            entry(518, new DeviceStatus("border-cut", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(519,
                    new DeviceStatus("idle-in-lawn", EnumSet.of(DeviceStateAttribute.READY_TO_MOW), DeviceCommand.MOW)),
            entry(523, new DeviceStatus("spotmow", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(524, new DeviceStatus("mowing-randomly", EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.MOW)),
            entry(768,
                    new DeviceStatus("returning-to-dock", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(769,
                    new DeviceStatus("returning-to-dock", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(770,
                    new DeviceStatus("returning-to-dock", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(771,
                    new DeviceStatus("returning-to-dock-battery-low", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(772,
                    new DeviceStatus("returning-to-dock-calendar-timeslot-ended",
                            EnumSet.of(DeviceStateAttribute.ACTIVE), DeviceCommand.RETURN)),
            entry(773,
                    new DeviceStatus("returning-to-dock-battery-temp-range", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(774,
                    new DeviceStatus("returning-to-dock", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(775, new DeviceStatus("returning-to-dock-lawn-complete",
                    EnumSet.of(DeviceStateAttribute.ACTIVE, DeviceStateAttribute.COMPLETED), DeviceCommand.RETURN)),
            entry(776,
                    new DeviceStatus("returning-to-dock-relocalising", EnumSet.of(DeviceStateAttribute.ACTIVE),
                            DeviceCommand.RETURN)),
            entry(1025, new DeviceStatus("diagnostic-mode", EnumSet.noneOf(DeviceStateAttribute.class), null)),
            entry(1026, new DeviceStatus("end-of-life", EnumSet.noneOf(DeviceStateAttribute.class), null)),
            entry(1281, new DeviceStatus("software-update", EnumSet.noneOf(DeviceStateAttribute.class), null)),
            entry(1537,
                    new DeviceStatus("energy-save-mode", EnumSet.of(DeviceStateAttribute.READY_TO_MOW),
                            DeviceCommand.RETURN)),
            entry(64513, new DeviceStatus("docked",
                    EnumSet.of(DeviceStateAttribute.DOCKED, DeviceStateAttribute.READY_TO_MOW), DeviceCommand.RETURN)));

    private String textKey;

    private EnumSet<DeviceStateAttribute> attributes;

    private @Nullable DeviceCommand associatedCommand;

    private DeviceStatus(String textKey, EnumSet<DeviceStateAttribute> attributes,
            @Nullable DeviceCommand associatedCommand) {
        this.textKey = textKey;
        this.attributes = attributes;
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
        EnumSet<DeviceStateAttribute> attributes = EnumSet.noneOf(DeviceStateAttribute.class);
        switch (code & 0xff00) {
            case 0x100:
                command = DeviceCommand.RETURN;
                break;
            case 0x200:
                command = DeviceCommand.MOW;
                attributes.add(DeviceStateAttribute.ACTIVE);
                break;
            case 0x300:
                command = DeviceCommand.RETURN;
                attributes.add(DeviceStateAttribute.ACTIVE);
                break;
        }

        return new DeviceStatus(String.valueOf(code), attributes, command);
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
        return attributes.contains(DeviceStateAttribute.READY_TO_MOW);
    }

    public boolean isActive() {
        return attributes.contains(DeviceStateAttribute.ACTIVE);
    }

    public boolean isCharging() {
        return attributes.contains(DeviceStateAttribute.CHARGING);
    }

    public boolean isDocked() {
        return attributes.contains(DeviceStateAttribute.DOCKED);
    }

    public boolean isCompleted() {
        return attributes.contains(DeviceStateAttribute.COMPLETED);
    }

    public @Nullable DeviceCommand getAssociatedCommand() {
        return associatedCommand;
    }
}
