/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.internal.PlugwiseCommunicationContext.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.MessageFactory;
import org.openhab.binding.plugwise.internal.protocol.field.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * Processes messages received from the Plugwise Stick using a serial connection.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseMessageProcessor implements SerialPortEventListener {

    private class MessageProcessorThread extends Thread {

        public MessageProcessorThread() {
            super("Plugwise MessageProcessorThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    Message message = context.getReceivedQueue().take();
                    logger.debug("Took message from receivedQueue (length={})", context.getReceivedQueue().size());
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

    /** Matches Plugwise responses into the following groups: protocolHeader command sequence payload CRC */
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("(.{4})(\\w{4})(\\w{4})(\\w*?)(\\w{4})");

    private final Logger logger = LoggerFactory.getLogger(PlugwiseMessageProcessor.class);
    private final PlugwiseCommunicationContext context;
    private final MessageFactory messageFactory = new MessageFactory();

    private ByteBuffer readBuffer = ByteBuffer.allocate(PlugwiseCommunicationContext.MAX_BUFFER_SIZE);
    private int previousByte = -1;
    private MessageProcessorThread thread;

    public PlugwiseMessageProcessor(PlugwiseCommunicationContext context) {
        this.context = context;
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
                                context.getAcknowledgedQueue().put((AcknowledgementMessage) message);
                            } else {
                                logger.debug("Adding to receivedQueue: {}", message);
                                context.getReceivedQueue().put(message);
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

    private void processMessage(Message message) {
        context.getFilteredListeners().notifyListeners(message);

        // After processing the response to a message, we remove any reference to the original request
        // stored in the sentQueue
        // WARNING: We assume that each request sent out can only be followed bye EXACTLY ONE response - so
        // far it seems that the Plugwise protocol is operating in that way

        try {
            context.getSentQueueLock().lock();

            Iterator<PlugwiseQueuedMessage> messageIterator = context.getSentQueue().iterator();
            while (messageIterator.hasNext()) {
                PlugwiseQueuedMessage queuedSentMessage = messageIterator.next();
                if (queuedSentMessage.getMessage().getSequenceNumber() == message.getSequenceNumber()) {
                    logger.debug("Removing from sentQueue: {}", queuedSentMessage.getMessage());
                    context.getSentQueue().remove(queuedSentMessage);
                    break;
                }
            }
        } finally {
            context.getSentQueueLock().unlock();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            // We get here if data has been received
            try {
                // Read data from serial device
                while (context.getSerialPort().getInputStream().available() > 0) {
                    int currentByte = context.getSerialPort().getInputStream().read();
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
                logger.debug("Error receiving data on serial port {}: {}", context.getConfiguration().getSerialPort(),
                        e.getMessage());
            }
        }
    }

    public void start() throws PlugwiseInitializationException {
        try {
            context.getSerialPort().addEventListener(this);
        } catch (TooManyListenersException e) {
            throw new PlugwiseInitializationException("Failed to add serial port listener", e);
        }

        thread = new MessageProcessorThread();
        thread.start();
    }

    public void stop() {
        PlugwiseUtils.stopBackgroundThread(thread);
        if (context.getSerialPort() != null) {
            context.getSerialPort().removeEventListener();
        }
    }

}
