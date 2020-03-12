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

    private PS3Configuration config = getConfigAs(PS3Configuration.class);

    private @Nullable ScheduledFuture<?> refreshTimer;

    // State of PS3
    private OnOffType currentPower = OnOffType.OFF;

    public PS3Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
        } else {
            if (CHANNEL_POWER.equals(channelUID.getId()) && command instanceof OnOffType) {
                currentPower = (OnOffType) command;
                if (currentPower.equals(OnOffType.ON)) {
                    turnOnPS3();
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(PS3Configuration.class);

        updateStatus(ThingStatus.ONLINE);
        setupRefreshTimer();

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Sets up a timer for querying the PS3 (using the scheduler) every 10 seconds.
     */
    private void setupRefreshTimer() {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, 0, 10, TimeUnit.SECONDS);
    }

    private void refreshFromState(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_POWER)) {
            updateState(channelUID, currentPower);
        } else {
            logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
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

        try (DatagramSocket srchSocket = new DatagramSocket(); DatagramSocket wakeSocket = new DatagramSocket();) {
            wakeSocket.setBroadcast(true);
            srchSocket.setBroadcast(true);
            srchSocket.setSoTimeout(1 * 1000);

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
            for (int i = 0; i < 34; i++) {
                srchSocket.send(srchPacket);
                try {
                    srchSocket.receive(receivePacket);
                    logger.debug("PS3 started?: '{}'", receivePacket);
                    // leave the loop
                    break;
                } catch (SocketTimeoutException e) {
                    // try again
                }
                wakeSocket.send(wakePacket);
                if (i >= 33) {
                    logger.debug("PS3 not started!");
                }
            }
        } catch (IOException e) {
            logger.debug("No PS3 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private byte[] makeWOLMagicPacket(String macAddress) {
        byte[] packet = new byte[6 * 17];
        if (macAddress.length() < 17) {
            return packet;
        }
        int pos = 0;
        for (int i = 0; i < 6; i++) {
            packet[pos++] = -1; // 0xFF
        }
        for (int j = 0; j < 16; j++) {
            for (int i = 0; i < 6; i++) {
                packet[pos++] = (byte) Integer.parseInt(macAddress.substring(i * 3, i * 3 + 2), 16);
            }
        }
        return packet;
    }
}
