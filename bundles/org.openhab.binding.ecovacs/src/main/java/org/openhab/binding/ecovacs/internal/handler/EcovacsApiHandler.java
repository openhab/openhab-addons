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
package org.openhab.binding.ecovacs.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.ClientKeys;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.config.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.discovery.EcovacsDeviceDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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

/**
 * The {@link EcovacsApiHandler} is responsible for connecting to the Ecovacs cloud API account.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsApiHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EcovacsApiHandler.class);

    private @Nullable EcovacsDeviceDiscoveryService discoveryService;
    private @Nullable EcovacsApi api;
    private @Nullable Future<?> loginFuture;
    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;

    public EcovacsApiHandler(Bridge bridge, HttpClientFactory httpClientFactory, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.localeProvider = localeProvider;
    }

    public void setDiscoveryService(EcovacsDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Nullable
    public EcovacsApi getApi() {
        return api;
    }

    @Nullable
    public EcovacsApi createApiForDevice(String serial) {
        String country = localeProvider.getLocale().getCountry();
        EcovacsApi api = this.api;
        if (api == null || country.isEmpty()) {
            return null;
        }
        return createApi("-" + serial, country);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Ecovacs account '{}'", getThing().getUID().getId());
        // The API expects us to provide a unique device ID during authentication, so generate one once
        // and keep it in configuration afterwards
        if (!getConfig().keySet().contains("installId")) {
            Configuration newConfig = editConfiguration();
            newConfig.put("installId", UUID.randomUUID().toString());
            updateConfiguration(newConfig);
        }
        initializeApi();
    }

    @Override
    public void dispose() {
        super.dispose();
        final EcovacsDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.stopScan();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(EcovacsDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing Ecovacs API account '{}'", getThing().getUID().getId());
            initializeApi();
        }
    }

    public void onLoginExpired() {
        logger.debug("Ecovacs API login for account '{}' expired, logging in again", getThing().getUID().getId());
        final EcovacsApi api = this.api;
        if (api != null) {
            loginToApi(api);
        }
    }

    private void initializeApi() {
        String country = localeProvider.getLocale().getCountry();
        if (country.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-no-country");
            return;
        }

        EcovacsApi api = createApi("", country);
        loginToApi(api);
    }

    private EcovacsApi createApi(String deviceIdSuffix, String country) {
        EcovacsApiConfiguration config = getConfigAs(EcovacsApiConfiguration.class);
        String deviceId = config.installId + deviceIdSuffix;
        org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration apiConfig = new org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration(
                deviceId, config.email, config.password, config.continent, country, "EN", ClientKeys.CLIENT_KEY,
                ClientKeys.CLIENT_SECRET, ClientKeys.AUTH_CLIENT_KEY, ClientKeys.AUTH_CLIENT_SECRET);

        return EcovacsApi.create(httpClientFactory.getCommonHttpClient(), apiConfig);
    }

    private synchronized void loginToApi(final EcovacsApi api) {
        Future<?> loginFuture = this.loginFuture;
        if (loginFuture != null && !loginFuture.isDone()) {
            return;
        }
        loginFuture = scheduler.submit(() -> {
            try {
                api.loginAndGetAccessToken();
                this.api = api;
                updateStatus(ThingStatus.ONLINE);

                logger.debug("Ecovacs API initialized");
                final EcovacsDeviceDiscoveryService discoveryService = this.discoveryService;
                if (discoveryService != null) {
                    discoveryService.startScan();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE);
                this.api = null;
            } catch (EcovacsApiException e) {
                logger.debug("Ecovacs API login failed", e);
                this.api = null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }
}
