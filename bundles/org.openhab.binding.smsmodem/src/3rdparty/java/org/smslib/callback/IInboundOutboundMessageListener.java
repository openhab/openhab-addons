package org.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.InboundMessage;
import org.smslib.message.OutboundMessage;

/**
 *
 * Interface to implement to get messages and reports
 *
 * Extracted from SMSLib
 */
@NonNullByDefault
public interface IInboundOutboundMessageListener {

    /**
     * Implement this method to get incoming messages
     *
     * @param message The inbound message received
     */
    public void messageReceived(InboundMessage message);

    /**
     * Implement this method to get warned when
     * a message is sent on the network
     *
     * @param message the message sent
     */
    public void messageSent(OutboundMessage message);

    /**
     * Implement this method to get warned when
     * a message previously sent is received by the recipient
     *
     * @param message the delivery report message
     */
    public void messageDelivered(DeliveryReportMessage message);
}
