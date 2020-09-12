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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.velbus.internal.VelbusPacketInputStream;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.openhab.binding.velbus.internal.packets.VelbusSetDatePacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetDaylightSavingsStatusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetRealtimeClockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link VelbusBridgeHandler} is an abstract handler for a Velbus interface and connects it to
 * the framework.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public abstract class VelbusBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(VelbusBridgeHandler.class);

    private long lastPacketTimeMillis;

    protected @Nullable VelbusPacketListener defaultPacketListener;
    protected Map<Byte, VelbusPacketListener> packetListeners = new HashMap<Byte, VelbusPacketListener>();

    private @Nullable ScheduledFuture<?> timeUpdateJob;
    private @Nullable ScheduledFuture<?> reconnectionHandler;

    private @NonNullByDefault({}) OutputStream outputStream;
    private @NonNullByDefault({}) VelbusPacketInputStream inputStream;

    private boolean listenerStopped;

    public VelbusBridgeHandler(Bridge velbusBridge) {
        super(velbusBridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing velbus bridge handler.");

        connect();
        initializeTimeUpdate();
    }

    private void initializeTimeUpdate() {
        Object timeUpdateIntervalObject = getConfig().get(TIME_UPDATE_INTERVAL);
        if (timeUpdateIntervalObject != null) {
            int timeUpdateInterval = ((BigDecimal) timeUpdateIntervalObject).intValue();

            if (timeUpdateInterval > 0) {
                startTimeUpdates(timeUpdateInterval);
            }
        }
    }

    private void startTimeUpdates(int timeUpdatesInterval) {
        timeUpdateJob = scheduler.scheduleWithFixedDelay(() -> {
            updateDateTime();
        }, 0, timeUpdatesInterval, TimeUnit.MINUTES);
    }

    private void updateDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId());

        updateDate(zonedDateTime);
        updateTime(zonedDateTime);
        updateDaylightSavingsStatus(zonedDateTime);
    }

    private void updateTime(ZonedDateTime zonedDateTime) {
        VelbusSetRealtimeClockPacket packet = new VelbusSetRealtimeClockPacket((byte) 0x00, zonedDateTime);

        byte[] packetBytes = packet.getBytes();
        this.sendPacket(packetBytes);
    }

    private void updateDate(ZonedDateTime zonedDateTime) {
        VelbusSetDatePacket packet = new VelbusSetDatePacket((byte) 0x00, zonedDateTime);

        byte[] packetBytes = packet.getBytes();
        this.sendPacket(packetBytes);
    }

    private void updateDaylightSavingsStatus(ZonedDateTime zonedDateTime) {
        VelbusSetDaylightSavingsStatusPacket packet = new VelbusSetDaylightSavingsStatusPacket((byte) 0x00,
                zonedDateTime);

        byte[] packetBytes = packet.getBytes();
        this.sendPacket(packetBytes);
    }

    protected void initializeStreams(OutputStream outputStream, InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = new VelbusPacketInputStream(inputStream);
    }

    @Override
    public void dispose() {
        if (timeUpdateJob != null) {
            timeUpdateJob.cancel(true);
        }
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    public synchronized void sendPacket(byte[] packet) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeSinceLastPacket = currentTimeMillis - lastPacketTimeMillis;

        if (timeSinceLastPacket < 60) {
            // When sending you need a delay of 60ms between each packet (to prevent flooding the VMB1USB).
            long timeToDelay = 60 - timeSinceLastPacket;

            scheduler.schedule(() -> {
                sendPacket(packet);
            }, timeToDelay, TimeUnit.MILLISECONDS);

            return;
        }

        writePacket(packet);

        lastPacketTimeMillis = System.currentTimeMillis();
    }

    private void readPacket(byte[] packet) {
        byte address = packet[2];

        if (packetListeners.containsKey(address)) {
            VelbusPacketListener packetListener = packetListeners.get(address);
            packetListener.onPacketReceived(packet);
        } else if (defaultPacketListener != null) {
            defaultPacketListener.onPacketReceived(packet);
        }
    }

    protected void readPackets() {
        if (inputStream == null) {
            onConnectionLost();
            return;
        }

        byte[] packet;

        listenerStopped = false;

        try {
            while (!listenerStopped & ((packet = inputStream.readPacket()).length > 0)) {
                readPacket(packet);
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                onConnectionLost();
            }
        }
    }

    private void writePacket(byte[] packet) {
        if (outputStream == null) {
            onConnectionLost();
            return;
        }

        try {
            outputStream.write(packet);
            outputStream.flush();
        } catch (IOException e) {
            onConnectionLost();
        }
    }

    protected void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "A network communication error occurred.");
        disconnect();
        startReconnectionHandler();
    }

    protected abstract void connect();

    protected void disconnect() {
        listenerStopped = true;

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            logger.error("Error while closing output stream", e);
        }

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            logger.error("Error while closing input stream", e);
        }
    }

    public void startReconnectionHandler() {
        if (reconnectionHandler == null || reconnectionHandler.isCancelled()) {
            Object reconnectionIntervalObject = getConfig().get(RECONNECTION_INTERVAL);
            if (reconnectionIntervalObject != null) {
                long reconnectionInterval = ((BigDecimal) reconnectionIntervalObject).longValue();

                if (reconnectionInterval > 0) {
                    reconnectionHandler = scheduler.scheduleWithFixedDelay(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                connect();
                                if (reconnectionHandler != null) {
                                    reconnectionHandler.cancel(false);
                                }
                            } catch (Exception e) {
                                logger.error("Reconnection failed", e);
                            }
                        }
                    }, reconnectionInterval, reconnectionInterval, TimeUnit.SECONDS);
                }
            }
        }
    }

    public void setDefaultPacketListener(VelbusPacketListener velbusPacketListener) {
        defaultPacketListener = velbusPacketListener;
    }

    public void clearDefaultPacketListener() {
        defaultPacketListener = null;
    }

    public void registerPacketListener(byte address, VelbusPacketListener packetListener) {
        packetListeners.put(Byte.valueOf(address), packetListener);
    }

    public void unregisterRelayStatusListener(byte address) {
        packetListeners.remove(Byte.valueOf(address));
    }
}
