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
package org.openhab.binding.lifx.internal;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.MIN_ZONE_INDEX;
import static org.openhab.binding.lifx.internal.protocol.Product.Feature.*;
import static org.openhab.binding.lifx.internal.util.LifxMessageUtil.infraredToPercentType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;
import org.openhab.binding.lifx.internal.protocol.GetColorZonesRequest;
import org.openhab.binding.lifx.internal.protocol.GetLightInfraredRequest;
import org.openhab.binding.lifx.internal.protocol.GetRequest;
import org.openhab.binding.lifx.internal.protocol.GetWifiInfoRequest;
import org.openhab.binding.lifx.internal.protocol.Packet;
import org.openhab.binding.lifx.internal.protocol.Product;
import org.openhab.binding.lifx.internal.protocol.StateLightInfraredResponse;
import org.openhab.binding.lifx.internal.protocol.StateLightPowerResponse;
import org.openhab.binding.lifx.internal.protocol.StateMultiZoneResponse;
import org.openhab.binding.lifx.internal.protocol.StatePowerResponse;
import org.openhab.binding.lifx.internal.protocol.StateResponse;
import org.openhab.binding.lifx.internal.protocol.StateWifiInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCurrentStateUpdater} sends packets to a light in order to update the {@code currentLightState} to
 * the actual light state.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler
 */
@NonNullByDefault
public class LifxLightCurrentStateUpdater {

    private static final int STATE_POLLING_INTERVAL = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightCurrentStateUpdater.class);

    private final String logId;
    private final Product product;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private boolean wasOnline;
    private boolean updateSignalStrength;

    private @Nullable ScheduledFuture<?> statePollingJob;

    public LifxLightCurrentStateUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.product = context.getProduct();
        this.currentLightState = context.getCurrentLightState();
        this.scheduler = context.getScheduler();
        this.communicationHandler = communicationHandler;
    }

    public void pollLightState() {
        try {
            lock.lock();
            if (currentLightState.isOnline()) {
                logger.trace("{} : Polling the state of the light", logId);
                sendLightStateRequests();
            } else {
                logger.trace("{} : The light is not online, there is no point polling it", logId);
            }
            wasOnline = currentLightState.isOnline();
        } catch (Exception e) {
            logger.error("Error occurred while polling light state", e);
        } finally {
            lock.unlock();
        }
    }

    public void setUpdateSignalStrength(boolean updateSignalStrength) {
        this.updateSignalStrength = updateSignalStrength;
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localStatePollingJob = statePollingJob;
            if (localStatePollingJob == null || localStatePollingJob.isCancelled()) {
                statePollingJob = scheduler.scheduleWithFixedDelay(this::pollLightState, 0, STATE_POLLING_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting light state updater", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localStatePollingJob = statePollingJob;
            if (localStatePollingJob != null && !localStatePollingJob.isCancelled()) {
                localStatePollingJob.cancel(true);
                statePollingJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping light state updater", e);
        } finally {
            lock.unlock();
        }
    }

    private void sendLightStateRequests() {
        communicationHandler.sendPacket(new GetRequest());

        if (product.hasFeature(INFRARED)) {
            communicationHandler.sendPacket(new GetLightInfraredRequest());
        }
        if (product.hasFeature(MULTIZONE)) {
            communicationHandler.sendPacket(new GetColorZonesRequest());
        }
        if (updateSignalStrength) {
            communicationHandler.sendPacket(new GetWifiInfoRequest());
        }
    }

    public void handleResponsePacket(Packet packet) {
        try {
            lock.lock();

            if (packet instanceof StateResponse) {
                handleLightStatus((StateResponse) packet);
            } else if (packet instanceof StatePowerResponse) {
                handlePowerStatus((StatePowerResponse) packet);
            } else if (packet instanceof StateLightPowerResponse) {
                handleLightPowerStatus((StateLightPowerResponse) packet);
            } else if (packet instanceof StateLightInfraredResponse) {
                handleInfraredStatus((StateLightInfraredResponse) packet);
            } else if (packet instanceof StateMultiZoneResponse) {
                handleMultiZoneStatus((StateMultiZoneResponse) packet);
            } else if (packet instanceof StateWifiInfoResponse) {
                handleWifiInfoStatus((StateWifiInfoResponse) packet);
            }

            currentLightState.setOnline();

            if (currentLightState.isOnline() && !wasOnline) {
                wasOnline = true;
                logger.trace("{} : The light just went online, immediately polling the state of the light", logId);
                sendLightStateRequests();
            }
        } finally {
            lock.unlock();
        }
    }

    private void handleLightStatus(StateResponse packet) {
        currentLightState.setColor(packet.getColor(), MIN_ZONE_INDEX);
        currentLightState.setPowerState(packet.getPower());
    }

    private void handlePowerStatus(StatePowerResponse packet) {
        currentLightState.setPowerState(packet.getState());
    }

    private void handleLightPowerStatus(StateLightPowerResponse packet) {
        currentLightState.setPowerState(packet.getState());
    }

    private void handleInfraredStatus(StateLightInfraredResponse packet) {
        PercentType infrared = infraredToPercentType(packet.getInfrared());
        currentLightState.setInfrared(infrared);
    }

    private void handleMultiZoneStatus(StateMultiZoneResponse packet) {
        HSBK[] colors = currentLightState.getColors();
        if (colors.length != packet.getCount()) {
            colors = new HSBK[packet.getCount()];
        }
        for (int i = 0; i < packet.getColors().length && packet.getIndex() + i < colors.length; i++) {
            colors[packet.getIndex() + i] = packet.getColors()[i];
        }

        currentLightState.setColors(colors);
    }

    private void handleWifiInfoStatus(StateWifiInfoResponse packet) {
        currentLightState.setSignalStrength(packet.getSignalStrength());
    }

}
