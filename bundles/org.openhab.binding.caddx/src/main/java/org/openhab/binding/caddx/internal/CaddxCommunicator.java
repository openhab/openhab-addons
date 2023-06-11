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
package org.openhab.binding.caddx.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CaddxCommunicator} is responsible for the asynchronous serial communication
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxCommunicator implements SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(CaddxCommunicator.class);

    private final SerialPortManager portManager;
    private final Set<CaddxPanelListener> listenerQueue = new HashSet<>();
    private final Deque<CaddxMessage> messages = new LinkedBlockingDeque<>();
    private final SynchronousQueue<CaddxMessage> exchanger = new SynchronousQueue<>();
    private final Thread communicator;
    private final CaddxProtocol protocol;
    private final String serialPortName;
    private final int baudRate;
    private final SerialPort serialPort;
    private final InputStream in;
    private final OutputStream out;

    // Receiver state variables
    private boolean inMessage = false;
    private boolean haveFirstByte = false;
    private int messageBufferLength = 0;
    private byte[] message;
    private int messageBufferIndex = 0;
    private boolean unStuff = false;
    private int tempAsciiByte = 0;

    public CaddxCommunicator(String uid, SerialPortManager portManager, CaddxProtocol protocol, String serialPortName,
            int baudRate)
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {
        this.portManager = portManager;
        this.protocol = protocol;
        this.serialPortName = serialPortName;
        this.baudRate = baudRate;

        SerialPortIdentifier portIdentifier = this.portManager.getIdentifier(serialPortName);
        if (portIdentifier == null) {
            throw new IOException("Cannot get the port identifier.");
        }
        serialPort = portIdentifier.open(this.getClass().getName(), 2000);
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.enableReceiveThreshold(1);
        serialPort.disableReceiveTimeout();

        InputStream localIn = serialPort.getInputStream();
        if (localIn == null) {
            logger.warn("Cannot get the input stream of the serial port");
            throw new IOException("Input stream is null");
        }
        in = localIn;

        OutputStream localOut = serialPort.getOutputStream();
        if (localOut == null) {
            logger.warn("Cannot get the output stream of the serial port");
            throw new IOException("Output stream is null");
        }
        out = localOut;

        serialPort.notifyOnDataAvailable(true);
        serialPort.addEventListener(this);

        communicator = new Thread(this::messageDispatchLoop, "OH-binding-" + uid + "-caddxCommunicator");
        communicator.setDaemon(true);
        communicator.start();

        message = new byte[0];

        logger.trace("CaddxCommunicator communication thread started successfully for {}", serialPortName);
    }

    public CaddxProtocol getProtocol() {
        return protocol;
    }

    public String getSerialPortName() {
        return serialPortName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void addListener(CaddxPanelListener listener) {
        listenerQueue.add(listener);
    }

    /**
     * Send message to panel. Asynchronous, i.e. returns immediately.
     * Messages are sent only when panel is ready (i.e. sent an
     * acknowledgment to last message), but no checks are implemented that
     * the message was correctly received and executed.
     *
     * @param msg Data to be sent to panel. First byte is message type.
     *            Fletcher sum is computed and appended by transmit.
     */
    public void transmit(CaddxMessage msg) {
        messages.add(msg);
    }

    /**
     * Adds this message before any others in the queue.
     * Used by receiver to send ACKs.
     *
     * @param msg The message
     */
    public void transmitFirst(CaddxMessage msg) {
        messages.addFirst(msg);
    }

    public void stop() {
        logger.trace("CaddxCommunicator stopping");

        // kick thread out of waiting for FIFO
        communicator.interrupt();

        // Close the streams first to unblock blocked reads and writes
        try {
            in.close();
        } catch (IOException e) {
        }
        try {
            out.close();
        } catch (IOException e) {
        }

        // Wait until communication thread exits
        try {
            communicator.join(3000);
        } catch (InterruptedException e) {
        }

        // Also close the serial port
        serialPort.removeEventListener();
        serialPort.close();
    }

    @SuppressWarnings("null")
    private void messageDispatchLoop() {
        int[] expectedMessageNumbers = null;
        CaddxMessage outgoingMessage = null;
        boolean skipTransmit = true;
        CaddxMessageContext context = null;

        try {
            // loop until the thread is interrupted, sending out messages
            while (!Thread.currentThread().isInterrupted()) {
                // Initialize the state
                outgoingMessage = null;
                context = null;
                expectedMessageNumbers = null;

                if (!skipTransmit) {
                    // send next outgoing message if we have one
                    outgoingMessage = messages.poll();
                    if (outgoingMessage != null) {
                        logger.trace("CaddxCommunicator.run() Outgoing message: {}", outgoingMessage.getMessageType());

                        byte[] msg = outgoingMessage.getMessageFrameBytes(protocol);
                        out.write(msg);
                        out.flush();

                        expectedMessageNumbers = outgoingMessage.getReplyMessageNumbers();
                        context = outgoingMessage.getContext();

                        // Log message
                        if (logger.isDebugEnabled()) {
                            logger.debug("->: {}", outgoingMessage.getName());
                            logger.debug("->: {}", HexUtils
                                    .bytesToHex(outgoingMessage.getMessageFrameBytes(CaddxProtocol.Binary), " "));
                        }
                    }
                } else {
                    logger.trace("CaddxCommunicator.run() skipTransmit: true");
                    skipTransmit = false;
                }

                // Check for an incoming message
                CaddxMessage incomingMessage = null;
                try {
                    incomingMessage = exchanger.poll(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.debug("CaddxCommunicator.run() InterruptedException caught.");
                    Thread.currentThread().interrupt();
                }

                // Log
                if (incomingMessage == null) {
                    if (expectedMessageNumbers == null) { // Nothing expected, Nothing received we continue
                        logger.trace("CaddxCommunicator.run(): Nothing expected, Nothing received we continue");
                        continue;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("<-: {}", incomingMessage.getName());
                        logger.debug("<-: {}",
                                HexUtils.bytesToHex(incomingMessage.getMessageFrameBytes(CaddxProtocol.Binary), " "));
                    }
                }

                // Check if we wait for a reply
                if (expectedMessageNumbers == null) {
                    if (incomingMessage != null) { // Nothing expected. Message received.
                        logger.trace("CaddxCommunicator.run() Nothing expected, Message received");

                        // Check if Acknowledgement handling is required.
                        if (incomingMessage.hasAcknowledgementFlag()) {
                            if (incomingMessage.isChecksumCorrect()) {
                                // send ACK
                                transmitFirst(new CaddxMessage(CaddxMessageContext.NONE,
                                        CaddxMessageType.POSITIVE_ACKNOWLEDGE, ""));
                            } else {
                                // Send NAK
                                transmitFirst(new CaddxMessage(CaddxMessageContext.NONE,
                                        CaddxMessageType.NEGATIVE_ACKNOWLEDGE, ""));
                            }
                        }
                    }
                } else {
                    if (incomingMessage == null) {
                        logger.trace("CaddxCommunicator.run() Message expected. Nothing received");

                        // Message expected. Nothing received
                        if (outgoingMessage != null) {
                            transmitFirst(outgoingMessage); // put message in queue again
                            continue;
                        }
                    } else {
                        logger.trace("CaddxCommunicator.run() Message expected. Message received");

                        // Message expected. Message received.
                        int receivedMessageType = incomingMessage.getMessageType();
                        boolean isMessageExpected = IntStream.of(expectedMessageNumbers)
                                .anyMatch(x -> x == receivedMessageType);

                        if (!isMessageExpected) {
                            logger.trace("Non expected message received exp:{}, recv: {}", expectedMessageNumbers,
                                    receivedMessageType);

                            // Non expected reply received
                            if (outgoingMessage != null) {
                                transmitFirst(outgoingMessage); // put message in queue again
                                skipTransmit = true; // Skip the transmit on the next cycle to receive the panel message
                            }
                        }
                    }
                }

                // Inform the listeners
                if (incomingMessage != null) {
                    if (incomingMessage.isChecksumCorrect()) {
                        for (CaddxPanelListener listener : listenerQueue) {
                            if (context != null) {
                                incomingMessage.setContext(context);
                            }
                            listener.caddxMessage(incomingMessage);
                        }
                    } else {
                        logger.warn(
                                "CaddxCommunicator.run() Received packet checksum does not match. in: {} {}, calc {} {}",
                                incomingMessage.getChecksum1In(), incomingMessage.getChecksum2In(),
                                incomingMessage.getChecksum1Calc(), incomingMessage.getChecksum2Calc());
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("CaddxCommunicator.run() IOException. Stopping sender thread. {}", getSerialPortName());
            Thread.currentThread().interrupt();
        }

        logger.warn("CaddxCommunicator.run() Sender thread stopped. {}", getSerialPortName());
    }

    /**
     * Event handler to receive the data from the serial port
     *
     * @param SerialPortEvent serialPortEvent The event that occurred on the serial port
     */
    @Override
    public void serialEvent(@Nullable SerialPortEvent serialPortEvent) {
        if (serialPortEvent == null) {
            return;
        }

        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            logger.trace("Data receiving from the serial port");
            if (protocol == CaddxProtocol.Binary) {
                receiveInBinaryProtocol(serialPortEvent);
            } else {
                receiveInAsciiProtocol(serialPortEvent);
            }
        }
    }

    private int readByte(InputStream stream) throws IOException {
        int b = -1;

        if (stream.available() > 0) {
            b = stream.read();
        }
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    private int readAsciiByte(InputStream stream) throws IOException {
        if (!haveFirstByte) { // this is the 1st digit
            int b = readByte(in);
            tempAsciiByte = (b >= 0x30 && b <= 0x39) ? (b - 0x30) * 0x10 : (b - 0x37) * 0x10;
            haveFirstByte = true;
        }

        if (haveFirstByte) { // this is the 2nd digit
            int b = readByte(in);
            tempAsciiByte += (b >= 0x30 && b <= 0x39) ? (b - 0x30) : (b - 0x37);
            haveFirstByte = false;
        }

        return tempAsciiByte;
    }

    private void loopUntilByteIsRead(InputStream stream, int byteToRead) throws IOException {
        int b = 0;
        do {
            b = readByte(in);
        } while (b != byteToRead);
    }

    private void offerCaddxMessage() throws InterruptedException {
        logger.trace("Offering received message");

        // Full message received in data byte array
        CaddxMessage caddxMessage = new CaddxMessage(CaddxMessageContext.NONE, message, true);
        if (!exchanger.offer(caddxMessage, 3, TimeUnit.SECONDS)) {
            logger.debug("Offered message was not received");
        }
    }

    private void receiveInBinaryProtocol(SerialPortEvent serialPortEvent) {
        try {
            // Read the start byte
            if (!inMessage) // skip until 0x7E
            {
                loopUntilByteIsRead(in, 0x7e);

                inMessage = true;
                messageBufferLength = 0;
            }
            logger.trace("CaddxCommunicator.handleBinaryProtocol() Got start byte");

            // Read the message length
            if (messageBufferLength == 0) {
                int b = readByte(in);
                messageBufferLength = b + 2; // add two bytes for the checksum
                message = new byte[messageBufferLength];
                messageBufferIndex = 0;
            }
            logger.trace("CaddxCommunicator.handleBinaryProtocol() Got message length {}", messageBufferLength);

            // Read the message
            do {
                int b = readByte(in);
                message[messageBufferIndex] = (byte) b;

                if (message[messageBufferIndex] == 0x7D) {
                    unStuff = true;
                    continue;
                }

                if (unStuff) {
                    message[messageBufferIndex] |= 0x20;
                    unStuff = false;
                }

                messageBufferIndex++;
            } while (messageBufferIndex < messageBufferLength);

            // Offer the message
            offerCaddxMessage();

            logger.trace("CaddxCommunicator.handleBinaryProtocol() Got message {}", message[0]);
        } catch (EOFException e) {
            return;
        } catch (IOException e) {
        } catch (InterruptedException e) {
            logger.trace("InterruptedException caught.");
            Thread.currentThread().interrupt();
        }

        // Initialize state for a new reception
        inMessage = false;
        messageBufferLength = 0;
        messageBufferIndex = 0;
        unStuff = false;
    }

    private void receiveInAsciiProtocol(SerialPortEvent serialPortEvent) {
        try {
            // Read the start byte
            if (!inMessage) {
                loopUntilByteIsRead(in, 0x0a);

                inMessage = true;
                haveFirstByte = false;
                messageBufferLength = 0;
            }
            logger.trace("CaddxCommunicator.handleAsciiProtocol() Got start byte");

            // Read the message length
            if (messageBufferLength == 0) {
                int b = readAsciiByte(in);
                messageBufferLength = b + 2; // add 2 bytes for the checksum
                message = new byte[messageBufferLength];
            }
            logger.trace("CaddxCommunicator.handleAsciiProtocol() Got message length {}", messageBufferLength);

            // Read the message
            do {
                int b = readAsciiByte(in);
                message[messageBufferIndex] = (byte) b;
                messageBufferIndex++;
            } while (messageBufferIndex < messageBufferLength);

            // Offer the message
            offerCaddxMessage();

            logger.trace("CaddxCommunicator.handleAsciiProtocol() Got message {}", message[0]);
        } catch (EOFException e) {
            return;
        } catch (IOException e) {
        } catch (InterruptedException e) {
            logger.trace("InterruptedException caught.");
            Thread.currentThread().interrupt();
        }

        // Initialize state for a new reception
        inMessage = false;
        messageBufferLength = 0;
        messageBufferIndex = 0;
    }
}
