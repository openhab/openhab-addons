package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.AllLinkRecordFlags;
import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

/**
 * This handle the all linking records.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public class AllLinkRecordResponse extends BaseModemMessage {
    private final InsteonAddress address;
    private final AllLinkRecordFlags flags;
    private final byte group;
    private final byte[] linkData;

    public AllLinkRecordResponse(byte[] data) {
        super(ModemMessageType.AllLinkRecordResponse);
        this.flags = new AllLinkRecordFlags(data[0]);
        this.group = data[1];
        this.address = new InsteonAddress(data[2], data[3], data[4]);
        this.linkData = new byte[3];
        System.arraycopy(data, 5, this.linkData, 0, 3);
    }

    public InsteonAddress getAddress() {
        return address;
    }

    public AllLinkRecordFlags getFlags() {
        return flags;
    }

    public byte getGroup() {
        return group;
    }

    public byte[] getLinkData() {
        return linkData;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }
}
