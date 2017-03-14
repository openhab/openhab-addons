package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;
import org.openhab.binding.insteonplm.internal.message.X10Command;

/**
 * The X10 message sent over the wire.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class SendX10Message extends BaseModemMessage {
    private byte houseCode;
    private byte keyCode;
    private X10Command cmd;
    private boolean isCommand;

    public SendX10Message(byte[] data) {
        super(ModemMessageType.SendX10);
        houseCode = (byte) (data[0] >> 4);
        if ((data[1] & 0x80) == 0) {
            int pos = data[0] & 0xf;
            isCommand = true;
            cmd = X10Command.values()[pos];
        } else {
            isCommand = false;
            keyCode = (byte) (data[0] & 0xf);
        }
        setAckNackByte(data[2]);
    }

    public SendX10Message(X10Command cmd, byte houseCode) {
        super(ModemMessageType.SendX10);
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

    public byte getHouseCode() {
        return houseCode;
    }

    public byte getKeyCode() {
        return keyCode;
    }

    public X10Command getCmd() {
        return cmd;
    }

    public boolean isCommand() {
        return isCommand;
    }

    @Override
    public byte[] getPayload() {
        byte[] data = new byte[2];
        if (isCommand) {
            data[0] = (byte) ((houseCode << 4) | (cmd.ordinal()));
            data[1] = (byte) 0x80;
        } else {
            data[0] = (byte) ((houseCode << 4) | keyCode);
            data[1] = 0;
        }
        return data;
    }
}
