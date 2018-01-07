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
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends messages to the Plugwise Stick using a serial connection.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseMessageSender {

    private class MessageSenderThread extends Thread {

        public MessageSenderThread() {
            super("Plugwise MessageSenderThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    PlugwiseQueuedMessage queuedMessage = context.getSendQueue().take();
                    logger.debug("Took message from sendQueue (length={})", context.getSendQueue().size());
                    if (queuedMessage == null) {
                        continue;
                    }
                    sendMessage(queuedMessage);
                    sleep(context.getConfiguration().getMessageWaitTime());
                } catch (InterruptedException e) {
                    // That's our signal to stop
                    break;
                } catch (Exception e) {
                    logger.warn("Error while polling/sending message", e);
                }
            }
        }

    }

    /** Default maximum number of attempts to send a message */
    private static final int MAX_RETRIES = 1;

    /** After exceeding this threshold the Stick is set offline */
    private static final int MAX_SEQUENTIAL_WRITE_ERRORS = 15;

    private final Logger logger = LoggerFactory.getLogger(PlugwiseMessageSender.class);
    private final PlugwiseCommunicationContext context;

    private WritableByteChannel outputChannel;
    private int sequentialWriteErrors;
    private MessageSenderThread thread;

    public PlugwiseMessageSender(PlugwiseCommunicationContext context) {
        this.context = context;
    }

    public void sendMessage(Message message, PlugwiseMessagePriority priority) throws IOException {
        if (sequentialWriteErrors > MAX_SEQUENTIAL_WRITE_ERRORS) {
            throw new IOException("Error writing to serial port " + context.getConfiguration().getSerialPort() + " ("
                    + sequentialWriteErrors + " times)");
        }

        if (message != null) {
            logger.debug("Adding {} message to sendQueue: {}", priority, message);
            context.getSendQueue().put(new PlugwiseQueuedMessage(message, priority));
        }
    }

    private void sendMessage(PlugwiseQueuedMessage queuedMessage) throws InterruptedException {
        if (queuedMessage.getAttempts() < MAX_RETRIES) {
            queuedMessage.increaseAttempts();

            Message message = queuedMessage.getMessage();

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
                logger.warn("Error writing '{}' to serial port {}: {}", packetString,
                        context.getConfiguration().getSerialPort(), e.getMessage());
                sequentialWriteErrors++;
                return;
            }

            // Poll the acknowledgement message for at most 1 second, normally it is received within 75ms
            AcknowledgementMessage ack = context.getAcknowledgedQueue().poll(1, TimeUnit.SECONDS);
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
                context.getSentQueueLock().lock();
                try {
                    if (context.getSentQueue().size() == PlugwiseCommunicationContext.MAX_BUFFER_SIZE) {
                        // For some reason Plugwise devices, or the Stick, does not send responses to Requests.
                        // They clog the sent queue. Let's flush some part of the queue
                        PlugwiseQueuedMessage someMessage = context.getSentQueue().poll();
                        logger.debug("Flushing from sentQueue: {}", someMessage);
                    }
                    context.getSentQueue().put(queuedMessage);
                } finally {
                    context.getSentQueueLock().unlock();
                }
            }
        } else {
            // Max attempts reached. We give up, and to a network reset
            logger.warn("Giving up on Plugwise message after {} attempts: {}", queuedMessage.getAttempts(),
                    queuedMessage.getMessage());
        }
    }

    public void start() throws PlugwiseInitializationException {
        sequentialWriteErrors = 0;
        try {
            outputChannel = Channels.newChannel(context.getSerialPort().getOutputStream());
        } catch (IOException e) {
            throw new PlugwiseInitializationException("Failed to get serial port output stream", e);
        }

        thread = new MessageSenderThread();
        thread.start();
    }

    public void stop() {
        PlugwiseUtils.stopBackgroundThread(thread);
        if (outputChannel != null) {
            try {
                outputChannel.close();
                outputChannel = null;
            } catch (IOException e) {
                logger.warn("Failed to close output channel", e);
            }
        }
    }

}
