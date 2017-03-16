package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.device.X10Address;
import org.openhab.binding.insteonplm.internal.message.ModemMessageType;
import org.openhab.binding.insteonplm.internal.message.X10Command;

/**
 * The X10 message sent over the wire.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class SendX10Message extends BaseModemMessage {
    private X10Address address;
    private X10Command cmd;
    private boolean isCommand;

    public SendX10Message(byte[] data) {
        super(ModemMessageType.SendX10);
        byte houseCode = (byte) (data[0] >> 4);
        if ((data[1] & 0x80) == 0) {
            int pos = data[0] & 0xf;
            isCommand = true;
            cmd = X10Command.values()[pos];
            this.address = new X10Address(houseCode, (byte) 0);
        } else {
            isCommand = false;
            byte keyCode = (byte) (data[0] & 0xf);
            this.address = new X10Address(houseCode, keyCode);
        }
        setAckNackByte(data[2]);
    }

    public SendX10Message(X10Command cmd, byte houseCode) {
        super(ModemMessageType.SendX10);
        this.cmd = cmd;
        this.address = new X10Address(houseCode, (byte) 0);
        this.isCommand = true;
    }

    public SendX10Message(byte houseCode, byte keyCode) {
        super(ModemMessageType.SendX10);
        this.address = new X10Address(houseCode, keyCode);
        this.isCommand = false;
    }

    public SendX10Message(X10Address address) {
        super(ModemMessageType.SendX10);
        this.address = address;
        this.isCommand = false;
    }

    public X10Address getAddress() {
        return address;
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
            data[0] = (byte) ((this.getAddress().getHouseCode() << 4) | (this.getCmd().ordinal()));
            data[1] = (byte) 0x80;
        } else {
            data[0] = (byte) ((this.getAddress().getHouseCode() << 4) | this.getAddress().getUnitCode());
            data[1] = 0;
        }
        return data;
    }
}
