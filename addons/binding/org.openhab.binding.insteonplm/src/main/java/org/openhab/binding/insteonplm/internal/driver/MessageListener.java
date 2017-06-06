package org.openhab.binding.insteonplm.internal.driver;

import org.openhab.binding.insteonplm.internal.message.modem.BaseModemMessage;

/**
 * Processes the message received on the port.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public interface MessageListener {
    void processMessage(BaseModemMessage msg);
}
