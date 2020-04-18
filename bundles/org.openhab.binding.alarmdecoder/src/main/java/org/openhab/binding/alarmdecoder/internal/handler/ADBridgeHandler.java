/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.alarmdecoder.internal.handler;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.alarmdecoder.internal.AlarmDecoderDiscoveryService;
import org.openhab.binding.alarmdecoder.internal.actions.BridgeActions;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMsgType;
import org.openhab.binding.alarmdecoder.internal.protocol.EXPMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.KeypadMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.LRRMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.RFXMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.VersionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for bridge handlers responsible for communicating with the Nu Tech Alarm Decoder devices.
 * Based partly on and including code from the original OH1 alarmdecoder binding by Bernd Pfrommer.
 *
 * @author Bernd Pfrommer - Initial contribution (OH1 version)
 * @author Bob Adair - Re-factored into OH2 binding
 */
@NonNullByDefault
public abstract class ADBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ADBridgeHandler.class);

    protected @Nullable BufferedReader reader = null;
    protected @Nullable BufferedWriter writer = null;
    protected @Nullable Thread msgReaderThread = null;
    private final Object msgReaderThreadLock = new Object();
    protected @Nullable AlarmDecoderDiscoveryService discoveryService;
    protected boolean discovery;
    protected boolean panelReadyReceived = false;
    protected volatile @Nullable Date lastReceivedTime;
    protected volatile boolean writeException;

    protected @Nullable ScheduledFuture<?> connectionCheckJob;
    protected @Nullable ScheduledFuture<?> connectRetryJob;

    public ADBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void dispose() {
        logger.trace("dispose called");
        disconnect();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(BridgeActions.class);
    }

    public void setDiscoveryService(AlarmDecoderDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Accepts no commands, so do nothing.
    }

    /**
     * Send a command to the alarm decoder using a buffered writer. This could block if the buffer is full, so it should
     * eventually be replaced with a queuing mechanism and a separate writer thread.
     *
     * @param command Command string to send including terminator
     */
    public void sendADCommand(ADCommand command) {
        logger.debug("Sending AD command: {}", command);
        try {
            if (writer != null) {
                writer.write(command.toString());
                writer.flush();
            }
        } catch (IOException e) {
            logger.info("Exception while sending command: {}", e.getMessage());
            writeException = true;
        }
    }

    protected abstract void connect();

    protected abstract void disconnect();

    protected void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    protected void startMsgReader() {
        synchronized (msgReaderThreadLock) {
            msgReaderThread = new Thread(this::readerThread, "AD Reader");
            msgReaderThread.setDaemon(true);
            msgReaderThread.start();
        }
    }

    protected void stopMsgReader() {
        synchronized (msgReaderThreadLock) {
            if (msgReaderThread != null) {
                logger.trace("Stopping reader thread.");
                msgReaderThread.interrupt();
                msgReaderThread = null;
            }
        }
    }

    /**
     * Method executed by message reader thread
     */
    private void readerThread() {
        logger.debug("Message reader thread started");
        String msg = null;
        try {
            // Send version command to get device to respond with VER message.
            sendADCommand(ADCommand.getVersion());
            while (!Thread.interrupted() && reader != null && (msg = reader.readLine()) != null) {
                logger.trace("Received msg: {}", msg);
                ADMsgType mt = ADMsgType.getMsgType(msg);
                if (mt != ADMsgType.INVALID) {
                    lastReceivedTime = new Date();
                }
                try {
                    switch (mt) {
                        case KPM:
                            parseKeypadMessage(msg);
                            break;
                        case REL:
                        case EXP:
                            parseRelayOrExpanderMessage(mt, msg);
                            break;
                        case RFX:
                            parseRFMessage(msg);
                            break;
                        case LRR:
                            parseLRRMessage(msg);
                            break;
                        case VER:
                            parseVersionMessage(msg);
                            break;
                        case INVALID:
                        default:
                            break;
                    }
                } catch (MessageParseException e) {
                    logger.info("Error {} while parsing message {}", e.getMessage(), msg);
                }
            }
            if (msg == null) {
                logger.info("End of input stream detected");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
            }
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    /**
     * Parse and handle keypad messages
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseKeypadMessage(String msg) throws MessageParseException {
        KeypadMessage kpm;

        // Parse the message
        try {
            kpm = new KeypadMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        if (kpm.panelClear()) {
            // the panel is clear, so we can assume that all contacts that we
            // have not heard from are open
            notifyChildHandlersPanelReady();
        }

        // Notify appropriate KeypadHandlers
        Collection<KeypadHandler> handlers = findKeypadHandlers(kpm.getIntAddressMask());
        for (KeypadHandler handler : handlers) {
            handler.handleUpdate(kpm);
        }
    }

    /**
     * Parse and handle relay and expander messages. The REL and EXP messages have identical format.
     *
     * @param mt message type of incoming message
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseRelayOrExpanderMessage(ADMsgType mt, String msg) throws MessageParseException {
        // mt is unused at the moment
        EXPMessage expm;

        try {
            expm = new EXPMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        ZoneHandler handler = findZoneHandler(expm.address, expm.channel);
        if (handler != null) {
            handler.handleUpdate(expm.data);
        }

        if (discovery && discoveryService != null) {
            discoveryService.processZone(expm.address, expm.channel);
        }
    }

    /**
     * Parse and handle RFX messages.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseRFMessage(String msg) throws MessageParseException {
        RFXMessage rfxm;

        try {
            rfxm = new RFXMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        RFZoneHandler handler = findRFZoneHandler(rfxm.serial);
        if (handler != null) {
            handler.handleUpdate(rfxm.data);
        }

        if (discovery && discoveryService != null) {
            discoveryService.processRFZone(rfxm.serial);
        }
    }

    /**
     * Parse and handle LRR messages.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseLRRMessage(String msg) throws MessageParseException {
        LRRMessage lrrm;

        // Parse the message
        try {
            lrrm = new LRRMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        // Notify appropriate LRRHandlers
        Collection<LRRHandler> handlers = findLRRHandlers(lrrm.partition);
        for (LRRHandler handler : handlers) {
            handler.handleUpdate(lrrm);
        }
    }

    /**
     * Parse and handle version (VER) message. This just updates bridge properties.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseVersionMessage(String msg) throws MessageParseException {
        VersionMessage verm;

        try {
            verm = new VersionMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        logger.trace("Processing version message sn:{} ver:{} cap:{}", verm.serial, verm.version, verm.capabilities);
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_SERIALNUM, verm.serial);
        properties.put(PROPERTY_VERSION, verm.version);
        properties.put(PROPERTY_CAPABILITIES, verm.capabilities);
        updateProperties(properties);
    }

    /**
     * Notify all child thing handlers that the alarm panel is in the ready state. Since there is no way to poll, all
     * channels are initialized into the UNDEF state. This method is called when there is reason to assume that there
     * are no faulted zones, because the alarm panel is in state READY. Zone handlers that have not yet received updates
     * can then set their contact states to CLOSED. Only executes the first time panel is ready after bridge
     * connect/reconnect.
     */
    private void notifyChildHandlersPanelReady() {
        if (!panelReadyReceived) {
            panelReadyReceived = true;
            logger.trace("Notifying child handlers that panel is in ready state");

            // Notify child zone handlers by calling notifyPanelReady() for each
            for (Thing thing : getThing().getThings()) {
                if (thing.getHandler() instanceof ZoneHandler || thing.getHandler() instanceof RFZoneHandler) {
                    ADThingHandler handler = (ADThingHandler) thing.getHandler();
                    if (handler != null) {
                        handler.notifyPanelReady();
                    }
                }
            }
        }
    }

    /**
     * Return the ZoneHandler for the given address and channel, or null if no handler exists and is initialized.
     */
    private @Nullable ZoneHandler findZoneHandler(int address, int channel) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof ZoneHandler) {
                ZoneHandler handler = (ZoneHandler) thing.getHandler();
                if (handler != null && handler.responsibleFor(address, channel)) {
                    return handler;
                }
            }
        }
        return null;
    }

    /**
     * Return the RFZoneHandler for the given serial number, or null if no handler exists and is initialized.
     */
    private @Nullable RFZoneHandler findRFZoneHandler(int serial) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof RFZoneHandler) {
                RFZoneHandler handler = (RFZoneHandler) thing.getHandler();
                if (handler != null && handler.responsibleFor(serial)) {
                    return handler;
                }
            }
        }
        return null;
    }

    /**
     * Return a collection of the KeypadHandler(s) matching the given address mask. Empty if no matching handlers exists
     * and are initialized.
     */
    private Collection<KeypadHandler> findKeypadHandlers(int mask) {
        Collection<KeypadHandler> result = new ArrayList<>();

        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof KeypadHandler) {
                KeypadHandler handler = (KeypadHandler) thing.getHandler();
                if (handler != null && handler.responsibleFor(mask)) {
                    result.add(handler);
                }
            }
        }
        return result;
    }

    /**
     * Return a collection of the LRRHandler(s) matching the given partition. Empty if no matching handlers exists
     * and are initialized.
     */
    private Collection<LRRHandler> findLRRHandlers(int partition) {
        Collection<LRRHandler> result = new ArrayList<>();

        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LRRHandler) {
                LRRHandler handler = (LRRHandler) thing.getHandler();
                if (handler != null && handler.responsibleFor(partition)) {
                    result.add(handler);
                }
            }
        }
        return result;
    }

    /**
     * Exception thrown by message parsing code when it encounters a malformed message
     */
    private static class MessageParseException extends Exception {
        private static final long serialVersionUID = 1L;

        public MessageParseException(String msg) {
            super(msg);
        }
    }
}
