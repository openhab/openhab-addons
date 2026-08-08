/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.APP_KEY;
import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.AUTH_CLIENT_KEY;
import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.AUTH_CLIENT_SECRET;
import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.CLIENT_KEY;
import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.CLIENT_SECRET;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.jivesoftware.smack.util.Objects;
import org.openhab.binding.ecovacs.internal.action.EcovacsApiActions;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi.Credentials;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.util.SchedulerTask;
import org.openhab.binding.ecovacs.internal.config.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.discovery.EcovacsDeviceDiscoveryService;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EcovacsApiHandler} is responsible for connecting to the Ecovacs cloud API account.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsApiHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EcovacsApiHandler.class);

    private Optional<EcovacsDeviceDiscoveryService> discoveryService = Optional.empty();
    private SchedulerTask credentialsCheckTask;
    private SchedulerTask tokenRefreshTask;
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private @Nullable EcovacsApi api;

    public EcovacsApiHandler(Bridge bridge, HttpClient httpClient, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
        this.credentialsCheckTask = new SchedulerTask(scheduler, logger, "Credentials Check", this::checkCredentials);
        this.tokenRefreshTask = new SchedulerTask(scheduler, logger, "Token Refresh", this::refreshToken);
    }

    public void setDiscoveryService(EcovacsDeviceDiscoveryService discoveryService) {
        this.discoveryService = Optional.of(discoveryService);
    }

    public EcovacsApi getApi() {
        return Objects.requireNonNull(this.api);
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

        String country = localeProvider.getLocale().getCountry();
        if (country.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-no-country");
            return;
        }
        this.api = createApi(country);
        credentialsCheckTask.submit();
    }

    @Override
    public void dispose() {
        super.dispose();
        credentialsCheckTask.cancel();
        tokenRefreshTask.cancel();
        discoveryService.ifPresent(ds -> ds.stopScan());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcovacsDeviceDiscoveryService.class, EcovacsApiActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public boolean requestVerificationCode() {
        try {
            logger.debug("Requesting verification code for Ecovacs API account '{}'", getThing().getUID().getId());
            getApi().startLoginAndRequestVerificationCode();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("Ecovacs API request for device verification code failed", e);
        }
        return false;
    }

    public boolean enterVerificationCode(String verificationCode) {
        try {
            logger.debug("Entering verification code for Ecovacs API account '{}'", getThing().getUID().getId());
            Credentials creds = getApi().finishLogin(verificationCode);
            cacheCredentials(creds);
            scheduleTokenRefresh(creds);
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("Ecovacs API request to enter device verification code failed", e);
        }
        return false;
    }

    public void onLoginExpired() {
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        logger.debug("Ecovacs API login for account '{}' expired", getThing().getUID().getId());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.config-error-login-required");
        try {
            Files.deleteIfExists(getCredentialsCacheFile());
        } catch (IOException e) {
            logger.debug("Could not delete credentials cache file", e);
        }
    }

    private EcovacsApi createApi(String country) {
        EcovacsApiConfiguration config = getConfigAs(EcovacsApiConfiguration.class);
        var apiConfig = new org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration(config.installId,
                config.email, config.password, config.continent, country, "EN", CLIENT_KEY, CLIENT_SECRET,
                AUTH_CLIENT_KEY, AUTH_CLIENT_SECRET, APP_KEY);

        return EcovacsApi.create(httpClient, apiConfig);
    }

    private void checkCredentials() {
        Credentials creds = restoreCachedCredentials();
        if (creds == null || creds.isExpired()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-login-required");
            return;
        }

        try {
            EcovacsApi api = getApi();
            logger.debug("Restoring cached credentials for Ecovacs API account '{}'", getThing().getUID().getId());
            api.testAndSetCredentials(creds);
            scheduleTokenRefresh(creds);
            updateStatus(ThingStatus.ONLINE);
            discoveryService.ifPresent(ds -> ds.startScanningWithApi(api));
            logger.debug("Ecovacs API initialized");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE);
        } catch (EcovacsApiException e) {
            logger.debug("Ecovacs API login failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshToken() {
        try {
            logger.debug("Refreshing access token for Ecovacs API account '{}'", getThing().getUID().getId());
            Credentials creds = getApi().refreshCredentials();
            cacheCredentials(creds);
            scheduleTokenRefresh(creds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("Token refresh failed", e);
        }
    }

    private void scheduleTokenRefresh(Credentials creds) {
        long expiresInSeconds = (creds.expiryTimestampMs() - System.currentTimeMillis()) / 1000;
        long minRemainderBeforeExpiry = 6 * 60 * 60; // 6 hours
        tokenRefreshTask.cancel();
        if (expiresInSeconds < minRemainderBeforeExpiry) {
            tokenRefreshTask.schedule(0);
        } else {
            tokenRefreshTask.schedule(expiresInSeconds - minRemainderBeforeExpiry);
        }
    }

    private void cacheCredentials(Credentials creds) {
        try {
            Path cacheFile = getCredentialsCacheFile();
            String json = new Gson().toJson(creds);
            Files.writeString(cacheFile, json);
        } catch (IOException e) {
            logger.debug("Could not cache credentials", e);
        }
    }

    private @Nullable Credentials restoreCachedCredentials() {
        try {
            Path cacheFile = getCredentialsCacheFile();
            if (!Files.exists(cacheFile)) {
                return null;
            }
            String json = Files.readString(cacheFile);
            return new Gson().fromJson(json, Credentials.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Could not restore credentials", e);
            return null;
        }
    }

    private Path getCredentialsCacheFile() throws IOException {
        Path cacheDir = Paths.get(OpenHAB.getUserDataFolder(), "cache", getThing().getUID().getBindingId());
        Files.createDirectories(cacheDir);
        return cacheDir.resolve(getThing().getUID().getId() + "_creds_cache.json");
    }
}
