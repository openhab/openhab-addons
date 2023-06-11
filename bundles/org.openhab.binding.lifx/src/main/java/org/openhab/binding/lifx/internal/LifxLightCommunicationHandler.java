/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.lifx.internal.fields.MACAddress.BROADCAST_ADDRESS;
import static org.openhab.binding.lifx.internal.util.LifxMessageUtil.randomSourceId;
import static org.openhab.binding.lifx.internal.util.LifxSelectorUtil.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.dto.GetServiceRequest;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.dto.StateServiceResponse;
import org.openhab.binding.lifx.internal.fields.MACAddress;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;
import org.openhab.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.openhab.binding.lifx.internal.util.LifxNetworkUtil;
import org.openhab.binding.lifx.internal.util.LifxSelectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCommunicationHandler} is responsible for the communications with a light.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightCommunicationHandler {

    private final Logger logger = LoggerFactory.getLogger(LifxLightCommunicationHandler.class);

    private final String logId;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;

    private final ReentrantLock lock = new ReentrantLock();
    private final long sourceId = randomSourceId();
    private final Supplier<Integer> sequenceNumberSupplier = new LifxSequenceNumberSupplier();

    private int service;
    private int unicastPort;
    private final int broadcastPort = LifxNetworkUtil.getNewBroadcastPort();

    private @Nullable ScheduledFuture<?> networkJob;

    private @Nullable MACAddress macAddress;
    private @Nullable InetSocketAddress host;
    private boolean broadcastEnabled;

    private @Nullable Selector selector;
    private @Nullable SelectionKey broadcastKey;
    private @Nullable SelectionKey unicastKey;
    private @Nullable LifxSelectorContext selectorContext;

    public LifxLightCommunicationHandler(LifxLightContext context) {
        this.logId = context.getLogId();
        this.macAddress = context.getConfiguration().getMACAddress();
        this.host = context.getConfiguration().getHost();
        this.currentLightState = context.getCurrentLightState();
        this.scheduler = context.getScheduler();
        this.broadcastEnabled = context.getConfiguration().getHost() == null;
    }

    private List<LifxResponsePacketListener> responsePacketListeners = new CopyOnWriteArrayList<>();

    public void addResponsePacketListener(LifxResponsePacketListener listener) {
        responsePacketListeners.add(listener);
    }

    public void removeResponsePacketListener(LifxResponsePacketListener listener) {
        responsePacketListeners.remove(listener);
    }

    public void start() {
        try {
            lock.lock();

            logger.debug("{} : Starting communication handler", logId);
            logger.debug("{} : Using '{}' as source identifier", logId, Long.toString(sourceId, 16));

            ScheduledFuture<?> localNetworkJob = networkJob;
            if (localNetworkJob == null || localNetworkJob.isCancelled()) {
                networkJob = scheduler.scheduleWithFixedDelay(this::receiveAndHandlePackets, 0, PACKET_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            currentLightState.setOffline();

            Selector localSelector = Selector.open();
            selector = localSelector;

            if (isBroadcastEnabled()) {
                broadcastKey = openBroadcastChannel(selector, logId, broadcastPort);
                selectorContext = new LifxSelectorContext(localSelector, sourceId, sequenceNumberSupplier, logId, host,
                        macAddress, broadcastKey, unicastKey);
                broadcastPacket(new GetServiceRequest());
            } else {
                unicastKey = openUnicastChannel(selector, logId, host);
                selectorContext = new LifxSelectorContext(localSelector, sourceId, sequenceNumberSupplier, logId, host,
                        macAddress, broadcastKey, unicastKey);
                sendPacket(new GetServiceRequest());
            }
        } catch (IOException e) {
            logger.error("{} while starting LIFX communication handler for light '{}' : {}",
                    e.getClass().getSimpleName(), logId, e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();

            ScheduledFuture<?> localNetworkJob = networkJob;
            if (localNetworkJob != null && !localNetworkJob.isCancelled()) {
                localNetworkJob.cancel(true);
                networkJob = null;
            }

            closeSelector(selector, logId);
            selector = null;
            broadcastKey = null;
            unicastKey = null;
            selectorContext = null;
        } finally {
            lock.unlock();
        }
    }

    public @Nullable InetSocketAddress getIpAddress() {
        return host;
    }

    public @Nullable MACAddress getMACAddress() {
        return macAddress;
    }

    public void receiveAndHandlePackets() {
        try {
            lock.lock();
            Selector localSelector = selector;
            if (localSelector == null || !localSelector.isOpen()) {
                logger.debug("{} : Unable to receive and handle packets with null or closed selector", logId);
            } else {
                LifxSelectorUtil.receiveAndHandlePackets(localSelector, logId,
                        (packet, address) -> handlePacket(packet, address));
            }
        } catch (Exception e) {
            logger.error("{} while receiving a packet from the light ({}): {}", e.getClass().getSimpleName(), logId,
                    e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void handlePacket(Packet packet, InetSocketAddress address) {
        boolean packetFromConfiguredMAC = macAddress != null && (packet.getTarget().equals(macAddress));
        boolean packetFromConfiguredHost = host != null && (address.equals(host));
        boolean broadcastPacket = packet.getTarget().equals(BROADCAST_ADDRESS);
        boolean packetSourceIsHandler = (packet.getSource() == sourceId || packet.getSource() == 0);

        if ((packetFromConfiguredMAC || packetFromConfiguredHost || broadcastPacket) && packetSourceIsHandler) {
            logger.trace("{} : Packet type '{}' received from '{}' for '{}' with sequence '{}' and source '{}'",
                    new Object[] { logId, packet.getClass().getSimpleName(), address.toString(),
                            packet.getTarget().getHex(), packet.getSequence(), Long.toString(packet.getSource(), 16) });

            if (packet instanceof StateServiceResponse) {
                StateServiceResponse response = (StateServiceResponse) packet;
                MACAddress discoveredAddress = response.getTarget();
                if (packetFromConfiguredHost && macAddress == null) {
                    macAddress = discoveredAddress;
                    currentLightState.setOnline(discoveredAddress);

                    LifxSelectorContext context = selectorContext;
                    if (context != null) {
                        context.setMACAddress(macAddress);
                    }
                    return;
                } else if (macAddress != null && macAddress.equals(discoveredAddress)) {
                    boolean newHost = host == null || !address.equals(host);
                    boolean newPort = unicastPort != (int) response.getPort();
                    boolean newService = service != response.getService();

                    if (newHost || newPort || newService || currentLightState.isOffline()) {
                        this.unicastPort = (int) response.getPort();
                        this.service = response.getService();

                        if (unicastPort == 0) {
                            logger.warn("Light ({}) service with ID '{}' is currently not available", logId, service);
                            currentLightState.setOfflineByCommunicationError();
                        } else {
                            this.host = new InetSocketAddress(address.getAddress(), unicastPort);

                            try {
                                cancelKey(unicastKey, logId);
                                unicastKey = openUnicastChannel(selector, logId, host);

                                LifxSelectorContext context = selectorContext;
                                if (context != null) {
                                    context.setHost(host);
                                    context.setUnicastKey(unicastKey);
                                }
                            } catch (IOException e) {
                                logger.warn("{} while opening the unicast channel of the light ({}): {}",
                                        e.getClass().getSimpleName(), logId, e.getMessage());
                                currentLightState.setOfflineByCommunicationError();
                                return;
                            }

                            currentLightState.setOnline();
                        }
                    }
                }
            }

            // Listeners are notified in a separate thread for better concurrency and to prevent deadlock.
            scheduler.schedule(() -> {
                responsePacketListeners.forEach(listener -> listener.handleResponsePacket(packet));
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public void broadcastPacket(Packet packet) {
        wrappedPacketSend((s, p) -> LifxSelectorUtil.broadcastPacket(s, p), packet);
    }

    public void sendPacket(Packet packet) {
        if (host != null) {
            wrappedPacketSend((s, p) -> LifxSelectorUtil.sendPacket(s, p), packet);
        }
    }

    public void resendPacket(Packet packet) {
        if (host != null) {
            wrappedPacketSend((s, p) -> LifxSelectorUtil.resendPacket(s, p), packet);
        }
    }

    private void wrappedPacketSend(BiFunction<LifxSelectorContext, Packet, Boolean> function, Packet packet) {
        LifxSelectorContext localSelectorContext = selectorContext;
        if (localSelectorContext != null) {
            boolean result = false;
            try {
                lock.lock();
                result = function.apply(localSelectorContext, packet);
            } finally {
                lock.unlock();
                if (!result) {
                    currentLightState.setOfflineByCommunicationError();
                }
            }
        }
    }
}
