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
package org.openhab.binding.smsmodem.internal.smslib.modem;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage.FailureCause;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage.SentStatus;
import org.openhab.binding.smsmodem.internal.smslib.modem.driver.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Poll the modem queue and send messages
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class MessageSender extends Thread {
    static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    Queue<OutboundMessage> messageQueue;

    Modem modem;

    private int gatewayDispatcherYield;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private boolean interrupt = false;

    public MessageSender(String name, Queue<OutboundMessage> messageQueue, Modem modem, int gatewayDispatcherYield) {
        setName(name);
        setDaemon(false);
        this.messageQueue = messageQueue;
        this.modem = modem;
        this.gatewayDispatcherYield = gatewayDispatcherYield;
    }

    @Override
    public void run() {
        if (!isRunning.getAndSet(true)) {
            interrupt = false; // reset interruption status
            try {
                logger.debug("Started!");
                while (!interrupt && messageQueue.size() > 0) {
                    try {
                        OutboundMessage message = messageQueue.poll();
                        if (message != null) {
                            try {
                                this.modem.send(message);
                            } catch (CommunicationException e) {
                                logger.error("Send failed!", e);
                                message.setSentStatus(SentStatus.Failed);
                                message.setFailureCause(FailureCause.None);
                            } finally {
                                this.modem.processMessageSent(message);
                                sleep(this.gatewayDispatcherYield);
                            }
                        }
                    } catch (InterruptedException e) {
                        logger.debug("Message dispatcher thread interrupted", e);
                    }
                }
                logger.debug("Ended!");
            } finally {
                this.isRunning.set(false);
            }
        }
    }

    public void setInterrupt() {
        this.interrupt = true;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
