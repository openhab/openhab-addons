package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;

public class X10MessageReceived extends BaseModemMessage {
    byte rawX10;
    byte x10Flag;

    public X10MessageReceived(byte[] data) {
        super(ModemMessageType.X10MessageReceived);
        rawX10 = data[0];
        x10Flag = data[1];
    }

    public byte getRawX10() {
        return rawX10;
    }

    public byte getX10Flag() {
        return x10Flag;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }
}
