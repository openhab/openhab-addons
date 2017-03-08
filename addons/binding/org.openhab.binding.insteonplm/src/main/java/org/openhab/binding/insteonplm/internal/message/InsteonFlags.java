package org.openhab.binding.insteonplm.internal.message;

public class InsteonFlags {
    /*
     * From the official Insteon docs: the message flags are as follows:
     *
     * Bit 0 max hops low bit
     * Bit 1 max hops high bit
     * Bit 2 hops left low bit
     * Bit 3 hops left high bit
     * Bit 4 0: is standard message, 1: is extended message
     * Bit 5 ACK
     * Bit 6 0: not link related, 1: is ALL-Link message
     * Bit 7 Broadcast/NAK
     */
    private boolean broadcast;
    private boolean extended;
    private boolean group;
    private boolean acknowledge;
    private byte maxHops;
    private byte maxHopsLeft;

    public InsteonFlags(byte input) {

    }

    /**
     * Create the flags for sending a message with the default pieces.
     *
     * @param extended If we are sending an extended message or not
     */
    public InsteonFlags() {
        this.broadcast = false;
        this.extended = false;
        this.group = false;
        this.acknowledge = false;
        this.maxHops = 0x3;
        this.maxHopsLeft = 0x3;
    }

    /**
     * @return Turns the flags into a byte to send.
     */
    public byte getByte() {
        return (byte) ((broadcast ? 0x80 : 0) | (group ? 0x40 : 0) | (acknowledge ? 0x20 : 0) | (extended ? 0x10 : 0)
                | ((this.maxHopsLeft & 0x3) << 2) | (this.maxHops & 0x3));
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isAcknowledge() {
        return acknowledge;
    }

    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }

    public byte getMaxHops() {
        return maxHops;
    }

    public void setMaxHops(byte maxHops) {
        this.maxHops = maxHops;
    }

    public byte getMaxHopsLeft() {
        return maxHopsLeft;
    }

    public void setMaxHopsLeft(byte maxHopsLeft) {
        this.maxHopsLeft = maxHopsLeft;
    }

}
