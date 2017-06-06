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
public class StandardMessageReceived extends BaseModemMessage {
    private final InsteonAddress fromAddress;
    private final InsteonAddress toAddress;
    private final InsteonFlags flags;
    private final StandardInsteonMessages cmd1;
    private final Byte cmd2;
    private final byte[] data;

    public StandardMessageReceived(byte[] data) {
        super(ModemMessageType.StandardMessageReceived);

        fromAddress = new InsteonAddress(data[0], data[1], data[2]);
        toAddress = new InsteonAddress(data[3], data[4], data[5]);
        flags = new InsteonFlags(data[6]);
        if (flags.isExtended() || data.length == 25) {
            this.data = new byte[14];
            StandardInsteonMessages tmp = StandardInsteonMessages.fromByte((data[7] << 8) | data[8]);
            if (tmp == null) {
                tmp = StandardInsteonMessages.fromByte((data[7] << 8));
            }
            cmd2 = data[8];
            cmd1 = tmp;
            System.arraycopy(data, 9, this.data, 0, 14);
        } else {
            StandardInsteonMessages tmp = StandardInsteonMessages.fromByte((data[7] << 8) | data[8]);
            if (tmp == null) {
                tmp = StandardInsteonMessages.fromByte((data[7] << 8));
            }
            cmd1 = tmp;
            cmd2 = data[8];
            this.data = new byte[0];
        }
    }

    public InsteonAddress getFromAddress() {
        return fromAddress;
    }

    public InsteonAddress getToAddress() {
        return toAddress;
    }

    public InsteonFlags getFlags() {
        return flags;
    }

    public StandardInsteonMessages getCmd1() {
        return cmd1;
    }

    public byte getCmd2() {
        return cmd2;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }
}
