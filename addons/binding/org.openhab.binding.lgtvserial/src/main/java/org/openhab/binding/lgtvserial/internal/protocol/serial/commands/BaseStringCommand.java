package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This command is the base command for the On/Off type command which translates to 00/01 on the wire.
 *
 * @author Richard Lavoie
 *
 */
public abstract class BaseStringCommand extends BaseLGSerialCommand {

    protected BaseStringCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    public BaseStringCommand(char command1, char command2, int setId, boolean updatable) {
        super(command1, command2, setId, updatable);
    }

    @Override
    public LGSerialResponse parseResponse(String response) {
        int set = Integer.parseInt(response.substring(2, 4), 16);
        boolean success = 'O' == response.charAt(5) && 'K' == response.charAt(6);

        return new StringResponse(set, success, response.substring(7));
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return data.toString();
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        return new StringResponse(set, success, data);
    }

}
