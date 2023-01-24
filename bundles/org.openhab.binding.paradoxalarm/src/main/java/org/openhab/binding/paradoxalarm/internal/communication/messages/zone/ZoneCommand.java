package org.openhab.binding.paradoxalarm.internal.communication.messages.zone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.paradoxalarm.internal.communication.IRequest;
import org.openhab.binding.paradoxalarm.internal.communication.RequestType;
import org.openhab.binding.paradoxalarm.internal.communication.ZoneCommandRequest;
import org.openhab.binding.paradoxalarm.internal.communication.messages.Command;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.partition.PartitionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public enum ZoneCommand implements Command {
    CLEAR_BYPASS(0),
    BYPASS(8);

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionCommand.class);

    private byte command;

    ZoneCommand(int command) {
        this.command = (byte) command;
    }

    public byte getCommand() {
        return command;
    }

    public static @Nullable ZoneCommand parse(String command) {
        try {
            return ZoneCommand.valueOf(command);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unable to parse command={}. Fallback to UNKNOWN.", command);
            return null;
        }
    }

    @Override
    public IRequest getRequest(int zoneId) {
        ZoneCommandPayload payload = new ZoneCommandPayload(zoneId, this);
        ParadoxIPPacket packet = new ParadoxIPPacket(payload.getBytes())
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
        return new ZoneCommandRequest(RequestType.ZONE_COMMAND, packet, null);
    }
}
