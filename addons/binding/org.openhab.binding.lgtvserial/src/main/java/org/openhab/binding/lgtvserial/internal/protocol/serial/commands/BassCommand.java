package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

/**
 * This command adjust the bass.
 *
 * @author Richard Lavoie
 *
 */
public class BassCommand extends BasePercentCommand {

    protected BassCommand(int setId) {
        super('k', 's', setId);
    }

}
