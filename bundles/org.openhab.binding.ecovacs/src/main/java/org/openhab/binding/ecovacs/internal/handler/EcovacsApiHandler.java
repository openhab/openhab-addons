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
package org.openhab.binding.ecovacs.internal.handler;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.util.SchedulerTask;
import org.openhab.binding.ecovacs.internal.config.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.discovery.EcovacsDeviceDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.LocaleProvider;
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
    private static final long RETRY_INTERVAL_SECONDS = 120;

    private Optional<EcovacsDeviceDiscoveryService> discoveryService = Optional.empty();
    private SchedulerTask loginTask;
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;

    public EcovacsApiHandler(Bridge bridge, HttpClient httpClient, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
        this.loginTask = new SchedulerTask(scheduler, logger, "API Login", this::loginToApi);
    }

    public void setDiscoveryService(EcovacsDeviceDiscoveryService discoveryService) {
        this.discoveryService = Optional.of(discoveryService);
    }

    public EcovacsApi createApiForDevice(String serial) throws ConfigurationException {
        String country = localeProvider.getLocale().getCountry();
        if (country.isEmpty()) {
            throw new ConfigurationException("@text/offline.config-error-no-country");
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
        updateStatus(ThingStatus.UNKNOWN);
        loginTask.submit();
    }

    @Override
    public void dispose() {
        super.dispose();
        discoveryService.ifPresent(ds -> ds.stopScan());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcovacsDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing Ecovacs API account '{}'", getThing().getUID().getId());
            scheduleLogin(0);
        }
    }

    public void onLoginExpired() {
        logger.debug("Ecovacs API login for account '{}' expired, logging in again", getThing().getUID().getId());
        scheduleLogin(0);
    }

    private void scheduleLogin(long delaySeconds) {
        loginTask.cancel();
        loginTask.schedule(delaySeconds);
    }

    private EcovacsApi createApi(String deviceIdSuffix, String country) {
        EcovacsApiConfiguration config = getConfigAs(EcovacsApiConfiguration.class);
        String deviceId = config.installId + deviceIdSuffix;
        org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration apiConfig = new org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration(
                deviceId, config.email, config.password, config.continent, country, "EN", CLIENT_KEY, CLIENT_SECRET,
                AUTH_CLIENT_KEY, AUTH_CLIENT_SECRET, APP_KEY);

        return EcovacsApi.create(httpClient, apiConfig);
    }

    private void loginToApi() {
        try {
            String country = localeProvider.getLocale().getCountry();
            if (country.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-no-country");
                return;
            }
            EcovacsApi api = createApi("", country);
            api.loginAndGetAccessToken();
            updateStatus(ThingStatus.ONLINE);
            discoveryService.ifPresent(ds -> ds.startScanningWithApi(api));

            logger.debug("Ecovacs API initialized");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE);
        } catch (EcovacsApiException e) {
            logger.debug("Ecovacs API login failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleLogin(RETRY_INTERVAL_SECONDS);
        }
    }
}
