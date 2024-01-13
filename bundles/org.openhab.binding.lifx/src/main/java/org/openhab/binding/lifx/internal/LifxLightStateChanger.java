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

import static org.openhab.binding.lifx.internal.LifxBindingConstants.PACKET_INTERVAL;
import static org.openhab.binding.lifx.internal.LifxProduct.Feature.MULTIZONE;
import static org.openhab.binding.lifx.internal.util.LifxMessageUtil.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.LifxProduct.Features;
import org.openhab.binding.lifx.internal.dto.AcknowledgementResponse;
import org.openhab.binding.lifx.internal.dto.ApplicationRequest;
import org.openhab.binding.lifx.internal.dto.Effect;
import org.openhab.binding.lifx.internal.dto.GetColorZonesRequest;
import org.openhab.binding.lifx.internal.dto.GetHevCycleRequest;
import org.openhab.binding.lifx.internal.dto.GetLightInfraredRequest;
import org.openhab.binding.lifx.internal.dto.GetLightPowerRequest;
import org.openhab.binding.lifx.internal.dto.GetRequest;
import org.openhab.binding.lifx.internal.dto.HevCycleState;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.dto.PowerState;
import org.openhab.binding.lifx.internal.dto.SetColorRequest;
import org.openhab.binding.lifx.internal.dto.SetColorZonesRequest;
import org.openhab.binding.lifx.internal.dto.SetHevCycleRequest;
import org.openhab.binding.lifx.internal.dto.SetLightInfraredRequest;
import org.openhab.binding.lifx.internal.dto.SetLightPowerRequest;
import org.openhab.binding.lifx.internal.dto.SetPowerRequest;
import org.openhab.binding.lifx.internal.dto.SetTileEffectRequest;
import org.openhab.binding.lifx.internal.dto.SignalStrength;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.listener.LifxLightStateListener;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightStateChanger} listens to state changes of the {@code pendingLightState}. It sends packets to a
 * light so the change the actual light state to that of the {@code pendingLightState}. When the light does not
 * acknowledge a packet, it resends it (max 3 times).
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightStateChanger implements LifxLightStateListener {

    /**
     * Milliseconds before a packet is considered to be lost (unacknowledged).
     */
    private static final int PACKET_ACKNOWLEDGE_INTERVAL = 250;

    /**
     * The number of times a lost packet will be resent.
     */
    private static final int MAX_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightStateChanger.class);

    private final String logId;
    private final Features features;
    private final Duration fadeTime;
    private final LifxLightState pendingLightState;
    private final ScheduledExecutorService scheduler;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private @Nullable ScheduledFuture<?> sendJob;

    private Map<Integer, List<PendingPacket>> pendingPacketsMap = new ConcurrentHashMap<>();

    private class PendingPacket {

        long lastSend;
        int sendCount;
        final Packet packet;

        private PendingPacket(Packet packet) {
            this.packet = packet;
        }

        private boolean hasAcknowledgeIntervalElapsed() {
            long millisSinceLastSend = System.currentTimeMillis() - lastSend;
            return millisSinceLastSend > PACKET_ACKNOWLEDGE_INTERVAL;
        }
    }

    public LifxLightStateChanger(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.features = context.getFeatures();
        this.fadeTime = context.getConfiguration().getFadeTime();
        this.pendingLightState = context.getPendingLightState();
        this.scheduler = context.getScheduler();
        this.communicationHandler = communicationHandler;
    }

    private void sendPendingPackets() {
        try {
            lock.lock();

            removeFailedPackets();
            PendingPacket pendingPacket = findPacketToSend();

            if (pendingPacket != null) {
                Packet packet = pendingPacket.packet;

                if (pendingPacket.sendCount == 0) {
                    // sendPacket will set the sequence number
                    logger.debug("{} : Sending {} packet", logId, packet.getClass().getSimpleName());
                    communicationHandler.sendPacket(packet);
                } else {
                    // resendPacket will reuse the sequence number
                    logger.debug("{} : Resending {} packet", logId, packet.getClass().getSimpleName());
                    communicationHandler.resendPacket(packet);
                }
                pendingPacket.lastSend = System.currentTimeMillis();
                pendingPacket.sendCount++;
            }
        } catch (Exception e) {
            logger.error("Error occurred while sending packet", e);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            pendingLightState.addListener(this);
            ScheduledFuture<?> localSendJob = sendJob;
            if (localSendJob == null || localSendJob.isCancelled()) {
                sendJob = scheduler.scheduleWithFixedDelay(this::sendPendingPackets, 0, PACKET_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting send packets job", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            pendingLightState.removeListener(this);
            ScheduledFuture<?> localSendJob = sendJob;
            if (localSendJob != null && !localSendJob.isCancelled()) {
                localSendJob.cancel(true);
                sendJob = null;
            }
            pendingPacketsMap.clear();
        } catch (Exception e) {
            logger.error("Error occurred while stopping send packets job", e);
        } finally {
            lock.unlock();
        }
    }

    private List<PendingPacket> createPendingPackets(Packet... packets) {
        Integer packetType = null;
        List<PendingPacket> pendingPackets = new ArrayList<>();

        for (Packet packet : packets) {
            // the acknowledgement is used to resend the packet in case of packet loss
            packet.setAckRequired(true);
            // the LIFX LAN protocol spec indicates that the response returned for a request would be the
            // previous value
            packet.setResponseRequired(false);
            pendingPackets.add(new PendingPacket(packet));

            if (packetType == null) {
                packetType = packet.getPacketType();
            } else if (packetType != packet.getPacketType()) {
                throw new IllegalArgumentException("Packets should have same packet type");
            }
        }

        return pendingPackets;
    }

    private void addPacketsToMap(Packet... packets) {
        List<PendingPacket> newPendingPackets = createPendingPackets(packets);
        int packetType = packets[0].getPacketType();

        try {
            lock.lock();
            List<PendingPacket> pendingPackets = pendingPacketsMap.get(packetType);
            if (pendingPackets == null) {
                pendingPacketsMap.put(packetType, newPendingPackets);
            } else {
                pendingPackets.addAll(newPendingPackets);
            }
        } finally {
            lock.unlock();
        }
    }

    private void replacePacketsInMap(Packet... packets) {
        List<PendingPacket> pendingPackets = createPendingPackets(packets);
        int packetType = packets[0].getPacketType();

        try {
            lock.lock();
            pendingPacketsMap.put(packetType, pendingPackets);
        } finally {
            lock.unlock();
        }
    }

    private @Nullable PendingPacket findPacketToSend() {
        PendingPacket result = null;
        for (List<PendingPacket> pendingPackets : pendingPacketsMap.values()) {
            for (PendingPacket pendingPacket : pendingPackets) {
                if (pendingPacket.hasAcknowledgeIntervalElapsed()
                        && (result == null || pendingPacket.lastSend < result.lastSend)) {
                    result = pendingPacket;
                }
            }
        }

        return result;
    }

    private void removePacketsByType(int packetType) {
        try {
            lock.lock();
            pendingPacketsMap.remove(packetType);
        } finally {
            lock.unlock();
        }
    }

    private void removeFailedPackets() {
        for (List<PendingPacket> pendingPackets : pendingPacketsMap.values()) {
            Iterator<PendingPacket> it = pendingPackets.iterator();
            while (it.hasNext()) {
                PendingPacket pendingPacket = it.next();
                if (pendingPacket.sendCount > MAX_RETRIES && pendingPacket.hasAcknowledgeIntervalElapsed()) {
                    logger.warn("{} failed (unacknowledged {} times to light {})",
                            pendingPacket.packet.getClass().getSimpleName(), pendingPacket.sendCount, logId);
                    it.remove();
                }
            }
        }
    }

    private @Nullable PendingPacket removeAcknowledgedPacket(int sequenceNumber) {
        for (List<PendingPacket> pendingPackets : pendingPacketsMap.values()) {
            Iterator<PendingPacket> it = pendingPackets.iterator();
            while (it.hasNext()) {
                PendingPacket pendingPacket = it.next();
                if (pendingPacket.packet.getSequence() == sequenceNumber) {
                    it.remove();
                    return pendingPacket;
                }
            }
        }

        return null;
    }

    @Override
    public void handleColorsChange(HSBK[] oldColors, HSBK[] newColors) {
        if (sameColors(newColors)) {
            SetColorRequest packet = new SetColorRequest(pendingLightState.getColors()[0], fadeTime.toMillis());
            removePacketsByType(SetColorZonesRequest.TYPE);
            replacePacketsInMap(packet);
        } else {
            List<SetColorZonesRequest> packets = new ArrayList<>();
            for (int i = 0; i < newColors.length; i++) {
                if (newColors[i] != null && !newColors[i].equals(oldColors[i])) {
                    packets.add(
                            new SetColorZonesRequest(i, newColors[i], fadeTime.toMillis(), ApplicationRequest.APPLY));
                }
            }
            if (!packets.isEmpty()) {
                removePacketsByType(SetColorRequest.TYPE);
                addPacketsToMap(packets.toArray(new SetColorZonesRequest[packets.size()]));
            }
        }
    }

    @Override
    public void handlePowerStateChange(@Nullable PowerState oldPowerState, PowerState newPowerState) {
        if (!newPowerState.equals(oldPowerState)) {
            SetLightPowerRequest packet = new SetLightPowerRequest(pendingLightState.getPowerState());
            replacePacketsInMap(packet);
        }
    }

    @Override
    public void handleHevCycleStateChange(@Nullable HevCycleState oldHevCycleState, HevCycleState newHevCycleState) {
        // The change should be ignored when both the old and new state are disabled regardless of the cycle duration
        if (!newHevCycleState.equals(oldHevCycleState)
                && (oldHevCycleState == null || oldHevCycleState.isEnable() || newHevCycleState.isEnable())) {
            SetHevCycleRequest packet = new SetHevCycleRequest(newHevCycleState.isEnable(),
                    newHevCycleState.getDuration());
            replacePacketsInMap(packet);
        }
    }

    @Override
    public void handleInfraredChange(@Nullable PercentType oldInfrared, PercentType newInfrared) {
        PercentType infrared = pendingLightState.getInfrared();
        if (infrared != null) {
            SetLightInfraredRequest packet = new SetLightInfraredRequest(percentTypeToInfrared(infrared));
            replacePacketsInMap(packet);
        }
    }

    @Override
    public void handleSignalStrengthChange(@Nullable SignalStrength oldSignalStrength,
            SignalStrength newSignalStrength) {
        // Nothing to handle
    }

    @Override
    public void handleTileEffectChange(@Nullable Effect oldEffect, Effect newEffect) {
        if (!newEffect.equals(oldEffect)) {
            SetTileEffectRequest packet = new SetTileEffectRequest(newEffect);
            replacePacketsInMap(packet);
        }
    }

    public void handleResponsePacket(Packet packet) {
        if (packet instanceof AcknowledgementResponse) {
            long ackTimestamp = System.currentTimeMillis();

            PendingPacket pendingPacket;

            try {
                lock.lock();
                pendingPacket = removeAcknowledgedPacket(packet.getSequence());
            } finally {
                lock.unlock();
            }

            if (pendingPacket != null) {
                Packet sentPacket = pendingPacket.packet;
                logger.debug("{} : {} packet was acknowledged in {}ms", logId, sentPacket.getClass().getSimpleName(),
                        ackTimestamp - pendingPacket.lastSend);

                // when these packets get lost the current state will still be updated by the
                // LifxLightCurrentStateUpdater
                if (sentPacket instanceof SetPowerRequest) {
                    GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                    communicationHandler.sendPacket(powerPacket);
                } else if (sentPacket instanceof SetColorRequest) {
                    GetRequest colorPacket = new GetRequest();
                    communicationHandler.sendPacket(colorPacket);
                    getZonesIfZonesAreSet();
                } else if (sentPacket instanceof SetColorZonesRequest) {
                    getZonesIfZonesAreSet();
                } else if (sentPacket instanceof SetHevCycleRequest) {
                    scheduler.schedule(() -> {
                        GetHevCycleRequest hevCyclePacket = new GetHevCycleRequest();
                        communicationHandler.sendPacket(hevCyclePacket);
                        GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                        communicationHandler.sendPacket(powerPacket);
                    }, 600, TimeUnit.MILLISECONDS);
                } else if (sentPacket instanceof SetLightInfraredRequest) {
                    GetLightInfraredRequest infraredPacket = new GetLightInfraredRequest();
                    communicationHandler.sendPacket(infraredPacket);
                }
            } else {
                logger.debug("{} : No pending packet found for ack with sequence number: {}", logId,
                        packet.getSequence());
            }
        }
    }

    private void getZonesIfZonesAreSet() {
        if (features.hasFeature(MULTIZONE)) {
            List<PendingPacket> pending = pendingPacketsMap.get(SetColorZonesRequest.TYPE);
            if (pending == null || pending.isEmpty()) {
                GetColorZonesRequest zoneColorPacket = new GetColorZonesRequest();
                communicationHandler.sendPacket(zoneColorPacket);
            }
        }
    }
}
