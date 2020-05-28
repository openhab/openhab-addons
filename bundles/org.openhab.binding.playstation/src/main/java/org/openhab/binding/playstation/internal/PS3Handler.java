/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.playstation.internal;

import static org.openhab.binding.playstation.internal.PlayStationBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PS3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS3Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PS3Handler.class);
    private static final int SOCKET_TIMEOUT_SECONDS = 2;

    private PS3Configuration config = new PS3Configuration();

    private @Nullable ScheduledFuture<?> refreshTimer;

    public PS3Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            if (CHANNEL_POWER.equals(channelUID.getId()) && command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    turnOnPS3();
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(PS3Configuration.class);

        updateStatus(ThingStatus.ONLINE);
        setupRefreshTimer();
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Sets up a timer for querying the PS3 (using the scheduler) every 10 seconds.
     */
    private void setupRefreshTimer() {
        final ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * This tries to connect to port 5223 on the PS3,
     * if the connection times out the PS3 is OFF, if connection is refused the PS3 is ON.
     */
    private void updateAllChannels() {
        try (SocketChannel channel = SocketChannel.open()) {
            Socket socket = channel.socket();
            socket.setSoTimeout(SOCKET_TIMEOUT_SECONDS * 1000);
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(config.ipAddress, DEFAULT_PS3_WAKE_ON_LAN_PORT));
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.contains("refused")) {
                updateState(CHANNEL_POWER, OnOffType.ON);
                updateStatus(ThingStatus.ONLINE);
            } else if (message.contains("timed out") || message.contains("is down")) {
                updateState(CHANNEL_POWER, OnOffType.OFF);
            } else {
                logger.info("PS3 read power, IOException: {}", e.getMessage());
            }
        }
    }

    private void turnOnPS3() {
        try {
            InetAddress bcAddress = InetAddress.getByName("255.255.255.255");

            // send WOL magic packet
            byte[] magicPacket = makeWOLMagicPacket(thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS));
            logger.debug("PS3 wol packet: {}", magicPacket);
            DatagramPacket wakePacket = new DatagramPacket(magicPacket, magicPacket.length, bcAddress,
                    DEFAULT_PS3_WAKE_ON_LAN_PORT);
            // send discover
            byte[] discover = "SRCH".getBytes();
            DatagramPacket srchPacket = new DatagramPacket(discover, discover.length, bcAddress,
                    DEFAULT_PS3_WAKE_ON_LAN_PORT);
            logger.debug("Search message: '{}'", discover);

            // wait for responses
            byte[] rxbuf = new byte[256];
            DatagramPacket receivePacket = new DatagramPacket(rxbuf, rxbuf.length);
            scheduler.execute(() -> wakeMethod(srchPacket, receivePacket, wakePacket, 34));
        } catch (IOException e) {
            logger.debug("No PS3 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private void wakeMethod(DatagramPacket srchPacket, DatagramPacket receivePacket, DatagramPacket wakePacket,
            int triesLeft) {
        try (
            DatagramSocket searchSocket = new DatagramSocket(); 
            DatagramSocket wakeSocket = new DatagramSocket();
        ) {
            wakeSocket.setBroadcast(true);
            searchSocket.setBroadcast(true);
            searchSocket.setSoTimeout(1000);

            searchSocket.send(srchPacket);
            try {
                searchSocket.receive(receivePacket);
                logger.debug("PS3 started?: '{}'", receivePacket);
                return;
            } catch (SocketTimeoutException e) {
                // try again
            }
            wakeSocket.send(wakePacket);
            if (triesLeft <= 0) {
                logger.debug("PS3 not started!");
            } else {
                scheduler.execute(() -> wakeMethod(srchPacket, receivePacket, wakePacket, triesLeft - 1));
            }
        } catch (IOException e) {
            logger.debug("No PS3 device found. Diagnostic: {}", e.getMessage());
        }

    }

    private byte[] makeWOLMagicPacket(String macAddress) {
        byte[] wolPacket = new byte[6 * 17];
        if (macAddress.length() < 17) {
            return wolPacket;
        }
        int pos = 0;
        for (int i = 0; i < 6; i++) {
            wolPacket[pos++] = -1; // 0xFF
        }
        byte[] macBytes = HexUtils.hexToBytes(macAddress, ":");
        for (int j = 0; j < 16; j++) {
            System.arraycopy(macBytes, 0, wolPacket, 6 + j * 6, 6);
        }
        return wolPacket;
    }
}
