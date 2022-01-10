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
package org.openhab.binding.openuv.internal.handler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openuv.internal.OpenUVException;
import org.openhab.binding.openuv.internal.config.BridgeConfiguration;
import org.openhab.binding.openuv.internal.discovery.OpenUVDiscoveryService;
import org.openhab.binding.openuv.internal.json.OpenUVResponse;
import org.openhab.binding.openuv.internal.json.OpenUVResult;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link OpenUVBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class OpenUVBridgeHandler extends BaseBridgeHandler {
    private static final String QUERY_URL = "https://api.openuv.io/api/v1/uv?lat=%s&lng=%s&alt=%s";
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(OpenUVBridgeHandler.class);
    private final Properties header = new Properties();
    private final Gson gson;
    private final LocationProvider locationProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private @Nullable ScheduledFuture<?> reconnectJob;

    public OpenUVBridgeHandler(Bridge bridge, LocationProvider locationProvider, TranslationProvider i18nProvider,
            LocaleProvider localeProvider, Gson gson) {
        super(bridge);
        this.gson = gson;
        this.locationProvider = locationProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV API bridge handler.");
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        if (config.apikey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unknown-apikey");
            return;
        }
        header.put("x-access-token", config.apikey);
        initiateConnexion();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = this.reconnectJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        reconnectJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            initiateConnexion();
            return;
        }
        logger.debug("The OpenUV bridge only handles Refresh command and not '{}'", command);
    }

    private void initiateConnexion() {
        // Just checking if the provided api key is a valid one by making a fake call
        getUVData("0", "0", "0");
    }

    public @Nullable OpenUVResult getUVData(String latitude, String longitude, String altitude) {
        try {
            String jsonData = HttpUtil.executeUrl("GET", String.format(QUERY_URL, latitude, longitude, altitude),
                    header, null, null, REQUEST_TIMEOUT_MS);
            OpenUVResponse uvResponse = gson.fromJson(jsonData, OpenUVResponse.class);
            if (uvResponse != null) {
                String error = uvResponse.getError();
                if (error == null) {
                    updateStatus(ThingStatus.ONLINE);
                    return uvResponse.getResult();
                }
                throw new OpenUVException(error);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (OpenUVException e) {
            if (e.isQuotaError()) {
                LocalDate today = LocalDate.now();
                LocalDate tomorrow = today.plusDays(1);
                LocalDateTime tomorrowMidnight = tomorrow.atStartOfDay().plusMinutes(2);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                        .format("@text/offline.comm-error-quota-exceeded [ \"%s\" ]", tomorrowMidnight.toString()));

                reconnectJob = scheduler.schedule(this::initiateConnexion,
                        Duration.between(LocalDateTime.now(), tomorrowMidnight).toMinutes(), TimeUnit.MINUTES);
            } else {
                updateStatus(ThingStatus.OFFLINE,
                        e.isApiKeyError() ? ThingStatusDetail.CONFIGURATION_ERROR : ThingStatusDetail.NONE,
                        e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OpenUVDiscoveryService.class);
    }

    public @Nullable PointType getLocation() {
        return locationProvider.getLocation();
    }

    public TranslationProvider getI18nProvider() {
        return i18nProvider;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }
}
