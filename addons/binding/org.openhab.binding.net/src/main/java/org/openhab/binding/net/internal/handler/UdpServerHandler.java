/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal.handler;

import static org.openhab.binding.net.internal.NetBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.net.internal.config.UdpServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UdpServerHandler} is responsible for handling UDP server thing functionality.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class UdpServerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);

    private final int SOCKET_TIMEOUT_IN_MS = 1000;

    private UdpServerConfiguration configuration;
    private UdpServer server;

    public UdpServerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(UdpServerConfiguration.class);
        logger.debug("Using configuration: {}", configuration);

        try {
            startServer();
        } catch (Exception e) {
            logger.debug("Exception occurred during initalization: {}. ", e.getMessage(), e);
            shutdownServer();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stopping thing");
        shutdownServer();
    }

    private void startServer() {
        logger.debug("Start UDP server");
        server = new UdpServer(configuration.port, configuration.maxpdu, SOCKET_TIMEOUT_IN_MS, configuration.charset);
        scheduler.execute(server);
    }

    private void shutdownServer() {
        logger.debug("Shutdown UDP server");
        server.shutdown();

        // wait that server is fully stopped
        while (server.isRunning()) {
            logger.debug("Server still running...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private class UdpServer implements Runnable {
        private final int port;
        private final int maxpdu;
        private final int socketTimeout;
        private final String charset;

        private volatile boolean stop;
        private volatile boolean running = false;

        public UdpServer(int port, int maxpdu, int socketTimeout, String charset) {
            this.port = port;
            this.maxpdu = maxpdu;
            this.socketTimeout = socketTimeout;
            this.charset = charset;
        }

        public void shutdown() {
            stop = true;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            running = true;
            try (DatagramSocket socket = new DatagramSocket(port)) {
                socket.setSoTimeout(socketTimeout);
                DatagramPacket packet = new DatagramPacket(new byte[maxpdu], maxpdu);
                updateStatus(ThingStatus.ONLINE);
                while (!stop) {
                    try {
                        socket.receive(packet);
                        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                        if (logger.isDebugEnabled()) {
                            logger.debug("Received datagram: {}", HexUtils.bytesToHex(data));
                        }
                        triggerChannel(CHANNEL_DATA_RECEIVED, convertBytesToString(data, charset));
                    } catch (SocketTimeoutException e) {
                        // do nothing but continue
                    } catch (IOException e) {
                        logger.debug("IOException: reason {}.", e.getMessage(), e);
                    }
                }
            } catch (SocketException | SecurityException e) {
                logger.debug("SocketException: reason {}.", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            } catch (Exception e) {
                logger.debug("Unknow Exception received: reason {}.", e.getMessage(), e);
                throw e;
            }
            logger.debug("UDP server stopped");
            running = false;
        }
    }

    private String convertBytesToString(byte[] data, String charset) throws UnsupportedEncodingException {
        String retval = "";
        switch (charset != null ? charset : CHARSET_HEX_STRING) {
            case CHARSET_ASCII:
                retval = new String(data, StandardCharsets.US_ASCII.name());
                break;
            case CHARSET_UTF8:
                retval = new String(data, StandardCharsets.UTF_8.name());
                break;
            case CHARSET_HEX_STRING:
            default:
                retval = HexUtils.bytesToHex(data);
                break;
        }
        return retval;
    }
}
