/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal;

import java.io.IOException;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link PlugwiseCommunicationHandler} handles all serial communication with the Plugwise Stick.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class PlugwiseCommunicationHandler {

    private final PlugwiseCommunicationContext context;
    private final PlugwiseMessageProcessor messageProcessor;
    private final PlugwiseMessageSender messageSender;

    private boolean initialized = false;

    public PlugwiseCommunicationHandler(ThingUID bridgeUID, Supplier<PlugwiseStickConfig> configurationSupplier,
            SerialPortManager serialPortManager) {
        context = new PlugwiseCommunicationContext(bridgeUID, configurationSupplier, serialPortManager);
        messageProcessor = new PlugwiseMessageProcessor(context);
        messageSender = new PlugwiseMessageSender(context);
    }

    public void addMessageListener(PlugwiseMessageListener listener) {
        context.getFilteredListeners().addListener(listener);
    }

    public void addMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        context.getFilteredListeners().addListener(listener, macAddress);
    }

    public void removeMessageListener(PlugwiseMessageListener listener) {
        context.getFilteredListeners().removeListener(listener);
    }

    public void sendMessage(Message message, PlugwiseMessagePriority priority) throws IOException {
        if (initialized) {
            messageSender.sendMessage(message, priority);
        }
    }

    public void start() throws PlugwiseInitializationException {
        try {
            context.clearQueues();
            context.initializeSerialPort();
            messageSender.start();
            messageProcessor.start();
            initialized = true;
        } catch (PlugwiseInitializationException e) {
            initialized = false;
            throw e;
        }
    }

    public void stop() {
        messageSender.stop();
        messageProcessor.stop();
        context.closeSerialPort();
        initialized = false;
    }
}
