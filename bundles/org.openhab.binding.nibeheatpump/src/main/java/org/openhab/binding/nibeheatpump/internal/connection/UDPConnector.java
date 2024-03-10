/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nibeheatpump.internal.connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for UDP communication.
 *
 * Command for testing:
 *
 * @formatter:off
 * OK: echo -e "\x5C\x00\x20\x68\x50\x01\xA8\x1F\x01\x00\xA8\x64\x00\xFD\xA7\xD0\x03\x44\x9C\x1E\x00\x4F\x9C\xA0\x00\x50\x9C\x78\x00\x51\x9C\x03\x01\x52\x9C\x1B\x01\x87\x9C\x14\x01\x4E\x9C\xC6\x01\x47\x9C\x01\x01\x15\xB9\xB0\xFF\x3A\xB9\x4B\x00\xC9\xAF\x00\x00\x48\x9C\x0D\x01\x4C\x9C\xE7\x00\x4B\x9C\x00\x00\xFF\xFF\x00\x00\xFF\xFF\x00\x00\xFF\xFF\x00\x00\x45" | nc -4u -w1 localhost 9999
 * Special len: echo -e "\x5C\x00\x20\x68\x51\x44\x9C\x25\x00\x48\x9C\xFC\x00\x4C\x9C\xF1\x00\x4E\x9C\xC7\x01\x4D\x9C\x0B\x02\x4F\x9C\x25\x00\x50\x9C\x33\x00\x51\x9C\x0B\x01\x52\x9C\x5C\x5C\x01\x56\x9C\x31\x00\xC9\xAF\x00\x00\x01\xA8\x0C\x01\xFD\xA7\x16\xFA\xFA\xA9\x07\x00\x98\xA9\x1B\x1B\xFF\xFF\x00\x00\xA0\xA9\xCA\x02\xFF\xFF\x00\x00\x9C\xA9\x92\x12\xFF\xFF\x00\x00\xBE" | nc -4u -w1 localhost 9999
 * Special len: echo -e "\x5C\x00\x20\x68\x52\x44\x9C\x25\x00\x48\x9C\xFE\x00\x4C\x9C\xF2\x00\x4E\x9C\xD4\x01\x4D\x9C\xFB\x01\x4F\x9C\x25\x00\x50\x9C\x37\x00\x51\x9C\x0D\x01\x52\x9C\x5C\x5C\x01\x56\x9C\x32\x00\xC9\xAF\x00\x00\x01\xA8\x0C\x01\xFD\xA7\x12\xFA\xFA\xA9\x07\x00\x98\xA9\x5C\x5C\x1B\xFF\xFF\x00\x00\xA0\xA9\xD1\x02\xFF\xFF\x00\x00\x9C\xA9\xB4\x12\xFF\xFF\x00\x00\x7F" | nc -4u -w1 localhost 9999
 * Special CRC: echo -e "\x5C\x00\x20\x68\x50\x44\x9C\x26\x00\x48\x9C\xF6\x00\x4C\x9C\xF1\x00\x4E\x9C\xD6\x01\x4D\x9C\x0C\x02\x4F\x9C\x45\x00\x50\x9C\x3F\x00\x51\x9C\xF1\x00\x52\x9C\x04\x01\x56\x9C\xD5\x00\xC9\xAF\x00\x00\x01\xA8\x0C\x01\xFD\xA7\x99\xFA\xFA\xA9\x02\x00\x98\xA9\x1A\x1B\xFF\xFF\x00\x00\xA0\xA9\xCA\x02\xFF\xFF\x00\x00\x9C\xA9\x92\x12\xFF\xFF\x00\x00\xC5" | nc -4u -w1 localhost 9999
 * CRC failure: echo -e "\x5C\x00\x20\x68\x50\x01\xA8\x1F\x01\x00\xA8\x64\x00\xFD\xA7\xD0\x03\x44\x9C\x1E\x00\x4F\x9C\xA0\x00\x50\x9C\x78\x00\x51\x9C\x03\x01\x52\x9C\x1B\x01\x87\x9C\x14\x01\x4E\x9C\xC6\x01\x47\x9C\x01\x01\x15\xB9\xB0\xFF\x3A\xB9\x4B\x00\xC9\xAF\x00\x00\x48\x9C\x0D\x01\x4C\x9C\xE7\x00\x4B\x9C\x00\x00\xFF\xFF\x00\x00\xFF\xFF\x00\x00\xFF\xFF\x00\x00\x44" | nc -4u -w1 localhost 9999
 * @formatter:on
 *
 * @author Pauli Anttila - Initial contribution
 */
public class UDPConnector extends NibeHeatPumpBaseConnector {

    private final Logger logger = LoggerFactory.getLogger(UDPConnector.class);

    private Thread readerThread;
    private NibeHeatPumpConfiguration conf;
    private DatagramSocket socket;

    public UDPConnector() {
        logger.debug("Nibe heatpump UDP message listener created");
    }

    @Override
    public void connect(NibeHeatPumpConfiguration configuration) throws NibeHeatPumpException {
        if (isConnected()) {
            return;
        }
        conf = configuration;
        if (socket == null) {
            try {
                socket = new DatagramSocket(conf.port);
            } catch (SocketException e) {
                throw new NibeHeatPumpException(e);
            }
        }

        readerThread = new Reader();
        readerThread.start();
        connected = true;
    }

    @Override
    public void disconnect() {
        if (readerThread != null) {
            logger.debug("Interrupt message listener");
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        if (socket != null) {
            socket.close();
        }

        readerThread = null;
        connected = false;
        logger.debug("Closed");
    }

    @Override
    public void sendDatagram(NibeHeatPumpMessage msg) throws NibeHeatPumpException {
        logger.debug("Sending request: {}", msg.toHexString());

        byte[] data = msg.decodeMessage();
        int port = -1;

        if (msg instanceof ModbusWriteRequestMessage) {
            port = conf.writeCommandsPort;
        } else if (msg instanceof ModbusReadRequestMessage) {
            port = conf.readCommandsPort;
        } else {
            logger.trace("Ignore PDU: {}", msg.getClass());
        }

        if (port > 0) {
            try (DatagramSocket socket = new DatagramSocket()) {
                // Create a packet
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(conf.hostName),
                        port);
                socket.send(packet);
            } catch (IOException e) {
                throw new NibeHeatPumpException(e);
            }
        }
    }

    private class Reader extends Thread {
        boolean interrupted = false;

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.debug("Data listener started");
            while (!interrupted) {
                final int packetSize = 255;
                try {
                    if (socket == null) {
                        socket = new DatagramSocket(conf.port);
                    }
                    // Create a packet
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    // Receive a packet (blocking)
                    socket.receive(packet);
                    sendMsgToListeners(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
                } catch (InterruptedIOException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted via InterruptedIOException");
                } catch (IOException e) {
                    sendErrorToListeners(e.getMessage());
                }
            }
            logger.debug("Data listener stopped");
        }
    }
}
