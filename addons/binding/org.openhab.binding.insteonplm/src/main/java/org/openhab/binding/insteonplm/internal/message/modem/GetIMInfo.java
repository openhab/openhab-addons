package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

/**
 * Class wrapper for an insteon modem message received from the modem. This
 * handles both extended and standard messages being received.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public class GetIMInfo extends BaseModemMessage {
    private InsteonAddress modemAddress;
    byte category;
    byte subcategory;
    byte firmwareRevision;

    public GetIMInfo() {
        super(ModemMessageType.GetImInfo);
    }

    public GetIMInfo(byte[] data) {
        super(ModemMessageType.GetImInfo);
        modemAddress = new InsteonAddress(data[0], data[1], data[2]);
        category = data[3];
        subcategory = data[4];
        firmwareRevision = data[5];
    }

    public InsteonAddress getModemAddress() {
        return modemAddress;
    }

    public byte getCategory() {
        return category;
    }

    public byte getSubcategory() {
        return subcategory;
    }

    public byte getFirmwareRevision() {
        return firmwareRevision;
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }

}
