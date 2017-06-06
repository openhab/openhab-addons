package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

/**
 * This handle the all linking records.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @author David Bennett - Updated
 */
public class GetNextAllLinkingRecord extends BaseModemMessage {
    public GetNextAllLinkingRecord() {
        super(ModemMessageType.GetNextAllLinkRecord);
    }

    public GetNextAllLinkingRecord(byte[] data) {
        super(ModemMessageType.GetNextAllLinkRecord);
        setAckNackByte(data[0]);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }

}
