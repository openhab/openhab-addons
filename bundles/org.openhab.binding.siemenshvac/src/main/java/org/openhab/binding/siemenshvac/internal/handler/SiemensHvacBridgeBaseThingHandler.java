/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.discovery.SiemensHvacDeviceDiscoveryService;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link SiemensHvacBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution and API
 */
@NonNullByDefault
public abstract class SiemensHvacBridgeBaseThingHandler extends BaseBridgeHandler {

    // protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<>();
    // private final ScheduledExecutorService knxScheduler = ThreadPoolManager.getScheduledPool("knx");
    // private final ScheduledExecutorService backgroundScheduler = Executors.newSingleThreadScheduledExecutor();

    private @Nullable SiemensHvacDeviceDiscoveryService discoveryService;
    @SuppressWarnings("unused")
    private final @Nullable NetworkAddressService networkAddressService;
    private final @Nullable HttpClientFactory httpClientFactory;
    private final SiemensHvacMetadataRegistry metaDataRegistry;
    private @Nullable SiemensHvacConnector connector;

    public SiemensHvacBridgeBaseThingHandler(Bridge bridge, @Nullable NetworkAddressService networkAddressService,
            @Nullable HttpClientFactory httpClientFactory, SiemensHvacMetadataRegistry metaDataRegistry) {
        super(bridge);
        this.networkAddressService = networkAddressService;
        this.httpClientFactory = httpClientFactory;
        this.metaDataRegistry = metaDataRegistry;

        connector = this.metaDataRegistry.getSiemensHvacConnector();
        if (connector != null) {
            connector.setSiemensHvacBridgeBaseThingHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here
    }

    /*
     * public ScheduledExecutorService getScheduler() {
     * return knxScheduler;
     * }
     *
     * public ScheduledExecutorService getBackgroundScheduler() {
     * return backgroundScheduler;
     * }
     */

    @Override
    public void initialize() {
        metaDataRegistry.ReadMeta();
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public boolean registerDiscoveryListener(SiemensHvacDeviceDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
            discoveryService.setSiemensHvacMetadataRegistry(metaDataRegistry);
            // getFullLights().forEach(listener::addLightDiscovery);
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    public @Nullable HttpClientFactory getHttpClientFactory() {
        return this.httpClientFactory;
    }
}
