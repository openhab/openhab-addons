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
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
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

    private static final Comparator<? super PlugwiseQueuedMessage> QUEUED_MESSAGE_COMPERATOR = new Comparator<PlugwiseQueuedMessage>() {
        @Override
        public int compare(PlugwiseQueuedMessage o1, PlugwiseQueuedMessage o2) {
            int result = o1.getPriority().compareTo(o2.getPriority());
            if (result == 0) {
                result = o1.getDateTime().compareTo(o2.getDateTime());
            }
            return result;
        }
    };

    private final Logger logger = LoggerFactory.getLogger(PlugwiseCommunicationContext.class);
    private final BlockingQueue<AcknowledgementMessage> acknowledgedQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE,
            true);
    private final BlockingQueue<Message> receivedQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE, true);
    private final PriorityBlockingQueue<PlugwiseQueuedMessage> sendQueue = new PriorityBlockingQueue<>(MAX_BUFFER_SIZE,
            QUEUED_MESSAGE_COMPERATOR);
    private final BlockingQueue<PlugwiseQueuedMessage> sentQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE, true);
    private final ReentrantLock sentQueueLock = new ReentrantLock();
    private final PlugwiseFilteredMessageListenerList filteredListeners = new PlugwiseFilteredMessageListenerList();

    private PlugwiseStickConfig configuration;
    private SerialPort serialPort;

    public void clearQueues() {
        acknowledgedQueue.clear();
        receivedQueue.clear();
        sendQueue.clear();
        sentQueue.clear();
    }

    public void closeSerialPort() {
        if (serialPort != null) {
            try {
                IOUtils.closeQuietly(serialPort.getInputStream());
                IOUtils.closeQuietly(serialPort.getOutputStream());
                serialPort.close();
                serialPort = null;
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the serial port {} ({})", serialPort, e.getMessage());
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

    public BlockingQueue<AcknowledgementMessage> getAcknowledgedQueue() {
        return acknowledgedQueue;
    }

    public PlugwiseStickConfig getConfiguration() {
        return configuration;
    }

    public PlugwiseFilteredMessageListenerList getFilteredListeners() {
        return filteredListeners;
    }

    public BlockingQueue<Message> getReceivedQueue() {
        return receivedQueue;
    }

    public PriorityBlockingQueue<PlugwiseQueuedMessage> getSendQueue() {
        return sendQueue;
    }

    public BlockingQueue<PlugwiseQueuedMessage> getSentQueue() {
        return sentQueue;
    }

    public ReentrantLock getSentQueueLock() {
        return sentQueueLock;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Initialize this device and open the serial port
     *
     * @throws PlugwiseInitializationException if port can not be found or opened
     */
    public void initializeSerialPort() throws PlugwiseInitializationException {
        try {
            serialPort = findSerialPortIdentifier().open(getClass().getName(), 2000);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
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
