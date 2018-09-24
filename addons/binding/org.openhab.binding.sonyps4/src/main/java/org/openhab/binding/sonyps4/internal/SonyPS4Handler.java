/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal;

import static org.openhab.binding.sonyps4.internal.SonyPS4BindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4Handler.class);
    private static final int BROADCAST_PORT = 987;
    private static final int SOCKET_TIMEOUT_SECONDS = 4;

    @Nullable
    private SonyPS4Configuration config;

    @Nullable
    private ScheduledFuture<?> refreshTimer;

    // State of PS4
    private String currentApplication = "";
    private OnOffType currentPower = OnOffType.OFF;

    public SonyPS4Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
        } else {
            if (CHANNEL_POWER.equals(channelUID.getId())) {

                // TODO: handle command

                // Note: if communication with thing fails for some reason,
                // indicate that by setting the status with detail information:
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                // "Could not control device at IP address x.x.x.x");
            }
            if (CHANNEL_APPLICATION.equals(channelUID.getId())) {
            }
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(SonyPS4Configuration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        setupRefreshTimer(1);
        // Example for background initialization:
        /*
         * scheduler.execute(() -> {
         * boolean thingReachable = true; // <background task with long running initialization here>
         * // when done do:
         * if (thingReachable) {
         * updateStatus(ThingStatus.ONLINE);
         * } else {
         * updateStatus(ThingStatus.OFFLINE);
         * }
         * });
         */

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *                            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> updateAllChannels(), initialWaitTime, 5,
                TimeUnit.SECONDS);
    }

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                updateState(channelUID, currentPower);
                break;
            case CHANNEL_APPLICATION:
                updateState(channelUID, StringType.valueOf(currentApplication));
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    private void updateAllChannels() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            socket.setSoTimeout(SOCKET_TIMEOUT_SECONDS * 1000);

            InetAddress inetAddress = InetAddress.getByName(config.getIpAddress());

            // send discover
            byte[] discover = "SRCH * HTTP/1.1\ndevice-discovery-protocol-version:00020020\n".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, BROADCAST_PORT);
            socket.send(packet);

            // wait for responses
            byte[] rxbuf = new byte[256];
            packet = new DatagramPacket(rxbuf, rxbuf.length);
            socket.receive(packet);

            parsePacket(packet);
        } catch (SocketTimeoutException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("PS4 communication timeout. Diagnostic: {}", e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("No PS4 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private void parsePacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = "";
        try {
            message = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("UTF-8 decoding not supported? {}", e);
        }
        String applicationTitleId = "";

        String[] ss = message.trim().split("\n");
        for (String row : ss) {
            int index = row.indexOf(':');
            if (index == -1 && !row.isEmpty()) {
                OnOffType power = null;
                if (row.contains("200")) {
                    power = OnOffType.ON;
                } else if (row.contains("620")) {
                    power = OnOffType.OFF;
                }
                if (power != null) {
                    if (currentPower != power) {
                        currentPower = power;
                        ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_POWER);
                        updateState(channel, currentPower);
                    }
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
                continue;
            }
            String key = row.substring(0, index);
            String value = row.substring(index + 1);
            switch (key) {
                case RESPONSE_RUNNING_APP_NAME:
                    if (!currentApplication.equals(value)) {
                        currentApplication = value;
                        // String appName = String.
                        ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_APPLICATION);
                        updateState(channel, StringType.valueOf(value));
                        logger.debug("PS4 current application: {}", value);
                    }
                    break;
                case RESPONSE_RUNNING_APP_TITLEID:
                    applicationTitleId = value;
                    logger.debug("PS4 current application title id: {}", applicationTitleId);
                    break;

                default:
                    break;
            }
        }
    }

}
