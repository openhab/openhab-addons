package org.openhab.binding.paradoxalarm.internal.communication.messages.zone;

import java.nio.ByteBuffer;

import org.openhab.binding.paradoxalarm.internal.communication.messages.IPayload;

public class ZoneCommandPayload implements IPayload {

    private static final int BYTES_LENGTH = 15;

    private static final byte MESSAGE_START = 0x40;
    private static final byte PAYLOAD_SIZE = 0x0f;
    private static final byte[] EMPTY_FOUR_BYTES = { 0, 0, 0, 0 };
    private static final byte CHECKSUM = 0;

    private int zoneNumber;
    private ZoneCommand command;

    ZoneCommandPayload(int zoneNumber, ZoneCommand command) {
        this.zoneNumber = zoneNumber;
        this.command = command;
    }

    @Override
    public byte[] getBytes() {
        byte[] bufferArray = new byte[BYTES_LENGTH];
        ByteBuffer buf = ByteBuffer.wrap(bufferArray);
        buf.put(MESSAGE_START);
        buf.put(PAYLOAD_SIZE);
        buf.put(EMPTY_FOUR_BYTES);
        buf.put(calculateMessageBytes());
        buf.put(EMPTY_FOUR_BYTES);
        buf.put(CHECKSUM);
        return bufferArray;
    }

    private ByteBuffer calculateMessageBytes() {
        // TODO Auto-generated method stub
        return null;
    }

}
