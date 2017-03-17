package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.ModemMessageType;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;

/**
 * Class wrapper for an insteon modem message received from the modem. This
 * handles both extended and standard messages being received.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public class SendInsteonMessage extends BaseModemMessage {
    private InsteonAddress toAddress;
    private StandardInsteonMessages cmd1;
    private InsteonFlags flags;
    private Byte cmd2;
    private byte[] data;

    /**
     * Create from data right off the modem.
     *
     * @param data from the modem
     */
    public SendInsteonMessage(byte[] data) {
        super(ModemMessageType.SendInsteonMessage);
        // Incoming message.
        this.toAddress = new InsteonAddress(data[0], data[1], data[2]);
        this.flags = new InsteonFlags(data[3]);
        this.cmd1 = StandardInsteonMessages.fromByte((data[4] << 8) | data[5]);
        if (cmd1 == null) {
            this.cmd1 = StandardInsteonMessages.fromByte((data[4] << 8));
            cmd2 = data[5];
        }
        if (flags.isExtended()) {
            this.data = new byte[14];
            System.arraycopy(data, 6, this.data, 0, 14);
            setAckNackByte(data[20]);
        } else {
            setAckNackByte(data[6]);
        }
    }

    /**
     * Standard basic message.
     *
     * @param toAddress The address to send to
     * @param flags the flags
     * @param cmd1 the cmd to use
     * @param cmd2 the second cmd field
     */
    public SendInsteonMessage(InsteonAddress toAddress, InsteonFlags flags, StandardInsteonMessages cmd1, byte cmd2) {
        super(ModemMessageType.SendInsteonMessage);
        flags.setExtended(false);
        this.toAddress = toAddress;
        this.cmd1 = cmd1;
        this.cmd2 = cmd2;
        this.flags = flags;
    }

    /**
     * Standard basic message.
     *
     * @param toAddress The address to send to
     * @param flags the flags
     * @param cmd1 the cmd to use
     * @param cmd2 the second cmd field
     */
    public SendInsteonMessage(InsteonAddress toAddress, InsteonFlags flags, StandardInsteonMessages cmd1) {
        super(ModemMessageType.SendInsteonMessage);
        flags.setExtended(false);
        this.toAddress = toAddress;
        this.cmd1 = cmd1;
        this.flags = flags;
    }

    /**
     * Standard extended message.
     *
     * @param toAddress The address to send to
     * @param flags the flags
     * @param cmd1 the cmd to use
     * @param data the data to send to the client
     */
    public SendInsteonMessage(InsteonAddress toAddress, InsteonFlags flags, StandardInsteonMessages cmd1, byte[] data) {
        super(ModemMessageType.SendInsteonMessage);
        assert (data.length == 14);
        flags.setExtended(true);
        this.toAddress = toAddress;
        this.cmd2 = null;
        this.flags = flags;
    }

    /**
     * Extended message with override on the cmd2 param with extra data.
     *
     * @param toAddress The address to send to
     * @param flags the flags
     * @param cmd1 the cmd to use
     * @param cmd2 the second cmd field
     * @param data the data to send to the client
     */
    public SendInsteonMessage(InsteonAddress toAddress, InsteonFlags flags, StandardInsteonMessages cmd1, byte cmd2,
            byte[] data) {
        super(ModemMessageType.SendInsteonMessage);
        assert (data.length == 14);
        flags.setExtended(true);
        this.toAddress = toAddress;
        this.cmd1 = cmd1;
        this.cmd2 = cmd2;
        this.flags = flags;
        this.data = data;
    }

    @Override
    public byte[] getPayload() {
        if (flags.isExtended()) {
            byte[] payload = new byte[20];
            payload[0] = toAddress.getHighByte();
            payload[1] = toAddress.getMiddleByte();
            payload[2] = toAddress.getLowByte();
            payload[3] = flags.getByte();
            payload[4] = cmd1.getCmd1();
            if (cmd2 != null) {
                payload[5] = cmd2;
            } else {
                payload[5] = cmd1.getCmd2();
            }
            System.arraycopy(this.data, 0, payload, 6, 14);
            return payload;
        } else {
            byte[] payload = new byte[6];
            payload[0] = toAddress.getHighByte();
            payload[1] = toAddress.getMiddleByte();
            payload[2] = toAddress.getLowByte();
            payload[3] = flags.getByte();
            payload[4] = cmd1.getCmd1();
            if (cmd2 == null) {
                payload[5] = cmd1.getCmd2();
            } else {
                payload[5] = cmd2;
            }
            return payload;
        }
    }

    public InsteonAddress getToAddress() {
        return toAddress;
    }

    public StandardInsteonMessages getCmd1() {
        return cmd1;
    }

    public InsteonFlags getFlags() {
        return flags;
    }

    public Byte getCmd2() {
        return cmd2;
    }

    public byte[] getData() {
        return data;
    }

    public void setCRC() {
        // TODO Auto-generated method stub

    }

    public void setCRC2() {
        // TODO Auto-generated method stub

    }

    // 10 seconds
    public long getDirectAckTimeout() {
        return 10000;
    }
}
