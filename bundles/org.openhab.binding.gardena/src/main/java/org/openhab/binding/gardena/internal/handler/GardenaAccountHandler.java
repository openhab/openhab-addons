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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
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
import org.openhab.core.thing.binding.ThingHandler;
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
    private static final Duration REINITIALIZE_DELAY_SECONDS = Duration.ofSeconds(120);
    private static final Duration REINITIALIZE_DELAY_MINUTES_BACK_OFF = Duration.ofMinutes(15).plusSeconds(30);
    private static final Duration REINITIALIZE_DELAY_HOURS_LIMIT_EXCEEDED = Duration.ofHours(24).plusMinutes(15);

    private @Nullable GardenaDeviceDiscoveryService discoveryService;

    private @Nullable GardenaSmart gardenaSmart;
    private HttpClientFactory httpClientFactory;
    private WebSocketFactory webSocketFactory;

    private final Object reInitializationCodeLock = new Object();
    private @Nullable ScheduledFuture<?> reInitializationTask;
    private boolean reInitializationCausedBy429 = false;
    private LocalDateTime lastErrorTime = LocalDateTime.MIN;

    public GardenaAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory,
            WebSocketFactory webSocketFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Gardena account '{}'", getThing().getUID().getId());
        scheduler.submit(() -> initializeGardena());
    }

    public void setDiscoveryService(GardenaDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Initializes the GardenaSmart account.
     * This method is called on a thread.
     */
    private synchronized void initializeGardena() {
        try {
            GardenaConfig gardenaConfig = getThing().getConfiguration().as(GardenaConfig.class);
            logger.debug("{}", gardenaConfig);

            String id = getThing().getUID().getId();
            gardenaSmart = new GardenaSmartImpl(id, gardenaConfig, this, scheduler, httpClientFactory,
                    webSocketFactory);
            final GardenaDeviceDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();
            }
            reInitializationCausedBy429 = false;
            updateStatus(ThingStatus.ONLINE);
        } catch (GardenaException ex) {
            logger.warn("{}", ex.getMessage());

            long secondsUntilRestart;

            synchronized (reInitializationCodeLock) {
                boolean isHttp429Error = (ex.getStatus() == 429);
                boolean isRepeatError = lastErrorTime.plus(REINITIALIZE_DELAY_MINUTES_BACK_OFF)
                        .isAfter(LocalDateTime.now());

                Duration delay = isHttp429Error ? REINITIALIZE_DELAY_HOURS_LIMIT_EXCEEDED
                        : isRepeatError ? REINITIALIZE_DELAY_MINUTES_BACK_OFF : REINITIALIZE_DELAY_SECONDS;

                ScheduledFuture<?> reInitializationTask = this.reInitializationTask;
                if (reInitializationTask == null || reInitializationTask.isDone()
                        || (isHttp429Error != reInitializationCausedBy429)) {
                    reInitializationTask = scheduleReinitialize(delay);
                }
                reInitializationCausedBy429 = isHttp429Error;
                lastErrorTime = LocalDateTime.now();
                secondsUntilRestart = reInitializationTask.getDelay(TimeUnit.SECONDS);
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Auto reconnect @ "
                    + LocalTime.now().plusSeconds(secondsUntilRestart).truncatedTo(ChronoUnit.SECONDS).toString());

            disposeGardena();
        }
    }

    /**
     * Re-initializes the GardenaSmart account.
     * This method is called on a thread.
     */
    private synchronized void reIninitializeGardena() {
        if (getThing().getStatus() != ThingStatus.UNINITIALIZED) {
            initializeGardena();
        }
    }

    /**
     * Schedules a reinitialization, if Gardena smart system account is not reachable.
     *
     * @return
     *
     * @return pointer to the reinitialization task
     */
    private ScheduledFuture<?> scheduleReinitialize(Duration delay) {
        ScheduledFuture<?> reInitializationTask = this.reInitializationTask;
        if (reInitializationTask != null) {
            reInitializationTask.cancel(false);
        }
        reInitializationTask = scheduler.schedule(() -> reIninitializeGardena(), delay.getSeconds(), TimeUnit.SECONDS);
        this.reInitializationTask = reInitializationTask;
        return reInitializationTask;
    }

    @Override
    public void dispose() {
        super.dispose();
        synchronized (reInitializationCodeLock) {
            ScheduledFuture<?> reInitializeTask = this.reInitializationTask;
            if (reInitializeTask != null) {
                reInitializeTask.cancel(false);
            }
            this.reInitializationTask = null;
            this.reInitializationCausedBy429 = false;
        }
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
            this.gardenaSmart = null;
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
            // ****
            // TODO: REALLY ?? should a refresh be causing a complete initialisation of the bridge ???
            // ****
            logger.debug("Refreshing Gardena account '{}'", getThing().getUID().getId());
            disposeGardena();
            scheduler.submit(() -> initializeGardena());
        }
    }

    @Override
    public void onDeviceUpdated(Device device) {
        for (ThingUID thingUID : UidUtils.getThingUIDs(device, getThing())) {
            final Thing gardenaThing = getThing().getThing(thingUID);
            if (gardenaThing == null) {
                logger.debug("No thing exists for thingUID:{}", thingUID);
                continue;
            }
            final ThingHandler thingHandler = gardenaThing.getHandler();
            if (!(thingHandler instanceof GardenaThingHandler)) {
                logger.debug("Handler for thingUID:{} is not a 'GardenaThingHandler' ({})", thingUID, thingHandler);
                continue;
            }
            final GardenaThingHandler gardenaThingHandler = (GardenaThingHandler) thingHandler;
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
        synchronized (reInitializationCodeLock) {
            reInitializationCausedBy429 = false;
            scheduleReinitialize(REINITIALIZE_DELAY_SECONDS);
        }
    }
}
