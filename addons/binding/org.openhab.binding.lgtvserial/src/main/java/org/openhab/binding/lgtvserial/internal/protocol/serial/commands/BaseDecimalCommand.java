package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This command is the base command to handle percent type commands (0-100) in hex format on the wire.
 *
 * @author Richard Lavoie
 *
 */
public abstract class BaseDecimalCommand extends BaseLGSerialCommand {

    /**
     * Create a command.
     *
     * @param command1 Command category
     * @param command2 Command key
     * @param setId TV Set id this command is tied to
     * @param updatable Define if this command is one that can update the TV or can only ever be a read status command.
     */
    protected BaseDecimalCommand(char command1, char command2, int setId, boolean updatable) {
        super(command1, command2, setId, updatable);
    }

    /**
     * Create a command that can update the TV.
     *
     * @param command1 Command category
     * @param command2 Command key
     * @param setId TV Set id this command is tied to
     */

    protected BaseDecimalCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return String.format("%02x", ((PercentType) data).intValue());
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        String decimalValue = Integer.toString(Integer.parseInt(data, 16));
        return new DecimalResponse(set, success, DecimalType.valueOf(decimalValue));
    }

}
