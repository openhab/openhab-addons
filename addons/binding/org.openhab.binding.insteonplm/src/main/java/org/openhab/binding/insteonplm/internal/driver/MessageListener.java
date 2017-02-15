package org.openhab.binding.insteonplm.internal.driver;

import org.openhab.binding.insteonplm.internal.message.Message;

/**
 * Processes the message received on the port.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public interface MessageListener {
    void processMessage(Message message);
}
