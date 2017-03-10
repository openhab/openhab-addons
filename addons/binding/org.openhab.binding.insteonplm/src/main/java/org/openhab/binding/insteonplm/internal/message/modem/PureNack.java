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
public class PureNack extends BaseModemMessage {
    public PureNack() {
        super(ModemMessageType.PureNack);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
}
