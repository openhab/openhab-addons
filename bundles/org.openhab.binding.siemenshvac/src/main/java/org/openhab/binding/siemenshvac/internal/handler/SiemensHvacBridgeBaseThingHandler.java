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

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.discovery.SiemensHvacDeviceDiscoveryService;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution and API
 */
@NonNullByDefault
public abstract class SiemensHvacBridgeBaseThingHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacBridgeBaseThingHandler.class);
    private @Nullable SiemensHvacDeviceDiscoveryService discoveryService;
    private final @Nullable HttpClientFactory httpClientFactory;
    private final SiemensHvacMetadataRegistry metaDataRegistry;
    private @Nullable SiemensHvacBridgeConfig config;

    public SiemensHvacBridgeBaseThingHandler(Bridge bridge, @Nullable NetworkAddressService networkAddressService,
            @Nullable HttpClientFactory httpClientFactory, SiemensHvacMetadataRegistry metaDataRegistry) {
        super(bridge);
        SiemensHvacConnector lcConnector = null;
        this.httpClientFactory = httpClientFactory;
        this.metaDataRegistry = metaDataRegistry;

        lcConnector = this.metaDataRegistry.getSiemensHvacConnector();
        if (lcConnector != null) {
            lcConnector.setSiemensHvacBridgeBaseThingHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        metaDataRegistry.invalidate();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                "Waiting bridge initialization, reading metadata in background");

        SiemensHvacBridgeConfig lcConfig = getConfigAs(SiemensHvacBridgeConfig.class);
        String baseUrl = null;

        if (lcConfig.baseUrl != null) {
            baseUrl = lcConfig.baseUrl;
        }

        if (baseUrl == null) {
            logger.debug("baseUrl is mandatory on configuration !");
            return;
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://" + baseUrl;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        config = lcConfig;

        // Will read metadata in background to not block initialize for a long period !
        scheduler.schedule(this::initializeCode, 1, TimeUnit.SECONDS);
    }

    public static String getStackTrace(final Throwable throwable) {
        StringBuffer sb = new StringBuffer();

        Throwable current = throwable;
        while (current != null) {
            sb.append(current.getLocalizedMessage());
            sb.append(",\r\n");

            Throwable cause = throwable.getCause();
            if (cause != null) {
                if (!cause.equals(throwable)) {
                    current = current.getCause();
                } else {
                    current = null;
                }
            } else {
                current = null;
            }
        }
        return sb.toString();
    }

    private void initializeCode() {
        try {
            metaDataRegistry.readMeta();
            updateStatus(ThingStatus.ONLINE);
        } catch (SiemensHvacException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error occurs during gateway initialization: %s", getStackTrace(ex)));
        }
    }

    public @Nullable SiemensHvacBridgeConfig getBridgeConfiguration() {
        return config;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public boolean registerDiscoveryListener(SiemensHvacDeviceDiscoveryService listener) {
        SiemensHvacDeviceDiscoveryService lcDiscoveryService = discoveryService;
        if (lcDiscoveryService == null) {
            lcDiscoveryService = listener;
            lcDiscoveryService.setSiemensHvacMetadataRegistry(metaDataRegistry);
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        SiemensHvacDeviceDiscoveryService lcDiscoveryService = discoveryService;
        if (lcDiscoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    public @Nullable HttpClientFactory getHttpClientFactory() {
        return this.httpClientFactory;
    }
}
