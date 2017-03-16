package org.openhab.binding.insteonplm.internal.message.modem;

import org.openhab.binding.insteonplm.internal.message.ModemMessageType;
import org.openhab.binding.insteonplm.internal.message.X10Command;

/**
 * The X10 message received over the wire.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class X10MessageReceived extends BaseModemMessage {
    private byte houseCode;
    private byte keyCode;
    private X10Command cmd;
    private boolean isCommand;

    public X10MessageReceived(byte[] data) {
        super(ModemMessageType.X10MessageReceived);
        houseCode = (byte) (data[0] >> 4);
        if ((data[1] & 0x80) == 0) {
            int pos = data[0] & 0xf;
            isCommand = true;
            cmd = X10Command.values()[pos];
        } else {
            isCommand = false;
            keyCode = (byte) (data[0] & 0xf);
        }
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
        return null;
    }
}
