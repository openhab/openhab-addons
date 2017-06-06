package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

/**
 * Class wrapper for an insteon modem message received from the modem. This
 * handles both extended and standard messages being received.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public abstract class BaseModemMessage {
    private final ModemMessageType messageType;
    private long quietTime = 0;
    private byte ackNackByte = 0;
    public static final int ACK_MESSAGE = 0x06;
    public static final int NACK_MESSAGE = 0x15;

    public BaseModemMessage(ModemMessageType messageType) {
        this.messageType = messageType;
    }

    public ModemMessageType getMessageType() {
        return messageType;
    }

    public abstract byte[] getPayload();

    public long getQuietTime() {
        // TODO Auto-generated method stub
        return quietTime;
    }

    public void setQuietTime(long quietTime) {
        // TODO Auto-generated method stub
        this.quietTime = quietTime;
    }

    protected void setAckNackByte(byte data) {
        ackNackByte = data;
    }

    public boolean isAck() {
        return ackNackByte == ACK_MESSAGE;
    }

    public boolean isNack() {
        return ackNackByte == NACK_MESSAGE;
    }

    /**
     * Check and see if they are equal.
     *
     * @param mess
     * @return
     */
    public boolean equals(BaseModemMessage mess) {
        if (mess.getMessageType() == messageType) {
            return true;
        }
        return false;
    }
}
