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
package org.openhab.binding.vesync.internal.handlers;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.api.VeSyncV2ApiHelper;
import org.openhab.binding.vesync.internal.discovery.DeviceMetaDataUpdatedHandler;
import org.openhab.binding.vesync.internal.discovery.VeSyncDiscoveryService;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthenticatedRequest;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncManagedDeviceBase;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncUserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;
import org.openhab.binding.vesync.internal.exceptions.DeviceUnknownException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeSyncBridgeHandler} is responsible for handling the bridge things created to use the VeSync
 * API. This way, the user credentials may be entered only once.
 *
 * @author David Goodyear - Initial Contribution
 */
@NonNullByDefault
public class VeSyncBridgeHandler extends BaseBridgeHandler implements VeSyncClient {

    private static final int DEFAULT_DEVICE_SCAN_INTERVAL = 600;
    private static final int DEFAULT_DEVICE_SCAN_RECOVERY_INTERVAL = 60;
    private static final int DEFAULT_DEVICE_SCAN_DISABLED = -1;

    private volatile int backgroundScanTime = -1;

    protected final VeSyncV2ApiHelper api;
    private final Logger logger = LoggerFactory.getLogger(VeSyncBridgeHandler.class);
    private final Object scanConfigLock = new Object();

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    private @Nullable ScheduledFuture<?> backgroundDiscoveryPollingJob;

    public VeSyncBridgeHandler(Bridge bridge, @Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        super(bridge);
        api = new VeSyncV2ApiHelper(httpClientFactory.getCommonHttpClient());
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    protected void checkIfIncreaseScanRateRequired() {
        logger.trace("Checking if increased background scanning for new devices / base information is required");
        boolean frequentScanReq = false;
        for (Thing th : getThing().getThings()) {
            ThingHandler handler = th.getHandler();
            if (handler instanceof VeSyncBaseDeviceHandler veSyncBaseDeviceHandler) {
                if (veSyncBaseDeviceHandler.requiresMetaDataFrequentUpdates()) {
                    frequentScanReq = true;
                    break;
                }
            }
        }

        if (!frequentScanReq
                && api.getMacLookupMap().values().stream().anyMatch(x -> "offline".equals(x.connectionStatus))) {
            frequentScanReq = true;
        }

        if (frequentScanReq) {
            setBackgroundScanInterval(DEFAULT_DEVICE_SCAN_RECOVERY_INTERVAL);
        } else {
            setBackgroundScanInterval(DEFAULT_DEVICE_SCAN_INTERVAL);
        }
    }

    protected void setBackgroundScanInterval(final int seconds) {
        synchronized (scanConfigLock) {
            ScheduledFuture<?> job = backgroundDiscoveryPollingJob;
            if (backgroundScanTime != seconds) {
                if (seconds > 0) {
                    logger.trace("Scheduling background scanning for new devices / base information every {} seconds",
                            seconds);
                } else {
                    logger.trace("Disabling background scanning for new devices / base information");
                }
                // Cancel the current scan's and re-schedule as required
                if (job != null && !job.isCancelled()) {
                    job.cancel(true);
                    backgroundDiscoveryPollingJob = null;
                }
                if (seconds > 0) {
                    backgroundDiscoveryPollingJob = scheduler.scheduleWithFixedDelay(
                            this::runDeviceScanSequenceNoAuthErrors, seconds, seconds, TimeUnit.SECONDS);
                }
                backgroundScanTime = seconds;
            }
        }
    }

    public void registerMetaDataUpdatedHandler(DeviceMetaDataUpdatedHandler dmduh) {
        handlers.add(dmduh);
    }

    public void unregisterMetaDataUpdatedHandler(DeviceMetaDataUpdatedHandler dmduh) {
        handlers.remove(dmduh);
    }

    private final CopyOnWriteArrayList<DeviceMetaDataUpdatedHandler> handlers = new CopyOnWriteArrayList<>();

    public void runDeviceScanSequenceNoAuthErrors() {
        try {
            runDeviceScanSequence();
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("bridge.offline.check-credentials"));
        }
    }

    public void runDeviceScanSequence() throws AuthenticationException {
        logger.trace("Scanning for new devices / base information now");
        api.discoverDevices();
        handlers.forEach(x -> x.handleMetadataRetrieved(this));
        checkIfIncreaseScanRateRequired();

        this.updateThings();
    }

    public java.util.stream.Stream<@NotNull VeSyncManagedDeviceBase> getAirPurifiersMetadata() {
        return api.getMacLookupMap().values().stream().filter(x -> !VeSyncBaseDeviceHandler
                .getDeviceFamilyMetadata(x.getDeviceType(), VeSyncDeviceAirPurifierHandler.DEV_TYPE_FAMILY_AIR_PURIFIER,
                        VeSyncDeviceAirPurifierHandler.SUPPORTED_MODEL_FAMILIES)
                .equals(VeSyncBaseDeviceHandler.UNKNOWN));
    }

    public java.util.stream.Stream<@NotNull VeSyncManagedDeviceBase> getAirHumidifiersMetadata() {
        return api.getMacLookupMap().values().stream()
                .filter(x -> !VeSyncBaseDeviceHandler
                        .getDeviceFamilyMetadata(x.getDeviceType(),
                                VeSyncDeviceAirHumidifierHandler.DEV_TYPE_FAMILY_AIR_HUMIDIFIER,
                                VeSyncDeviceAirHumidifierHandler.SUPPORTED_MODEL_FAMILIES)
                        .equals(VeSyncBaseDeviceHandler.UNKNOWN));
    }

    protected void updateThings() {
        final VeSyncBridgeConfiguration config = getConfigAs(VeSyncBridgeConfiguration.class);
        getThing().getThings().forEach((th) -> updateThing(config, th.getHandler()));
    }

    public void updateThing(ThingHandler handler) {
        final VeSyncBridgeConfiguration config = getConfigAs(VeSyncBridgeConfiguration.class);
        updateThing(config, handler);
    }

    private void updateThing(VeSyncBridgeConfiguration config, @Nullable ThingHandler handler) {
        if (handler instanceof VeSyncBaseDeviceHandler veSyncBaseDeviceHandler) {
            veSyncBaseDeviceHandler.updateDeviceMetaData();
            veSyncBaseDeviceHandler.updateBridgeBasedPolls(config);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(VeSyncDiscoveryService.class);
    }

    @Override
    public void initialize() {
        VeSyncBridgeConfiguration config = getConfigAs(VeSyncBridgeConfiguration.class);

        scheduler.submit(() -> {
            final String passwordMd5 = VeSyncV2ApiHelper.calculateMd5(config.password);

            try {
                api.login(config.username, passwordMd5, "Europe/London");
                api.updateBridgeData(this);
                runDeviceScanSequence();
                updateStatus(ThingStatus.ONLINE);
            } catch (final AuthenticationException ae) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        getLocalizedText("bridge.offline.check-credentials"));
                // The background scan will keep trying to authenticate in case the users credentials are updated on the
                // veSync servers,
                // to match the binding's configuration.
            }
        });
    }

    @Override
    public void dispose() {
        setBackgroundScanInterval(DEFAULT_DEVICE_SCAN_DISABLED);
        api.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("{}", getLocalizedText("warning.bridge.unexpected-command-call"));
    }

    public void handleNewUserSession(final @Nullable VeSyncUserSession userSessionData) {
        final Map<String, String> newProps = new HashMap<>();
        if (userSessionData != null) {
            newProps.put(DEVICE_PROP_BRIDGE_REG_TS, userSessionData.registerTime);
            newProps.put(DEVICE_PROP_BRIDGE_COUNTRY_CODE, userSessionData.countryCode);
            newProps.put(DEVICE_PROP_BRIDGE_ACCEPT_LANG, userSessionData.acceptLanguage);
        }
        this.updateProperties(newProps);
    }

    @Override
    public String reqV2Authorized(final String url, final String macId, final VeSyncAuthenticatedRequest requestData)
            throws AuthenticationException, DeviceUnknownException {
        return api.reqV2Authorized(url, macId, requestData);
    }
}
