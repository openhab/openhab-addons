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
package org.openhab.binding.plugwise.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The communication context used by the {@link PlugwiseMessageSender} and {@link PlugwiseMessageProcessor} for sending
 * and receiving messages.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class PlugwiseCommunicationContext {

    /** Plugwise protocol header code (hex) */
    public static final String PROTOCOL_HEADER = "\u0005\u0005\u0003\u0003";

    /** Carriage return */
    public static final char CR = '\r';

    /** Line feed */
    public static final char LF = '\n';

    /** Plugwise protocol trailer code (hex) */
    public static final String PROTOCOL_TRAILER = new String(new char[] { CR, LF });

    public static final int MAX_BUFFER_SIZE = 1024;

    private static final Comparator<? super @Nullable PlugwiseQueuedMessage> QUEUED_MESSAGE_COMPERATOR = new Comparator<@Nullable PlugwiseQueuedMessage>() {
        @Override
        public int compare(@Nullable PlugwiseQueuedMessage o1, @Nullable PlugwiseQueuedMessage o2) {
            if (o1 == null || o2 == null) {
                return -1;
            }
            int result = o1.getPriority().compareTo(o2.getPriority());
            if (result == 0) {
                result = o1.getDateTime().compareTo(o2.getDateTime());
            }
            return result;
        }
    };

    private final Logger logger = LoggerFactory.getLogger(PlugwiseCommunicationContext.class);
    private final BlockingQueue<@Nullable AcknowledgementMessage> acknowledgedQueue = new ArrayBlockingQueue<>(
            MAX_BUFFER_SIZE, true);
    private final BlockingQueue<@Nullable Message> receivedQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE, true);
    private final PriorityBlockingQueue<@Nullable PlugwiseQueuedMessage> sendQueue = new PriorityBlockingQueue<>(
            MAX_BUFFER_SIZE, QUEUED_MESSAGE_COMPERATOR);
    private final BlockingQueue<@Nullable PlugwiseQueuedMessage> sentQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE,
            true);
    private final ReentrantLock sentQueueLock = new ReentrantLock();
    private final PlugwiseFilteredMessageListenerList filteredListeners = new PlugwiseFilteredMessageListenerList();

    private final ThingUID bridgeUID;
    private final Supplier<PlugwiseStickConfig> configurationSupplier;
    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;

    public PlugwiseCommunicationContext(ThingUID bridgeUID, Supplier<PlugwiseStickConfig> configurationSupplier,
            SerialPortManager serialPortManager) {
        this.bridgeUID = bridgeUID;
        this.configurationSupplier = configurationSupplier;
        this.serialPortManager = serialPortManager;
    }

    public void clearQueues() {
        acknowledgedQueue.clear();
        receivedQueue.clear();
        sendQueue.clear();
        sentQueue.clear();
    }

    public void closeSerialPort() {
        SerialPort localSerialPort = serialPort;
        if (localSerialPort != null) {
            try {
                InputStream inputStream = localSerialPort.getInputStream();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.debug("Error while closing the input stream: {}", e.getMessage());
                    }
                }
                OutputStream outputStream = localSerialPort.getOutputStream();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        logger.debug("Error while closing the output stream: {}", e.getMessage());
                    }
                }
                localSerialPort.close();
                serialPort = null;
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the serial port {} ({})", localSerialPort,
                        e.getMessage());
            }
        }
    }

    private SerialPortIdentifier findSerialPortIdentifier() throws PlugwiseInitializationException {
        SerialPortIdentifier identifier = serialPortManager.getIdentifier(getConfiguration().getSerialPort());
        if (identifier != null) {
            logger.debug("Serial port '{}' has been found", getConfiguration().getSerialPort());
            return identifier;
        }

        // Build exception message when port not found
        String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                .collect(Collectors.joining(System.lineSeparator()));

        throw new PlugwiseInitializationException(
                String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                        getConfiguration().getSerialPort(), availablePorts));
    }

    public BlockingQueue<@Nullable AcknowledgementMessage> getAcknowledgedQueue() {
        return acknowledgedQueue;
    }

    public ThingUID getBridgeUID() {
        return bridgeUID;
    }

    public PlugwiseStickConfig getConfiguration() {
        return configurationSupplier.get();
    }

    public PlugwiseFilteredMessageListenerList getFilteredListeners() {
        return filteredListeners;
    }

    public BlockingQueue<@Nullable Message> getReceivedQueue() {
        return receivedQueue;
    }

    public PriorityBlockingQueue<@Nullable PlugwiseQueuedMessage> getSendQueue() {
        return sendQueue;
    }

    public BlockingQueue<@Nullable PlugwiseQueuedMessage> getSentQueue() {
        return sentQueue;
    }

    public ReentrantLock getSentQueueLock() {
        return sentQueueLock;
    }

    public @Nullable SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Initialize this device and open the serial port
     *
     * @throws PlugwiseInitializationException if port can not be found or opened
     */
    public void initializeSerialPort() throws PlugwiseInitializationException {
        try {
            SerialPort localSerialPort = findSerialPortIdentifier().open(getClass().getName(), 2000);
            localSerialPort.notifyOnDataAvailable(true);
            localSerialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort = localSerialPort;
        } catch (PortInUseException e) {
            throw new PlugwiseInitializationException("Serial port already in use", e);
        } catch (UnsupportedCommOperationException e) {
            throw new PlugwiseInitializationException("Failed to set serial port parameters", e);
        }
    }
}
