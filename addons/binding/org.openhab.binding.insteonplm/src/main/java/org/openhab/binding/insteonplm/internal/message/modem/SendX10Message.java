package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;
import org.openhab.binding.insteonplm.internal.message.X10Command;

public class SendX10Message extends BaseModemMessage {
    private byte houseCode;
    private byte keyCode;
    private X10Command cmd;
    private boolean isCommand;

    public SendX10Message(byte[] data) {
        super(ModemMessageType.SendX10);
        rawX10 = data[0];
        x10Flags = data[1];
        setAckNackByte(data[2]);
    }

    public SendX10Message(X10Command cmd, byte houseCode) {
        this.cmd = cmd;
        this.houseCode = houseCode;
        this.isCommand = true;
    }

    public SendX10Message(byte houseCode, byte keyCode) {
        super(ModemMessageType.SendX10);
        this.houseCode = houseCode;
        this.keyCode = keyCode;
        this.isCommand = false;
    }

    public byte getRawX10() {
        return rawX10;
    }

    public void setRawX10(byte rawX10) {
        this.rawX10 = rawX10;
    }

    public byte getX10Flags() {
        return x10Flags;
    }

    public void setX10Flags(byte x10Flags) {
        this.x10Flags = x10Flags;
    }

    @Override
    public byte[] getPayload() {
        byte[] data = new byte[2];
        data[0] = rawX10;
        data[1] = x10Flags;
        return data;
    }
}
