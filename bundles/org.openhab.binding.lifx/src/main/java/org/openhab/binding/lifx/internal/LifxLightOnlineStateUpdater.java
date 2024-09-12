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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.dto.GetEchoRequest;
import org.openhab.binding.lifx.internal.dto.GetServiceRequest;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightOnlineStateUpdater} sets the state of a light offline when it no longer responds to echo packets.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightOnlineStateUpdater {

    private static final int ECHO_POLLING_INTERVAL = 15;
    private static final int MAXIMUM_POLLING_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightOnlineStateUpdater.class);

    private final String logId;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private @Nullable ScheduledFuture<?> echoJob;
    private LocalDateTime lastSeen = LocalDateTime.MIN;
    private int unansweredEchoPackets;

    public LifxLightOnlineStateUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.scheduler = context.getScheduler();
        this.currentLightState = context.getCurrentLightState();
        this.communicationHandler = communicationHandler;
    }

    public void sendEchoPackets() {
        try {
            lock.lock();
            logger.trace("{} : Polling light state", logId);
            if (currentLightState.isOnline()) {
                if (Duration.between(lastSeen, LocalDateTime.now()).getSeconds() > ECHO_POLLING_INTERVAL) {
                    if (unansweredEchoPackets < MAXIMUM_POLLING_RETRIES) {
                        communicationHandler.sendPacket(GetEchoRequest.currentTimeEchoRequest());
                        unansweredEchoPackets++;
                    } else {
                        currentLightState.setOfflineByCommunicationError();
                        unansweredEchoPackets = 0;
                    }
                }
            } else {
                if (communicationHandler.isBroadcastEnabled()) {
                    logger.trace("{} : Light is not online, broadcasting request", logId);
                    communicationHandler.broadcastPacket(new GetServiceRequest());
                } else {
                    logger.trace("{} : Light is not online, unicasting request", logId);
                    communicationHandler.sendPacket(new GetServiceRequest());
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while polling the online state of a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localEchoJob = echoJob;
            if (localEchoJob == null || localEchoJob.isCancelled()) {
                echoJob = scheduler.scheduleWithFixedDelay(this::sendEchoPackets, 0, ECHO_POLLING_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting online state poller for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localEchoJob = echoJob;
            if (localEchoJob != null && !localEchoJob.isCancelled()) {
                localEchoJob.cancel(true);
                echoJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping online state poller for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void handleResponsePacket(Packet packet) {
        lastSeen = LocalDateTime.now();
        unansweredEchoPackets = 0;
        currentLightState.setOnline();
    }
}
