package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

/**
 * This command adjust the brightness.
 *
 * @author Richard Lavoie
 *
 */
public class BrightnessCommand extends BasePercentCommand {

    protected BrightnessCommand(int setId) {
        super('k', 'h', setId);
    }

}
