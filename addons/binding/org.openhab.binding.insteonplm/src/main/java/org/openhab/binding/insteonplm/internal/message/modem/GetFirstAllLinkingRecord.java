package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

/**
 * This handle the all linking records.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public class GetFirstAllLinkingRecord extends BaseModemMessage {
    public GetFirstAllLinkingRecord() {
        super(ModemMessageType.GetFirstAllLinkRecord);
    }

    public GetFirstAllLinkingRecord(byte[] data) {
        super(ModemMessageType.GetFirstAllLinkRecord);
        setAckNackByte(data[0]);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
}
