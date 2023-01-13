package org.openhab.binding.paradoxalarm.internal.communication.messages.zone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.communication.messages.partition.PartitionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public enum ZoneCommand {
    UNBYPASS(0),
    BYPASS(8);

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionCommand.class);

    private byte command;

    ZoneCommand(int command) {
        this.command = (byte) command;
    }

    public byte getCommand() {
        return command;
    }

    public static ZoneCommand parse(String command) {
        try {
            return ZoneCommand.valueOf(command);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unable to parse command={}. Fallback to UNKNOWN.", command);
            return ZoneCommand.UNBYPASS;
        }
    }
}
