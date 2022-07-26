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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.gardena.internal.GardenaBindingConstants;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.GardenaSmartImpl;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.discovery.GardenaDeviceDiscoveryService;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.framework.Bundle;
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

    // timing constants
    private static final Duration REINITIALIZE_DELAY_SECONDS = Duration.ofSeconds(120);
    private static final Duration REINITIALIZE_DELAY_MINUTES_BACK_OFF = Duration.ofMinutes(15).plusSeconds(10);
    private static final Duration REINITIALIZE_DELAY_HOURS_LIMIT_EXCEEDED = Duration.ofHours(24).plusSeconds(10);

    // localisation constants
    private static final String RECONNECT_MSG_KEY = "accounthandler.waiting-to-reconnect-at-time";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(" HH:MM:SS (cccc)");

    // assets
    private @Nullable GardenaDeviceDiscoveryService discoveryService;
    private @Nullable GardenaSmart gardenaSmart;
    private final HttpClientFactory httpClientFactory;
    private final WebSocketFactory webSocketFactory;
    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    // re- initialisation stuff
    private final Object reInitializationCodeLock = new Object();
    private @Nullable ScheduledFuture<?> reInitializationTask;
    private boolean reInitializationCausedBy429 = false;
    private Instant apiCallSuppressionStart = Instant.MIN;
    private Instant apiCallSuppressionEnd = Instant.MIN;

    public GardenaAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory, WebSocketFactory webSocketFactory,
            Bundle bundle, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
        this.bundle = bundle;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    /**
     * Load the api call suppression properties.
     */
    private void loadApiCallSuppressionProperties() {
        Map<String, String> properties = getThing().getProperties();
        String propertyValue;
        propertyValue = properties.getOrDefault(GardenaBindingConstants.API_CALL_SUPPRESSION_START, "");
        apiCallSuppressionStart = "".equals(propertyValue) ? Instant.MIN : Instant.parse(propertyValue);
        propertyValue = properties.getOrDefault(GardenaBindingConstants.API_CALL_SUPPRESSION_END, "");
        apiCallSuppressionEnd = "".equals(propertyValue) ? Instant.MIN : Instant.parse(propertyValue);
    }

    /**
     * Get the duration remaining until the end of the api call suppression window, or Duration.ZERO if we are outside
     * the call suppression window.
     *
     * @return the duration until the end of the suppression window, or zero.
     */
    private Duration getApiCallSuppressionDurationRemaining() {
        Instant now = Instant.now();
        if (now.isAfter(apiCallSuppressionStart) && now.isBefore(apiCallSuppressionEnd)) {
            return Duration.between(now, apiCallSuppressionEnd);
        }
        return Duration.ZERO;
    }

    /**
     * Updates the time when api call suppression begins to now(), and the time when api call suppression ends to now()
     * plus the given suppressionDuration. The suppressionDuration must not be a negative duration. Saves the start and
     * end time values as properties to ensure consistent behaviour across restarts.
     *
     * @param suppressionDuration the duration between the start and end of the suppression window.
     */
    private void apiCallSuppressionWindowUpdate(Duration suppressionDuration) {
        Duration offset = suppressionDuration.isNegative() ? Duration.ZERO : suppressionDuration;
        apiCallSuppressionStart = Instant.now();
        apiCallSuppressionEnd = apiCallSuppressionStart.plus(offset);
        Thing thing = getThing();
        thing.setProperty(GardenaBindingConstants.API_CALL_SUPPRESSION_START, apiCallSuppressionStart.toString());
        thing.setProperty(GardenaBindingConstants.API_CALL_SUPPRESSION_END, apiCallSuppressionEnd.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Gardena account '{}'", getThing().getUID().getId());
        loadApiCallSuppressionProperties();
        Duration delay = getApiCallSuppressionDurationRemaining();
        if (Duration.ZERO.equals(delay)) {
            // do immediate initialisation
            scheduler.submit(() -> initializeGardena());
        } else {
            // delay the initialisation
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, uiText(delay.getSeconds()));
            scheduleReinitialize(delay);
        }
    }

    public void setDiscoveryService(GardenaDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Format a localised description text to display on the thing main UI page.
     *
     * @param delaySeconds the delay that will be added to the current time.
     * @return the (localised) description text.
     */
    private String uiText(long delaySeconds) {
        return i18nProvider.getText(bundle, RECONNECT_MSG_KEY, RECONNECT_MSG_KEY, localeProvider.getLocale())
                + LocalDateTime.now().plusSeconds(delaySeconds).format(DATE_TIME_FORMAT);
    }

    /**
     * Initializes the GardenaSmart account.
     * This method is called on a background thread.
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
            apiCallSuppressionWindowUpdate(Duration.ZERO);
            updateStatus(ThingStatus.ONLINE);
        } catch (GardenaException ex) {
            logger.warn("{}", ex.getMessage());

            long delaySecs;

            synchronized (reInitializationCodeLock) {
                Duration delay;

                int status = ex.getStatus();
                boolean isNetworkError = (status <= 0);
                boolean isHttp429Error = (status == HttpStatus.TOO_MANY_REQUESTS_429);

                if (isNetworkError) {
                    delay = REINITIALIZE_DELAY_SECONDS;
                } else {
                    if (isHttp429Error) {
                        delay = REINITIALIZE_DELAY_HOURS_LIMIT_EXCEEDED;
                    } else {
                        delay = getApiCallSuppressionDurationRemaining().plus(REINITIALIZE_DELAY_MINUTES_BACK_OFF);
                    }
                    apiCallSuppressionWindowUpdate(delay);
                }

                ScheduledFuture<?> reInitializationTask = this.reInitializationTask;
                if (reInitializationTask == null || reInitializationTask.isDone()
                        || (isHttp429Error != reInitializationCausedBy429)) {
                    reInitializationTask = scheduleReinitialize(delay);
                }

                reInitializationCausedBy429 = isHttp429Error;
                delaySecs = reInitializationTask.getDelay(TimeUnit.SECONDS);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, uiText(delaySecs));
            disposeGardena();
        }
    }

    /**
     * Re-initializes the GardenaSmart account.
     * This method is called on a background thread.
     */
    private synchronized void reIninitializeGardena() {
        if (getThing().getStatus() != ThingStatus.UNINITIALIZED) {
            initializeGardena();
        }
    }

    /**
     * Schedules a reinitialization, if Gardena smart system account is not reachable.
     *
     * @return reinitialization task reference
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
        // nothing to do here because the thing has no channels
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
        Duration delay = REINITIALIZE_DELAY_SECONDS;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, uiText(delay.toSeconds()));
        disposeGardena();
        synchronized (reInitializationCodeLock) {
            reInitializationCausedBy429 = false;
            scheduleReinitialize(delay);
        }
    }
}
