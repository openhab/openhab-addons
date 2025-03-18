/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgtvserial.internal.protocol.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the communication (read/writes) between the COM port and the things.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class LGSerialCommunicator {

    private OutputStream output;
    private InputStream input;
    private SerialPort port;

    private Map<Integer, LGSerialResponseListener> handlers = new HashMap<>();
    private RegistrationCallback callback;

    private byte[] buffer = new byte[1024];

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(LGSerialCommunicator.class);

    private static final int BAUD_RATE = 9600;

    public LGSerialCommunicator(SerialPortIdentifier serialPortIdentifier, RegistrationCallback callback)
            throws IOException, PortInUseException, UnsupportedCommOperationException {
        SerialPort port = serialPortIdentifier.open(LGSerialCommunicator.class.getCanonicalName(), 2000);
        port.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        this.port = port;
        this.output = port.getOutputStream();
        this.input = port.getInputStream();
        this.callback = callback;
    }

    public synchronized void write(LGSerialCommand command, String rawCommand, ChannelUID channel) throws IOException {
        logger.debug("Sending command : {}", rawCommand);
        output.write(rawCommand.getBytes());
        output.write('\r');
        output.flush();

        // Let's wait not to spam the TV too much
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }

        int data;
        int len = 0;
        int offset = 0;
        while (input.available() > 0 && (data = input.read()) > -1) {
            if (data == 'x') {
                String result = new String(buffer, offset, len);
                if (logger.isDebugEnabled()) {
                    logger.debug("Buffer : {} offset={} len={}", Arrays.toString(buffer), offset, len);
                    logger.debug("Received response : {}", result);
                }
                LGSerialResponse response = command.parseResponse(result);
                updateHandler(response, channel);

                offset += len;
                len = 0;
            } else {
                if (offset + len == buffer.length) {
                    byte[] newBuffer = new byte[1024];
                    System.arraycopy(buffer, offset, newBuffer, 0, len);
                    buffer = newBuffer;
                }
                buffer[offset + len++] = (byte) data;
            }
        }
    }

    /**
     * Forward the response to the proper response handler, if a response was given.
     *
     * @param response Serial response
     * @param channel Channel to update
     */
    private void updateHandler(LGSerialResponse response, ChannelUID channel) {
        if (response != null) {
            LGSerialResponseListener listener = handlers.get(response.getSetID());
            if (listener != null) {
                if (response.isSuccess()) {
                    listener.onSuccess(channel, response);
                } else {
                    listener.onFailure(channel, response);
                }
            }
        }
    }

    public synchronized void register(LGSerialResponseListener listener) {
        handlers.put(listener.getSetID(), listener);
    }

    public synchronized void unregister(LGSerialResponseListener listener) {
        handlers.remove(listener.getSetID());
        if (handlers.isEmpty()) {
            callback.onUnregister();
        }
    }

    public void close() {
        try {
            input.close();
        } catch (IOException e) {
            logger.debug("An error occurred while closing the serial input stream", e);
        }
        try {
            output.close();
        } catch (IOException e) {
            logger.debug("An error occurred while closing the serial output stream", e);
        }
        port.close();
        // For some reason, there needs some delay after close so we don't fail to open back the serial device
        // TODO : investigate if this is still a real issue with the serial transport
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Thread interrupted while closing the communicator");
        }
    }
}
