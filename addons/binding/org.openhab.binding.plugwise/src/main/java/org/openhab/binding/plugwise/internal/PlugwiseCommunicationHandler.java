/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.MessageFactory;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link PlugwiseCommunicationHandler} handles all serial communication with the Plugwise Stick.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseCommunicationHandler implements SerialPortEventListener {

    /**
     * A filtered message listener listens to either all messages or only those of a device that has a certain MAC
     * address.
     */
    private class FilteredMessageListener {

        PlugwiseMessageListener listener;
        MACAddress macAddress;

        private FilteredMessageListener(PlugwiseMessageListener listener) {
            this.listener = listener;
        }

        private FilteredMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
            this.listener = listener;
            this.macAddress = macAddress;
        }

        public boolean matches(Message message) {
            return macAddress == null || macAddress.equals(message.getMACAddress());
        }
    }

    /**
     * When there are multiple {@link QueuedMessage}s in the {@link #sendQueue}, the message with the highest priority
     * and oldest {@link QueuedMessage#dateTime} is sent first. See also the {@link #QUEUED_MESSAGE_COMPERATOR}.
     */
    public enum MessagePriority {
        /**
         * Messages caused by Thing channel commands have the highest priority, e.g. to switch power on/off
         */
        COMMAND,

        /**
         * Messages that update the state of Thing channels immediately after a command has been sent.
         */
        FAST_UPDATE,

        /**
         * Messages for normal state updates and Thing discovery. E.g. scheduled tasks that update the state of a
         * channel.
         */
        UPDATE_AND_DISCOVERY
    }

    private class MessageProcessor extends Thread {

        private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

        public MessageProcessor() {
            super("Plugwise MessageProcessor");
            setDaemon(true);
        }

        private void processMessage(Message message) {

            notifyListeners(message);

            // After processing the response to a message, we remove any reference to the original request
            // stored in the sentQueue
            // WARNING: We assume that each request sent out can only be followed bye EXACTLY ONE response - so
            // far it seems that the Plugwise protocol is operating in that way

            try {
                sentQueueLock.lock();

                Iterator<QueuedMessage> messageIterator = sentQueue.iterator();
                while (messageIterator.hasNext()) {
                    QueuedMessage queuedSentMessage = messageIterator.next();
                    if (queuedSentMessage.message.getSequenceNumber() == message.getSequenceNumber()) {
                        logger.debug("Removing from sentQueue: {}", queuedSentMessage.message);
                        sentQueue.remove(queuedSentMessage);
                        break;
                    }
                }
            } finally {
                sentQueueLock.unlock();
            }
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    Message message = receivedQueue.take();
                    logger.debug("Took message from receivedQueue (length={})", receivedQueue.size());
                    processMessage(message);
                } catch (InterruptedException e) {
                    // That's our signal to stop
                    break;
                } catch (Exception e) {
                    logger.warn("Error while taking message from receivedQueue", e);
                }
            }
        }
    }

    private class MessageSender extends Thread {

        private final Logger logger = LoggerFactory.getLogger(MessageSender.class);

        public MessageSender() {
            super("Plugwise MessageSender");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    QueuedMessage queuedMessage = sendQueue.take();
                    logger.debug("Took message from sendQueue (length={})", sendQueue.size());
                    if (queuedMessage == null) {
                        continue;
                    }
                    sendMessage(queuedMessage);
                    sleep(configuration.getMessageWaitTime());
                } catch (InterruptedException e) {
                    // That's our signal to stop
                    break;
                } catch (Exception e) {
                    logger.warn("Error while polling/sending message", e);
                }
            }
        }

        private void sendMessage(QueuedMessage queuedMessage) throws InterruptedException {
            if (queuedMessage.attempts < MAX_RETRIES) {
                queuedMessage.attempts++;

                Message message = queuedMessage.message;

                String messageHexString = message.toHexString();
                String packetString = PROTOCOL_HEADER + messageHexString + PROTOCOL_TRAILER;
                ByteBuffer bytebuffer = ByteBuffer.allocate(packetString.length());
                bytebuffer.put(packetString.getBytes());
                bytebuffer.rewind();

                try {
                    logger.debug("Sending: {} as {}", message, messageHexString);
                    outputChannel.write(bytebuffer);
                    sequentialWriteErrors = 0;
                } catch (IOException e) {
                    logger.warn("Error writing '{}' to serial port {}: {}", packetString, configuration.getSerialPort(),
                            e.getMessage());
                    sequentialWriteErrors++;
                    return;
                }

                // Poll the acknowledgement message for at most 1 second, normally it is received within 75ms
                AcknowledgementMessage ack = acknowledgedQueue.poll(1, TimeUnit.SECONDS);
                logger.debug("Removing from acknowledgedQueue: {}", ack);

                if (ack == null) {
                    logger.warn("Error sending: No ACK received after 1 second: {}", packetString);
                } else if (!ack.isSuccess()) {
                    if (ack.isError()) {
                        logger.warn("Error sending: Negative ACK: {}", packetString);
                    }
                } else {
                    // Update the sent message with the new sequence number
                    message.setSequenceNumber(ack.getSequenceNumber());

                    // Place the sent message in the sent queue
                    logger.debug("Adding to sentQueue: {}", message);
                    sentQueueLock.lock();
                    try {
                        if (sentQueue.size() == MAX_BUFFER_SIZE) {
                            // For some reason Plugwise devices, or the Stick, does not send responses to Requests.
                            // They clog the sent queue. Let's flush some part of the queue
                            QueuedMessage someMessage = sentQueue.poll();
                            logger.debug("Flushing from sentQueue: {}", someMessage);
                        }
                        sentQueue.put(queuedMessage);
                    } finally {
                        sentQueueLock.unlock();
                    }
                }
            } else {
                // Max attempts reached. We give up, and to a network reset
                logger.warn("Giving up on Plugwise message after {} attempts: {}", queuedMessage.attempts,
                        queuedMessage.message);
            }
        }
    }

    private class QueuedMessage {
        private MessagePriority priority;
        private long dateTime = System.currentTimeMillis();
        private Message message;
        private int attempts = 0;

        public QueuedMessage(Message message, MessagePriority priority) {
            this.message = message;
            this.priority = priority;
        }
    }

    private static final Comparator<? super QueuedMessage> QUEUED_MESSAGE_COMPERATOR = new Comparator<QueuedMessage>() {
        @Override
        public int compare(QueuedMessage o1, QueuedMessage o2) {
            int result = o1.priority.compareTo(o2.priority);
            if (result == 0) {
                result = (o1.dateTime > o2.dateTime) ? 1 : (o1.dateTime == o2.dateTime) ? 0 : -1;
            }
            return result;
        }
    };

    /** Plugwise protocol header code (hex) */
    private static final String PROTOCOL_HEADER = "\u0005\u0005\u0003\u0003";

    /** Carriage return */
    private static final char CR = '\r';

    /** Line feed */
    private static final char LF = '\n';

    /** Plugwise protocol trailer code (hex) */
    private static final String PROTOCOL_TRAILER = new String(new char[] { CR, LF });

    /** Matches Plugwise responses into the following groups: protocolHeader command sequence payload CRC */
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("(.{4})(\\w{4})(\\w{4})(\\w*?)(\\w{4})");

    /** Default maximum number of attempts to send a message */
    private static final int MAX_RETRIES = 1;

    /** After exceeding this threshold the Stick is set offline */
    private static final int MAX_SEQUENTIAL_WRITE_ERRORS = 15;

    // Queue fields
    private static final int MAX_BUFFER_SIZE = 1024;

    private final Logger logger = LoggerFactory.getLogger(PlugwiseCommunicationHandler.class);
    private final MessageFactory messageFactory = new MessageFactory();
    private final List<FilteredMessageListener> filteredListeners = new CopyOnWriteArrayList<>();

    private PlugwiseStickConfig configuration;

    private final PriorityBlockingQueue<QueuedMessage> sendQueue = new PriorityBlockingQueue<>(MAX_BUFFER_SIZE,
            QUEUED_MESSAGE_COMPERATOR);
    private final BlockingQueue<AcknowledgementMessage> acknowledgedQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE,
            true);
    private final BlockingQueue<QueuedMessage> sentQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE, true);
    private final BlockingQueue<Message> receivedQueue = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE, true);

    private final ReentrantLock sentQueueLock = new ReentrantLock();

    // Background threads
    private Thread messageProcessor;
    private Thread messageSender;

    // Serial communication fields
    private ByteBuffer readBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
    private SerialPort serialPort;
    private WritableByteChannel outputChannel;
    private int previousByte = -1;
    private int sequentialWriteErrors = 0;

    private boolean initialized = false;

    public void addMessageListener(PlugwiseMessageListener listener) {
        if (!isExistingMessageListener(listener)) {
            filteredListeners.add(new FilteredMessageListener(listener));
        }
    }

    public void addMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        if (!isExistingMessageListener(listener, macAddress)) {
            filteredListeners.add(new FilteredMessageListener(listener, macAddress));
        }
    }

    /**
     * Initialize this device and open the serial port
     *
     * @throws PlugwiseInitializationException if port can not be opened
     */
    @SuppressWarnings("rawtypes")
    private void initializeSerialPort() throws PlugwiseInitializationException {

        CommPortIdentifier portId = null;

        // Parse ports and if the default port is found, initialized the reader
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL
                    && id.getName().equals(configuration.getSerialPort())) {
                logger.debug("Serial port '{}' has been found", configuration.getSerialPort());
                portId = id;
                break;
            }
        }

        if (portId == null) {
            StringBuilder sb = new StringBuilder();
            portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    sb.append(String.format("%s%n", id.getName()));
                }
            }
            throw new PlugwiseInitializationException(
                    String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                            configuration.getSerialPort(), sb));
        }

        try {
            serialPort = portId.open("openHAB", 2000);
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            outputChannel = Channels.newChannel(serialPort.getOutputStream());
        } catch (IOException e) {
            throw new PlugwiseInitializationException("Failed to get serial port output stream", e);
        } catch (PortInUseException e) {
            throw new PlugwiseInitializationException("Serial port already in use", e);
        } catch (TooManyListenersException e) {
            throw new PlugwiseInitializationException("Failed to add serial port listener", e);
        } catch (UnsupportedCommOperationException e) {
            throw new PlugwiseInitializationException("Failed to set serial port parameters", e);
        }
    }

    public boolean isExistingMessageListener(PlugwiseMessageListener listener) {
        for (FilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.listener.equals(listener)) {
                return true;
            }
        }
        return false;
    }

    public boolean isExistingMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        for (FilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.listener.equals(listener) && macAddress.equals(filteredListener.macAddress)) {
                return true;
            }
        }
        return false;
    }

    private void notifyListeners(Message message) {
        for (FilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.matches(message)) {
                try {
                    filteredListener.listener.handleReponseMessage(message);
                } catch (Exception e) {
                    logger.warn("Listener failed to handle message: {}", message, e);
                }
            }
        }
    }

    /**
     * Parse a buffer into a Message and put it in the appropriate queue for further processing
     *
     * @param readBuffer - the string to parse
     */
    private void parseAndQueue(ByteBuffer readBuffer) {
        if (readBuffer != null) {

            String response = new String(readBuffer.array(), 0, readBuffer.limit());
            response = StringUtils.chomp(response);

            Matcher matcher = RESPONSE_PATTERN.matcher(response);

            if (matcher.matches()) {

                String protocolHeader = matcher.group(1);
                String messageTypeHex = matcher.group(2);
                String sequence = matcher.group(3);
                String payload = matcher.group(4);
                String crc = matcher.group(5);

                if (protocolHeader.equals(PROTOCOL_HEADER)) {
                    String calculatedCRC = Message.getCRC(messageTypeHex + sequence + payload);
                    if (calculatedCRC.equals(crc)) {
                        MessageType messageType = MessageType.forValue(Integer.parseInt(messageTypeHex, 16));
                        int sequenceNumber = Integer.parseInt(sequence, 16);

                        if (messageType == null) {
                            logger.debug("Received unrecognized message: messageTypeHex=0x{}, sequence={}, payload={}",
                                    messageTypeHex, sequenceNumber, payload);
                            return;
                        }

                        logger.debug("Received message: messageType={}, sequenceNumber={}, payload={}", messageType,
                                sequenceNumber, payload);

                        try {
                            Message message = messageFactory.createMessage(messageType, sequenceNumber, payload);

                            if (message instanceof AcknowledgementMessage
                                    && !((AcknowledgementMessage) message).isExtended()) {
                                logger.debug("Adding to acknowledgedQueue: {}", message);
                                acknowledgedQueue.put((AcknowledgementMessage) message);
                            } else {
                                logger.debug("Adding to receivedQueue: {}", message);
                                receivedQueue.put(message);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Failed to create message", e);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                        }
                    } else {
                        logger.warn("Plugwise protocol CRC error: {} does not match {} in message", calculatedCRC, crc);
                    }
                } else {
                    logger.debug("Plugwise protocol header error: {} in message {}", protocolHeader, response);
                }
            } else if (!response.contains("APSRequestNodeInfo") && !response.contains("APSSetSleepBehaviour")
                    && !response.startsWith("# ")) {
                logger.warn("Plugwise protocol message error: {}", response);
            }
        }
    }

    public void removeMessageListener(PlugwiseMessageListener listener) {
        List<FilteredMessageListener> removedListeners = new ArrayList<>();
        for (FilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.listener.equals(listener)) {
                removedListeners.add(filteredListener);
            }
        }

        filteredListeners.removeAll(removedListeners);
    }

    public void sendMessage(Message message, MessagePriority priority) throws IOException {
        if (sequentialWriteErrors > MAX_SEQUENTIAL_WRITE_ERRORS) {
            throw new IOException("Error writing to serial port " + configuration.getSerialPort() + " ("
                    + sequentialWriteErrors + " times)");
        }

        if (message != null && initialized) {
            logger.debug("Adding {} message to sendQueue: {}", priority, message);
            sendQueue.put(new QueuedMessage(message, priority));
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            // We get here if data has been received
            try {
                // Read data from serial device
                while (serialPort.getInputStream().available() > 0) {
                    int currentByte = serialPort.getInputStream().read();
                    // Plugwise sends ASCII data, but for some unknown reason we sometimes get data with unsigned
                    // byte value >127 which in itself is very strange. We filter these out for the time being
                    if (currentByte < 128) {
                        readBuffer.put((byte) currentByte);
                        if (previousByte == CR && currentByte == LF) {
                            readBuffer.flip();
                            parseAndQueue(readBuffer);
                            readBuffer.clear();
                            previousByte = -1;
                        } else {
                            previousByte = currentByte;
                        }
                    }
                }
            } catch (IOException e) {
                logger.debug("Error receiving data on serial port {}: {}", configuration.getSerialPort(),
                        e.getMessage());
            }
        }
    }

    public void setConfiguration(PlugwiseStickConfig configuration) {
        this.configuration = configuration;
    }

    public void start() throws PlugwiseInitializationException {
        try {
            initializeSerialPort();

            acknowledgedQueue.clear();
            receivedQueue.clear();
            sendQueue.clear();
            sentQueue.clear();

            messageSender = new MessageSender();
            messageProcessor = new MessageProcessor();

            messageSender.start();
            messageProcessor.start();

            sequentialWriteErrors = 0;
            initialized = true;
        } catch (PlugwiseInitializationException e) {
            initialized = false;
            throw e;
        }
    }

    /**
     * Close this serial device associated with the Stick
     */
    public void stop() {
        stopBackgroundThread(messageSender);
        stopBackgroundThread(messageProcessor);

        if (serialPort != null) {
            serialPort.removeEventListener();
            try {
                IOUtils.closeQuietly(serialPort.getInputStream());
                IOUtils.closeQuietly(serialPort.getOutputStream());
                serialPort.close();
                serialPort = null;
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the serial port {} ({})", serialPort, e.getMessage());
            }
        }

        initialized = false;
    }

    private void stopBackgroundThread(Thread thread) {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

}
