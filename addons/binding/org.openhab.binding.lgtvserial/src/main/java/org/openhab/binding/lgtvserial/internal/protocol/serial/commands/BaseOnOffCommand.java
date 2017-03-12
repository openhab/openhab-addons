package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This command is the base command for the On/Off type command which translates to 00/01 on the wire.
 *
 * @author Richard Lavoie
 *
 */
public abstract class BaseOnOffCommand extends BaseLGSerialCommand {

    protected BaseOnOffCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return data == OnOffType.ON ? "01" : "00";
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        return new OnOffResponse(set, success, data);
    }

}
