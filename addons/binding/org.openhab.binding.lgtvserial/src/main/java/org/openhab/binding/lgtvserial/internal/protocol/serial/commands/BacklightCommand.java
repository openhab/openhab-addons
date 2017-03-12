package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

/**
 * This command adjust the screen backlight.
 *
 * @author Richard Lavoie
 *
 */
public class BacklightCommand extends BasePercentCommand {

    protected BacklightCommand(int setId) {
        super('m', 'g', setId);
    }

}
