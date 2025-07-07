/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.flume.internal.handler;

import static org.openhab.binding.flume.internal.FlumeBindingConstants.THING_TYPE_METER;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.flume.internal.FlumeBridgeConfig;
import org.openhab.binding.flume.internal.api.FlumeApi;
import org.openhab.binding.flume.internal.api.FlumeApiException;
import org.openhab.binding.flume.internal.api.dto.FlumeApiDevice;
import org.openhab.binding.flume.internal.discovery.FlumeDiscoveryService;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FlumeBridgeHandler} implements the Flume bridge cloud connector
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(FlumeBridgeHandler.class);

    public FlumeBridgeConfig config = new FlumeBridgeConfig();

    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(30);
    private ExpiringCache<List<FlumeApiDevice>> apiListDevicesCache = new ExpiringCache<>(CACHE_EXPIRY,
            this::apiListDevicesAction);

    private boolean logOnce = false;

    private final FlumeApi api;
    final SystemOfUnits systemOfUnits;
    final TranslationProvider i18nProvider;
    final LocaleProvider localeProvider;
    final Bundle bundle;

    public FlumeApi getApi() {
        return api;
    }

    protected @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable FlumeDiscoveryService discoveryService;

    /**
     * Get the services registered for this bridge. Provides the discovery service.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FlumeDiscoveryService.class);
    }

    public boolean registerDiscoveryListener(FlumeDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
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

    public FlumeBridgeHandler(final Bridge bridge, SystemOfUnits systemOfUnits, HttpClient httpClient,
            TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(bridge);

        api = new FlumeApi(httpClient);
        this.systemOfUnits = systemOfUnits;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
    }

    public FlumeBridgeConfig getFlumeBridgeConfig() {
        return config;
    }

    @Override
    public void initialize() {
        config = getConfigAs(FlumeBridgeConfig.class);

        if (config.clientId.isBlank() | config.clientSecret.isBlank() || config.password.isBlank()
                || config.username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.cloud-configuration-error");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::goOnline);
    }

    public synchronized void goOnline() {
        try {
            api.initialize(config.clientId, config.clientSecret, config.username, config.password,
                    this.getThing().getUID());
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            handleApiException(e);
            return;
        }

        if (!refreshDevices(true)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.cloud-configuration-error");
            return;
        }

        int pollingPeriod = Math.min(config.refreshIntervalCumulative, config.refreshIntervalInstantaneous);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 0, pollingPeriod, TimeUnit.MINUTES);
        updateStatus(ThingStatus.ONLINE);
    }

    @Nullable
    public List<FlumeApiDevice> apiListDevicesAction() {
        try {
            return api.getDeviceList();
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            handleApiException(e);
            return null;
        }
    }

    /**
     * update the listDevicesCache if expired or forcedUpdate. Will iterate through the list and
     * either notify of discovery to discoveryService or, if the device is already configured, will update
     * the device info.
     *
     * @param forcedUpdate force update
     * @return true if successful in querying the API
     */
    public boolean refreshDevices(boolean forcedUpdate) {
        final FlumeDiscoveryService discovery = discoveryService;

        if (forcedUpdate) {
            apiListDevicesCache.invalidateValue();
        }
        @Nullable
        List<FlumeApiDevice> listDevices = apiListDevicesCache.getValue();

        if (listDevices == null) {
            return false;
        }

        for (FlumeApiDevice dev : listDevices) {
            if (dev.type == 2 && discovery != null) {
                FlumeDeviceHandler deviceHandler = getFlumeDeviceHandler(dev.id);

                if (deviceHandler == null) {
                    // output ID of discovered device to log once to identify ID so it can be used for textual
                    // configuration
                    if (!logOnce) {
                        logger.info("Flume Meter Device Discovered: ID: {}", dev.id);
                        logOnce = true;
                    }
                    discovery.notifyDiscoveryDevice(dev.id);
                } else {
                    deviceHandler.updateDeviceInfo(dev);
                }
            }
        }

        return true;
    }

    /**
     * iterates through the child things to find the handler with the matching id
     *
     * @param id of the Flume device thing to find
     * @return FlumeDeviceHandler or null
     */
    @Nullable
    public FlumeDeviceHandler getFlumeDeviceHandler(String id) {
        return getThing().getThings().stream() //
                .filter(t -> t.getThingTypeUID().equals(THING_TYPE_METER)) //
                .map(t -> (FlumeDeviceHandler) t.getHandler()) //
                .filter(Objects::nonNull) //
                .filter(h -> h.getId().equals(id)) //
                .findFirst() //
                .orElse(null);
    }

    public void handleApiException(Exception e) {
        if (e instanceof FlumeApiException flumeApiException) {
            if (flumeApiException.isConfigurationIssue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        flumeApiException.getLocalizedMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        flumeApiException.getLocalizedMessage());
            }
        } else if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedIOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof TimeoutException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof ExecutionException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else {
            // capture in log since this is an unexpected exception
            logger.warn("Unhandled Exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.toString());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // cloud handler has no channels
    }

    /**
     * iterates through all child things to update usage
     */
    private void pollDevices() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            // try to go online if it is offline due to communication error
            if (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
                goOnline();
            }
            return;
        }

        // refresh listDevicesCache if necessary
        if (apiListDevicesCache.isExpired()) {
            refreshDevices(true);
        }

        getThing().getThings().stream() //
                .map(t -> t.getHandler()) //
                .filter(FlumeDeviceHandler.class::isInstance) //
                .map(FlumeDeviceHandler.class::cast) //
                .forEach(FlumeDeviceHandler::queryUsage);
    }

    public @Nullable String getLocaleString(String key) {
        return i18nProvider.getText(bundle, key, null, localeProvider.getLocale());
    }

    @Override
    public synchronized void dispose() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            pollingJob = null;
        }
    }
}
