package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

/**
 * This command adjust the balance.
 *
 * @author Richard Lavoie
 *
 */
public class BalanceCommand extends BasePercentCommand {

    protected BalanceCommand(int setId) {
        super('k', 't', setId);
    }

}
