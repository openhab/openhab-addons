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
     *
     * From http://www.madreporite.com/insteon/plm_basics.html
     * 100 = Broadcast Message
     *
     * 000 = Direct Message
     * 001 = ACK of Direct Message
     * 101 = NAK of Direct Message
     *
     * 110 = Group Broadcast Message
     * 010 = Group Cleanup Direct Message
     * 011 = ACK of Group Cleanup Direct Message
     * 111 = NAK of Group Cleanup Direct Message
     */
    public enum MessageType {
        BroadcastMessage(4),
        DirectMessage(0),
        AckOfDirect(1),
        NackOfDirect(5),
        GroupBroadcastMessage(6),
        GroupCleanupDirectMessage(2),
        AckOfGroupCleanupDirectMessage(3),
        NackOfGroupCleanupDirectMessage(7);
        private int val;

        MessageType(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public static MessageType fromInt(int val) {
            for (MessageType mess : MessageType.values()) {
                if (mess.getVal() == val) {
                    return mess;
                }
            }
            return null;
        }
    }

    private MessageType type;
    private boolean extended;
    private byte maxHops;
    private byte maxHopsLeft;

    public InsteonFlags(byte input) {
        this.maxHops = (byte) (input & 0x3);
        this.maxHopsLeft = (byte) ((input >> 2) & 0x3);
        this.extended = (input & 0x10) != 0;
        this.type = MessageType.fromInt(input >> 5);
    }

    /**
     * Create the flags for sending a message with the default pieces.
     *
     * @param extended If we are sending an extended message or not
     */
    public InsteonFlags() {
        this.type = MessageType.DirectMessage;
        this.extended = false;
        this.maxHops = 0x3;
        this.maxHopsLeft = 0x3;
    }

    /**
     * @return Turns the flags into a byte to send.
     */
    public byte getByte() {
        return (byte) ((this.type.getVal() << 5) | (extended ? 0x10 : 0) | ((this.maxHopsLeft & 0x3) << 2)
                | (this.maxHops & 0x3));
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
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

    public MessageType getMessageType() {
        return this.type;
    }

    public void setMessageType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InsteonFlags [type=");
        builder.append(type.toString());
        builder.append(", extended=");
        builder.append(extended);
        builder.append(", maxHops=");
        builder.append(maxHops);
        builder.append(", maxHopsLeft=");
        builder.append(maxHopsLeft);
        builder.append("]");
        return builder.toString();
    }
}
