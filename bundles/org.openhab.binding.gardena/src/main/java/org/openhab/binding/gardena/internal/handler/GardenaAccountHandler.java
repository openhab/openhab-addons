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
package org.openhab.binding.gardena.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.GardenaSmartImpl;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.discovery.GardenaDeviceDiscoveryService;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaAccountHandler} is the handler for a Gardena smart system access and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaAccountHandler extends BaseBridgeHandler implements GardenaSmartEventListener {
    private final Logger logger = LoggerFactory.getLogger(GardenaAccountHandler.class);
    private final long REINITIALIZE_DELAY_SECONDS = 10;

    private @Nullable GardenaDeviceDiscoveryService discoveryService;

    private @Nullable GardenaSmart gardenaSmart;
    private HttpClientFactory httpClientFactory;
    private WebSocketFactory webSocketFactory;

    public GardenaAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory,
            WebSocketFactory webSocketFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Gardena account '{}'", getThing().getUID().getId());
        initializeGardena();
    }

    public void setDiscoveryService(GardenaDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Initializes the GardenaSmart account.
     */
    private void initializeGardena() {
        final GardenaAccountHandler instance = this;
        scheduler.execute(() -> {
            try {
                GardenaConfig gardenaConfig = getThing().getConfiguration().as(GardenaConfig.class);
                logger.debug("{}", gardenaConfig);

                String id = getThing().getUID().getId();
                gardenaSmart = new GardenaSmartImpl(id, gardenaConfig, instance, scheduler, httpClientFactory,
                        webSocketFactory);
                final GardenaDeviceDiscoveryService discoveryService = this.discoveryService;
                if (discoveryService != null) {
                    discoveryService.startScan(null);
                    discoveryService.waitForScanFinishing();
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (GardenaException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                disposeGardena();
                scheduleReinitialize();
                logger.warn("{}", ex.getMessage());
            }
        });
    }

    /**
     * Schedules a reinitialization, if Gardena smart system account is not reachable.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(() -> {
            if (getThing().getStatus() != ThingStatus.UNINITIALIZED) {
                initializeGardena();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeGardena();
    }

    /**
     * Disposes the GardenaSmart account.
     */
    private void disposeGardena() {
        logger.debug("Disposing Gardena account '{}'", getThing().getUID().getId());
        final GardenaDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.stopScan();
        }
        final GardenaSmart gardenaSmart = this.gardenaSmart;
        if (gardenaSmart != null) {
            gardenaSmart.dispose();
        }
    }

    /**
     * Returns the Gardena smart system implementation.
     */
    public @Nullable GardenaSmart getGardenaSmart() {
        return gardenaSmart;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(GardenaDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing Gardena account '{}'", getThing().getUID().getId());
            disposeGardena();
            initializeGardena();
        }
    }

    @Override
    public void onDeviceUpdated(Device device) {
        for (ThingUID thingUID : UidUtils.getThingUIDs(device, getThing())) {
            final Thing gardenaThing;
            final GardenaThingHandler gardenaThingHandler;
            if ((gardenaThing = getThing().getThing(thingUID)) != null
                    && (gardenaThingHandler = (GardenaThingHandler) gardenaThing.getHandler()) != null) {
                try {
                    gardenaThingHandler.updateProperties(device);
                    for (Channel channel : gardenaThing.getChannels()) {
                        gardenaThingHandler.updateChannel(channel.getUID());
                    }
                    gardenaThingHandler.updateStatus(device);
                } catch (GardenaException ex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
                } catch (AccountHandlerNotAvailableException ignore) {
                }
            }
        }
    }

    @Override
    public void onNewDevice(Device device) {
        final GardenaDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }
        onDeviceUpdated(device);
    }

    @Override
    public void onError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
        disposeGardena();
        scheduleReinitialize();
    }
}
