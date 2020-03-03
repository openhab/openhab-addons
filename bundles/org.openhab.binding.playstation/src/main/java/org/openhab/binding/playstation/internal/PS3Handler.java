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
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
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
    private static final int SOCKET_TIMEOUT_SECONDS = 4;

    private PS4Configuration config = getConfigAs(PS4Configuration.class);

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
        config = getConfigAs(PS4Configuration.class);

        updateStatus(ThingStatus.UNKNOWN);
        setupRefreshTimer();

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Sets up a timer for querying the PS3 (using the scheduler) with the given interval.
     */
    private void setupRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, 0, 10, TimeUnit.SECONDS);
    }

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                updateState(channelUID, currentPower);
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    /**
     * This tries to connect to port 5223 on the PS3,
     * if the connection times out the PS3 is OFF, if connection is refused the PS3 is ON.
     */
    private void updateAllChannels() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            socket.setSoTimeout(SOCKET_TIMEOUT_SECONDS * 1000);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // logger.debug("PS3 power connect");
            // socket.connect(inetAddress, DEFAULT_PS3_COMMUNICATION_PORT);

            byte[] discover = PS4PacketHandler.makeSearchPacket();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress,
                    DEFAULT_PS3_UNKNOWN_PORT);
            logger.debug("PS3 power send");
            socket.send(packet);

            // wait for response
            byte[] rxbuf = new byte[256];
            packet = new DatagramPacket(rxbuf, rxbuf.length);
            socket.receive(packet);
            logger.debug("PS3 power received{}", rxbuf);
            updateState(CHANNEL_POWER, OnOffType.ON);
        } catch (PortUnreachableException e) {
            logger.info("PS3 read power, PortUnreachableException: {}", e.getMessage());
        } catch (IOException e) {
            updateState(CHANNEL_POWER, OnOffType.OFF);
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("PS3 read power, IOException: {}", e.getMessage());
        }
    }

    private void wakeUpPS3() {
        logger.debug("Waking up PS3...");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            InetAddress inetAddress = InetAddress.getByName("192.168.1.255");
            // send WOL magic packet
            byte[] magicPacket = makeWOLMagicPacket(thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS));
            logger.info("PS3 wol packet: {}", magicPacket);
            DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, inetAddress,
                    DEFAULT_PS3_BROADCAST_PORT);
            for (int i = 0; i < 4; i++) {
                socket.send(packet);
            }
        } catch (IOException e) {
            logger.info("Wake up PS3 exception: {}", e.getMessage());
        }
    }

    private void turnOnPS3() {
        wakeUpPS3();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(4 * 1000);

            InetAddress inetAddress = InetAddress.getByName("192.168.1.255");

            // send discover
            byte[] discover = "SRCH".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress,
                    DEFAULT_PS3_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Disover message sent: '{}'", discover);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }

                // parsePS3Packet(packet);
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
