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
package org.openhab.binding.automower.internal.things;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.core.thing.ChannelUID;

/**
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public enum AutomowerCommand {
    START("Start", AutomowerBindingConstants.CHANNEL_COMMAND_START),
    START_IN_WORK_AREA("StartInWorkArea", AutomowerBindingConstants.CHANNEL_COMMAND_START_IN_WORK_AREA),
    RESUME_SCHEDULE("ResumeSchedule", AutomowerBindingConstants.CHANNEL_COMMAND_RESUME_SCHEDULE),
    PAUSE("Pause", AutomowerBindingConstants.CHANNEL_COMMAND_PAUSE),
    PARK("Park", AutomowerBindingConstants.CHANNEL_COMMAND_PARK),
    PARK_UNTIL_NEXT_SCHEDULE("ParkUntilNextSchedule",
            AutomowerBindingConstants.CHANNEL_COMMAND_PARK_UNTIL_NEXT_SCHEDULE),
    PARK_UNTIL_FURTHER_NOTICE("ParkUntilFurtherNotice", AutomowerBindingConstants.CHANNEL_COMMAND_PARK_UNTIL_NOTICE);

    private static final Map<String, AutomowerCommand> CHANNEL_TO_CMD_MAP = new HashMap<>();

    static {
        EnumSet.allOf(AutomowerCommand.class).forEach(cmd -> CHANNEL_TO_CMD_MAP.put(cmd.getChannel(), cmd));
    }

    private final String command;
    private final String channel;

    AutomowerCommand(String command, String channel) {
        this.command = command;
        this.channel = channel;
    }

    public static Optional<AutomowerCommand> fromChannelUID(ChannelUID channelUID) {
        return Optional.ofNullable(CHANNEL_TO_CMD_MAP.get(channelUID.getId()));
    }

    public String getCommand() {
        return command;
    }

    public String getChannel() {
        return channel;
    }
}
