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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.dto.GetHostFirmwareRequest;
import org.openhab.binding.lifx.internal.dto.GetVersionRequest;
import org.openhab.binding.lifx.internal.dto.GetWifiFirmwareRequest;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.dto.StateHostFirmwareResponse;
import org.openhab.binding.lifx.internal.dto.StateVersionResponse;
import org.openhab.binding.lifx.internal.dto.StateWifiFirmwareResponse;
import org.openhab.binding.lifx.internal.fields.MACAddress;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;
import org.openhab.binding.lifx.internal.listener.LifxPropertiesUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightPropertiesUpdater} updates the light properties when a light goes online. When packets get lost
 * the requests are resent when the {@code UPDATE_INTERVAL} elapses.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightPropertiesUpdater {

    private final Logger logger = LoggerFactory.getLogger(LifxLightPropertiesUpdater.class);

    private static final int UPDATE_INTERVAL = 15;

    private final String logId;
    private final @Nullable InetSocketAddress ipAddress;
    private final @Nullable MACAddress macAddress;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;

    private final List<LifxPropertiesUpdateListener> propertiesUpdateListeners = new CopyOnWriteArrayList<>();

    private final List<Packet> requestPackets = Arrays.asList(new GetVersionRequest(), new GetHostFirmwareRequest(),
            new GetWifiFirmwareRequest());
    private final Set<Integer> receivedPacketTypes = new HashSet<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> updateJob;

    private final Map<String, String> properties = new HashMap<>();
    private boolean updating;
    private boolean wasOnline;

    public LifxLightPropertiesUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.macAddress = context.getConfiguration().getMACAddress();
        this.ipAddress = context.getConfiguration().getHost();
        this.currentLightState = context.getCurrentLightState();
        this.scheduler = context.getScheduler();
        this.communicationHandler = communicationHandler;
    }

    public void updateProperties() {
        if (propertiesUpdateListeners.isEmpty()) {
            logger.debug("{} : Not updating properties because there are no listeners", logId);
            return;
        }

        try {
            lock.lock();

            boolean isOnline = currentLightState.isOnline();
            if (isOnline) {
                if (!wasOnline) {
                    logger.debug("{} : Updating light properties", logId);
                    properties.clear();
                    receivedPacketTypes.clear();
                    updating = true;
                    updateHostProperty();
                    updateMACAddressProperty();
                    sendPropertyRequestPackets();
                } else if (updating && !receivedAllResponsePackets()) {
                    logger.debug("{} : Resending requests for missing response packets", logId);
                    sendPropertyRequestPackets();
                }
            }

            wasOnline = isOnline;
        } catch (Exception e) {
            logger.error("Error occurred while polling online state of a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    private void updateHostProperty() {
        InetSocketAddress host = communicationHandler.getIpAddress();
        if (host == null) {
            host = ipAddress;
        }
        if (host != null) {
            properties.put(LifxBindingConstants.PROPERTY_HOST, host.getHostString());
        }
    }

    private void updateMACAddressProperty() {
        MACAddress mac = communicationHandler.getMACAddress();
        if (mac == null) {
            mac = macAddress;
        }
        if (mac != null) {
            properties.put(LifxBindingConstants.PROPERTY_MAC_ADDRESS, mac.getAsLabel());
        }
    }

    private void sendPropertyRequestPackets() {
        for (Packet packet : requestPackets) {
            if (!receivedPacketTypes.contains(packet.expectedResponses()[0])) {
                communicationHandler.sendPacket(packet);
            }
        }
    }

    public void handleResponsePacket(Packet packet) {
        if (!updating) {
            return;
        }

        if (packet instanceof StateVersionResponse response) {
            long productId = response.getProduct();
            properties.put(LifxBindingConstants.PROPERTY_PRODUCT_ID, Long.toString(productId));

            long productVersion = response.getVersion();
            properties.put(LifxBindingConstants.PROPERTY_PRODUCT_VERSION, Long.toString(productVersion));

            try {
                LifxProduct product = LifxProduct.getProductFromProductID(productId);
                properties.put(LifxBindingConstants.PROPERTY_PRODUCT_NAME, product.getName());
                properties.put(LifxBindingConstants.PROPERTY_VENDOR_ID, Long.toString(product.getVendor().getID()));
                properties.put(LifxBindingConstants.PROPERTY_VENDOR_NAME, product.getVendor().getName());
            } catch (IllegalArgumentException e) {
                logger.debug("{} : Light has an unsupported product ID: {}", logId, productId);
            }

            receivedPacketTypes.add(packet.getPacketType());
        } else if (packet instanceof StateHostFirmwareResponse response) {
            String hostVersion = response.getVersion().toString();
            properties.put(LifxBindingConstants.PROPERTY_HOST_VERSION, hostVersion);
            receivedPacketTypes.add(packet.getPacketType());
        } else if (packet instanceof StateWifiFirmwareResponse response) {
            String wifiVersion = response.getVersion().toString();
            properties.put(LifxBindingConstants.PROPERTY_WIFI_VERSION, wifiVersion);
            receivedPacketTypes.add(packet.getPacketType());
        }

        if (receivedAllResponsePackets()) {
            updating = false;
            propertiesUpdateListeners.forEach(listener -> listener.handlePropertiesUpdate(properties));
            logger.debug("{} : Finished updating light properties", logId);
        }
    }

    private boolean receivedAllResponsePackets() {
        return requestPackets.size() == receivedPacketTypes.size();
    }

    public void addPropertiesUpdateListener(LifxPropertiesUpdateListener listener) {
        propertiesUpdateListeners.add(listener);
    }

    public void removePropertiesUpdateListener(LifxPropertiesUpdateListener listener) {
        propertiesUpdateListeners.remove(listener);
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localUpdateJob = updateJob;
            if (localUpdateJob == null || localUpdateJob.isCancelled()) {
                updateJob = scheduler.scheduleWithFixedDelay(this::updateProperties, 0, UPDATE_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting properties update job for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            ScheduledFuture<?> localUpdateJob = updateJob;
            if (localUpdateJob != null && !localUpdateJob.isCancelled()) {
                localUpdateJob.cancel(true);
                updateJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping properties update job for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }
}
