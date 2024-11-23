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
package org.openhab.binding.lifx.internal;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.MIN_ZONE_INDEX;
import static org.openhab.binding.lifx.internal.LifxProduct.Feature.*;
import static org.openhab.binding.lifx.internal.util.LifxMessageUtil.infraredToPercentType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.LifxProduct.Features;
import org.openhab.binding.lifx.internal.dto.GetColorZonesRequest;
import org.openhab.binding.lifx.internal.dto.GetHevCycleRequest;
import org.openhab.binding.lifx.internal.dto.GetLightInfraredRequest;
import org.openhab.binding.lifx.internal.dto.GetRequest;
import org.openhab.binding.lifx.internal.dto.GetTileEffectRequest;
import org.openhab.binding.lifx.internal.dto.GetWifiInfoRequest;
import org.openhab.binding.lifx.internal.dto.HevCycleState;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.dto.StateHevCycleResponse;
import org.openhab.binding.lifx.internal.dto.StateLightInfraredResponse;
import org.openhab.binding.lifx.internal.dto.StateLightPowerResponse;
import org.openhab.binding.lifx.internal.dto.StateMultiZoneResponse;
import org.openhab.binding.lifx.internal.dto.StatePowerResponse;
import org.openhab.binding.lifx.internal.dto.StateResponse;
import org.openhab.binding.lifx.internal.dto.StateTileEffectResponse;
import org.openhab.binding.lifx.internal.dto.StateWifiInfoResponse;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCurrentStateUpdater} sends packets to a light in order to update the {@code currentLightState} to
 * the actual light state.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightCurrentStateUpdater {

    private static final int STATE_POLLING_INTERVAL = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightCurrentStateUpdater.class);

    private final String logId;
    private final Features features;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private boolean wasOnline;
    private boolean updateSignalStrength;

    private @Nullable ScheduledFuture<?> statePollingJob;

    public LifxLightCurrentStateUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.features = context.getFeatures();
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

        if (features.hasFeature(HEV)) {
            communicationHandler.sendPacket(new GetHevCycleRequest());
        }
        if (features.hasFeature(INFRARED)) {
            communicationHandler.sendPacket(new GetLightInfraredRequest());
        }
        if (features.hasFeature(MULTIZONE)) {
            communicationHandler.sendPacket(new GetColorZonesRequest());
        }
        if (features.hasFeature(TILE_EFFECT)) {
            communicationHandler.sendPacket(new GetTileEffectRequest());
        }
        if (updateSignalStrength) {
            communicationHandler.sendPacket(new GetWifiInfoRequest());
        }
    }

    public void handleResponsePacket(Packet packet) {
        try {
            lock.lock();

            if (packet instanceof StateResponse response) {
                handleLightStatus(response);
            } else if (packet instanceof StatePowerResponse response) {
                handlePowerStatus(response);
            } else if (packet instanceof StateLightPowerResponse response) {
                handleLightPowerStatus(response);
            } else if (packet instanceof StateHevCycleResponse response) {
                handleHevCycleStatus(response);
            } else if (packet instanceof StateLightInfraredResponse response) {
                handleInfraredStatus(response);
            } else if (packet instanceof StateMultiZoneResponse response) {
                handleMultiZoneStatus(response);
            } else if (packet instanceof StateTileEffectResponse response) {
                handleTileEffectStatus(response);
            } else if (packet instanceof StateWifiInfoResponse response) {
                handleWifiInfoStatus(response);
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

    private void handleHevCycleStatus(StateHevCycleResponse packet) {
        HevCycleState hevCycleState = new HevCycleState(!packet.getRemaining().isZero(), packet.getDuration());
        currentLightState.setHevCycleState(hevCycleState);
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

    private void handleTileEffectStatus(StateTileEffectResponse packet) {
        currentLightState.setTileEffect(packet.getEffect());
    }

    private void handleWifiInfoStatus(StateWifiInfoResponse packet) {
        currentLightState.setSignalStrength(packet.getSignalStrength());
    }
}
