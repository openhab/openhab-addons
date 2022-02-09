/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smsmodem.internal.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smsmodem.internal.smslib.message.DeliveryReportMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.InboundMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage;

/**
 *
 * Interface to implement to get messages and reports
 *
 * @author Gwendal ROULLEAU - Initial contribution
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
