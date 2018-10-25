/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

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

    private PlugwiseStickConfig configuration = new PlugwiseStickConfig();
    private @Nullable SerialPort serialPort;

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
                IOUtils.closeQuietly(localSerialPort.getInputStream());
                IOUtils.closeQuietly(localSerialPort.getOutputStream());
                localSerialPort.close();
                serialPort = null;
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the serial port {} ({})", localSerialPort,
                        e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private CommPortIdentifier findSerialPortIdentifier() throws PlugwiseInitializationException {
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier identifier = portList.nextElement();
            if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL
                    && identifier.getName().equals(configuration.getSerialPort())) {
                logger.debug("Serial port '{}' has been found", configuration.getSerialPort());
                return identifier;
            }
        }

        // Build exception message when port not found
        StringBuilder sb = new StringBuilder();
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                sb.append(String.format("%s%n", id.getName()));
            }
        }
        throw new PlugwiseInitializationException(String.format(
                "Serial port '%s' could not be found. Available ports are:%n%s", configuration.getSerialPort(), sb));
    }

    public BlockingQueue<@Nullable AcknowledgementMessage> getAcknowledgedQueue() {
        return acknowledgedQueue;
    }

    public PlugwiseStickConfig getConfiguration() {
        return configuration;
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

    public void setConfiguration(PlugwiseStickConfig configuration) {
        this.configuration = configuration;
    }

}
