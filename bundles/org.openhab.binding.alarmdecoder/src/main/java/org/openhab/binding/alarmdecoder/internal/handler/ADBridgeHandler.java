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
package org.openhab.binding.alarmdecoder.internal.handler;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.AlarmDecoderDiscoveryService;
import org.openhab.binding.alarmdecoder.internal.actions.BridgeActions;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMsgType;
import org.openhab.binding.alarmdecoder.internal.protocol.EXPMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.KeypadMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.LRRMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.RFXMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.VersionMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
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
    protected static final Charset AD_CHARSET = StandardCharsets.UTF_8;

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
            BufferedWriter bw = writer;
            if (bw != null) {
                bw.write(command.toString());
                bw.flush();
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
            Thread mrt = new Thread(this::readerThread, "OH-binding-" + getThing().getUID() + "-ADReader");
            mrt.setDaemon(true);
            mrt.start();
            msgReaderThread = mrt;
        }
    }

    protected void stopMsgReader() {
        synchronized (msgReaderThreadLock) {
            Thread mrt = msgReaderThread;
            if (mrt != null) {
                logger.trace("Stopping reader thread.");
                mrt.interrupt();
                msgReaderThread = null;
            }
        }
    }

    /**
     * Method executed by message reader thread
     */
    private void readerThread() {
        logger.debug("Message reader thread started");
        String message = null;
        try {
            // Send version command to get device to respond with VER message.
            sendADCommand(ADCommand.getVersion());
            BufferedReader reader = this.reader;
            while (!Thread.interrupted() && reader != null && (message = reader.readLine()) != null) {
                logger.trace("Received msg: {}", message);
                ADMsgType msgType = ADMsgType.getMsgType(message);
                if (msgType != ADMsgType.INVALID) {
                    lastReceivedTime = new Date();
                }
                try {
                    switch (msgType) {
                        case KPM:
                            parseKeypadMessage(message);
                            break;
                        case REL:
                        case EXP:
                            parseRelayOrExpanderMessage(msgType, message);
                            break;
                        case RFX:
                            parseRFMessage(message);
                            break;
                        case LRR:
                            parseLRRMessage(message);
                            break;
                        case VER:
                            parseVersionMessage(message);
                            break;
                        case INVALID:
                        default:
                            break;
                    }
                } catch (MessageParseException e) {
                    logger.warn("Error {} while parsing message {}. Please report bug.", e.getMessage(), message);
                }
            }
            if (message == null) {
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
        KeypadMessage kpMsg;

        // Parse the message
        try {
            kpMsg = new KeypadMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        if (kpMsg.panelClear()) {
            // the panel is clear, so we can assume that all contacts that we
            // have not heard from are open
            notifyChildHandlersPanelReady();
        }

        notifyChildHandlers(kpMsg);
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
        EXPMessage expMsg;

        try {
            expMsg = new EXPMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        notifyChildHandlers(expMsg);

        AlarmDecoderDiscoveryService ds = discoveryService;
        if (discovery && ds != null) {
            ds.processZone(expMsg.address, expMsg.channel);
        }
    }

    /**
     * Parse and handle RFX messages.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseRFMessage(String msg) throws MessageParseException {
        RFXMessage rfxMsg;

        try {
            rfxMsg = new RFXMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        notifyChildHandlers(rfxMsg);

        AlarmDecoderDiscoveryService ds = discoveryService;
        if (discovery && ds != null) {
            ds.processRFZone(rfxMsg.serial);
        }
    }

    /**
     * Parse and handle LRR messages.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseLRRMessage(String msg) throws MessageParseException {
        LRRMessage lrrMsg;

        // Parse the message
        try {
            lrrMsg = new LRRMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        notifyChildHandlers(lrrMsg);
    }

    /**
     * Parse and handle version (VER) message. This just updates bridge properties.
     *
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseVersionMessage(String msg) throws MessageParseException {
        VersionMessage verMsg;

        try {
            verMsg = new VersionMessage(msg);
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }

        logger.trace("Processing version message sn:{} ver:{} cap:{}", verMsg.serial, verMsg.version,
                verMsg.capabilities);
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_SERIALNUM, verMsg.serial);
        properties.put(PROPERTY_VERSION, verMsg.version);
        properties.put(PROPERTY_CAPABILITIES, verMsg.capabilities);
        updateProperties(properties);
    }

    /**
     * Notify appropriate child thing handlers of an AD message by calling their handleUpdate() methods.
     *
     * @param msg message to forward to child handler(s)
     */
    private void notifyChildHandlers(ADMessage msg) {
        for (Thing thing : getThing().getThings()) {
            ADThingHandler handler = (ADThingHandler) thing.getHandler();
            //@formatter:off
            if (handler != null && ((handler instanceof ZoneHandler && msg instanceof EXPMessage) ||
                                    (handler instanceof RFZoneHandler && msg instanceof RFXMessage) ||
                                    (handler instanceof KeypadHandler && msg instanceof KeypadMessage) ||
                                    (handler instanceof LRRHandler && msg instanceof LRRMessage))) {
                handler.handleUpdate(msg);
            }
            //@formatter:on
        }
    }

    /**
     * Notify child thing handlers that the alarm panel is in the ready state. Since there is no way to poll, all
     * contact channels are initialized into the UNDEF state. This method is called when there is reason to assume that
     * there are no faulted zones, because the alarm panel is in state READY. Zone handlers that have not yet received
     * updates can then set their contact states to CLOSED. Only executes the first time panel is ready after bridge
     * connect/reconnect.
     */
    private void notifyChildHandlersPanelReady() {
        if (!panelReadyReceived) {
            panelReadyReceived = true;
            logger.trace("Notifying child handlers that panel is in ready state");

            // Notify child zone handlers by calling notifyPanelReady() for each
            for (Thing thing : getThing().getThings()) {
                ADThingHandler handler = (ADThingHandler) thing.getHandler();
                if (handler != null) {
                    handler.notifyPanelReady();
                }
            }
        }
    }

    /**
     * Exception thrown by message parsing code when it encounters a malformed message
     */
    private static class MessageParseException extends Exception {
        private static final long serialVersionUID = 1L;

        public MessageParseException(@Nullable String msg) {
            super(msg);
        }
    }
}
