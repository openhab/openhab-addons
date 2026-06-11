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
package org.openhab.binding.smartthings.internal.handler;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartThingsOAuthHandler;
import org.openhab.binding.smartthings.internal.SmartThingsServlet;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnector;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnectorImpl;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.discovery.SmartThingsDiscoveryService;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.SmartThingsApp;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public abstract class SmartThingsBridgeHandler extends BaseBridgeHandler
        implements SmartThingsOAuthHandler, AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(SmartThingsBridgeHandler.class);

    protected SmartThingsBridgeConfig config;

    protected SmartThingsHandlerFactory smartthingsHandlerFactory;
    protected BundleContext bundleContext;
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) SmartThingsApi smartthingsApi;
    private @NonNullByDefault({}) SmartThingsAuthService authService;
    private @NonNullByDefault({}) TranslationProvider translationProvider;
    protected @NonNullByDefault({}) SmartThingsTypeRegistry typeRegistry;
    protected @NonNullByDefault({}) SmartThingsDiscoveryService discoService;
    protected @NonNullByDefault({}) ClientBuilder clientBuilder;
    protected @NonNullByDefault({}) SseEventSourceFactory eventSourceFactory;

    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartThingsNetworkConnector networkConnector;
    private final OAuthFactory oAuthFactory;
    private String installedAppId = "";
    private @Nullable SmartThingsServlet servlet;
    private @Nullable ScheduledFuture<?> webhookRefreshTask;

    private @Nullable WebhookService webHookService;

    private static final long WEBHOOK_REFRESH_INTERVAL_HOURS = 24;

    public SmartThingsBridgeHandler(Bridge bridge, SmartThingsHandlerFactory smartthingsHandlerFactory,
            SmartThingsAuthService authService, TranslationProvider translationProvider, BundleContext bundleContext,
            HttpService httpService, OAuthFactory oAuthFactory, HttpClientFactory httpClientFactory,
            SmartThingsTypeRegistry typeRegistry, ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory,
            @Nullable WebhookService webHookService) {
        super(bridge);

        config = getThing().getConfiguration().as(SmartThingsBridgeConfig.class);

        this.smartthingsHandlerFactory = smartthingsHandlerFactory;
        this.bundleContext = bundleContext;
        this.httpService = httpService;
        this.oAuthFactory = oAuthFactory;
        this.translationProvider = translationProvider;
        this.authService = authService;
        this.httpClientFactory = httpClientFactory;
        this.typeRegistry = typeRegistry;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.webHookService = webHookService;
    }

    public void setWebHookService(WebhookService webHookService) {
        this.webHookService = webHookService;
        registerCloudWebhook();
    }

    public void unsetWebHookService(WebhookService webHookService) {
        if (webHookService.equals(this.webHookService)) {
            removeRefreshTask();
            this.webHookService = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Reference
    public void registerDiscoveryListener(SmartThingsDiscoveryService disco) {
        this.discoService = disco;
        this.discoService.setSmartThingsTypeRegistry(typeRegistry);
    }

    public boolean useDynamicThings() {
        return config.useDynamicThings;
    }

    public String getCallbackUrl() {
        return normalize(config.callbackUrl);
    }

    public boolean appCreated() {
        if ("".equals(config.appName) || "".equals(config.clientId)) {
            return false;
        }

        return true;
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(SmartThingsBridgeConfig.class);

        if (!validateConfig(this.config)) {
            return;
        }

        registerCloudWebhook();
        SmartThingsConverterFactory.registerConverters(typeRegistry);
        registerOAuth(false);

        try {
            registerServlet();
        } catch (SmartThingsException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.unable-register-servlet");
            return;
        }

        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.oauth-service-notinitialized");
            return;
        }
        try {
            AccessTokenResponse response = oAuthService.getAccessTokenResponse();
            if (response != null && response.getAccessToken() != null) {
                setupClient(null);
            } else {
                setStatusToAuthRequired();
            }
        } catch (OAuthResponseException e) {
            setStatusToAuthRequired();
        } catch (Exception e) {
            Locale locale = Locale.getDefault();
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            String text = translationProvider.getText(bundle, "offline.oauth-failed", null, locale);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, text + ":" + e.getMessage());
        }
    }

    private void setStatusToAuthRequired() {
        Locale locale = Locale.getDefault();
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        String text = translationProvider.getText(bundle, "authorize-message", null, locale);
        String authServletPath = getAuthServletPath();

        String msg = text + " <a " + "onclick=\"event.stopPropagation(); " + "var w = 600, h = 500;\r\n"
                + "var left = (screen.width - w) / 2;\r\n" + "var top = (screen.height - h) / 2;\r\n" + "window.open('"
                + authServletPath + "', 'popup', 'width=${w},height=${h},top=${top},left=${left}');"
                + "return false;\">" + authServletPath + "</a>. ";

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
    }

    public void registerOAuth(boolean forceCli) {
        releaseOAuthService();
        OAuthClientService oAuthService;

        // if no user app created, use the SmartThings CLI end point to create the app
        if ("".equals(config.clientId) || forceCli) {
            config.clientId = SmartThingsBindingConstants.CLIENT_ID;
            oAuthService = oAuthFactory.createOAuthClientService(getOAuthServiceHandle(),
                    SmartThingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                    SmartThingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, null,
                    SmartThingsBindingConstants.SMARTTHINGS_SCOPES, true);
        }
        // if the user app created, use the clientId/clientSecret from the app
        else {
            oAuthService = oAuthFactory.createOAuthClientService(getOAuthServiceHandle(),
                    SmartThingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                    SmartThingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, config.clientSecret,
                    SmartThingsBindingConstants.SMARTTHINGS_SCOPES_ST, true);
        }

        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(SmartThingsBridgeHandler.this);
    }

    public void finishOAuth(String eventCallbackUri, String oauthRedirectUri, String code, String verifier)
            throws SmartThingsException {
        org.openhab.core.auth.client.oauth2.OAuthClientService srv = oAuthService;
        if (srv != null) {
            try {
                srv.addExtraAuthField("code_verifier", verifier);
                org.openhab.core.auth.client.oauth2.AccessTokenResponse response = srv
                        .getAccessTokenResponseByAuthorizationCode(code, SmartThingsBindingConstants.REDIRECT_URI);
                if (response.getAccessToken() != null) {
                    logger.debug("SmartThings OAuth token exchange completed");
                    setupClient(eventCallbackUri, oauthRedirectUri);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.failed-to-exchange-token");
                }
            } catch (Exception e) {
                Locale locale = Locale.getDefault();
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                String text = translationProvider.getText(bundle, "offline.failed-to-exchange-token", null, locale);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, text + ":" + e.getMessage());
                throw new SmartThingsException("Token exchange failed:", e);
            }
        }
    }

    protected void setupClient(@Nullable String eventCallbackUri) throws SmartThingsException {
        setupClient(eventCallbackUri, SmartThingsBindingConstants.REDIRECT_URI);
    }

    protected void setupClient(@Nullable String eventCallbackUri, String oauthRedirectUri) throws SmartThingsException {
        final org.openhab.core.auth.client.oauth2.OAuthClientService oAuthService = this.oAuthService;

        if (oAuthService != null) {
            this.networkConnector = new SmartThingsNetworkConnectorImpl(httpClientFactory);
            smartthingsApi = new SmartThingsApi(httpClientFactory, this, networkConnector, clientBuilder,
                    eventSourceFactory);

            if (eventCallbackUri != null) {
                setupApp(eventCallbackUri, oauthRedirectUri);
            }

            registerOAuth(false);
        }
    }

    /**
     * Creates a new {@link SmartThingsServlet}.
     *
     * @return the newly created servlet
     */

    public void registerServlet() throws SmartThingsException {
        try {
            SmartThingsServlet s = new SmartThingsServlet(this, getAuthServletPath(), authService, translationProvider,
                    httpService);
            s.activate();
            servlet = s;
        } catch (Exception e) {
            throw new SmartThingsException("Error during SmartThings servlet startup", e);
        }
    }

    public void registerCloudWebhook() {
        if (webHookService == null) {
            removeRefreshTask();
            return;
        }

        ensureWebhookRefreshTask();

        // Always register at startup because if we have not run for more than 24 hours the webhook might have expired.
        String cloudWebhookUrl = setupWebHookUrl();
        if (cloudWebhookUrl != null) {
            String callbackUrl = getCallbackUrl();
            if (callbackUrl.isBlank()) {
                updateCallbackUrl(cloudWebhookUrl);
            } else if (!callbackUrl.equals(cloudWebhookUrl)) {
                removeRefreshTask();
                removeCloudWebhooks();
            }
        }
    }

    private void removeRefreshTask() {
        ScheduledFuture<?> task = webhookRefreshTask;
        if (task != null) {
            task.cancel(true);
            webhookRefreshTask = null;
        }
    }

    private void ensureWebhookRefreshTask() {
        ScheduledFuture<?> task = webhookRefreshTask;
        if (task == null || task.isCancelled()) {
            // Schedule daily refresh to keep the 30-day TTL from expiring.
            webhookRefreshTask = scheduler.scheduleWithFixedDelay(this::registerCloudWebhook,
                    WEBHOOK_REFRESH_INTERVAL_HOURS, WEBHOOK_REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
        }
    }

    public @Nullable String setupWebHookUrl() {
        logger.debug("Try to register openHAB Cloud webhook");

        try {
            WebhookService service = webHookService;
            if (service != null) {
                Webhook webHook = service.requestWebhook(getAuthCallbackPath()).get();

                URL result = webHook.url();
                String urlSt = result.toString();
                logger.debug("Registered openHAB Cloud webhook: {}", urlSt);
                return urlSt;
            }
        } catch (Exception ex) {
            logger.warn("Could not register openHAB Cloud webhook", ex);
            return null;
        }
        return null;
    }

    private void removeCloudWebhooks() {
        WebhookService service = webHookService;
        if (service != null) {
            service.removeWebhook(getAuthCallbackPath()).join();
        }
    }

    private void updateCallbackUrl(String callbackUrl) {
        config.callbackUrl = callbackUrl;

        Configuration configuration = getConfig();
        configuration.put(SmartThingsBindingConstants.CALLBACK_URL, callbackUrl);
        updateConfiguration(configuration);
    }

    private static String normalize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    public String getAuthServletPath() {
        return SmartThingsServlet.getServletPath(getThing().getUID().getId());
    }

    public String getAuthCallbackPath() {
        return getAuthServletPath() + "/cb";
    }

    protected void updateLocationProperties(@Nullable String location) {
        Map<String, String> properties = new HashMap<>(editProperties());
        if (location == null) {
            properties.put(SmartThingsBindingConstants.LOCATIONS, "");
        } else {
            String locations = getLocations();
            Set<String> locationSet = new LinkedHashSet<>();
            if (!locations.isBlank()) {
                for (String loc : locations.split(";")) {
                    if (!loc.isBlank()) {
                        locationSet.add(loc.trim());
                    }
                }
            }

            locationSet.add(location.trim());
            String updatedLocations = String.join(";", locationSet);
            properties.put(SmartThingsBindingConstants.LOCATIONS, updatedLocations);
        }
        updateProperties(properties);
    }

    public String getLocations() {
        return this.thing.getProperties().getOrDefault(SmartThingsBindingConstants.LOCATIONS, "");
    }

    public boolean hasLocations() {
        String locations = this.thing.getProperties().getOrDefault(SmartThingsBindingConstants.LOCATIONS, "");
        return !(locations.isBlank());
    }

    protected void updateConfig(String appName, String oAuthClientId, String oAuthClientSecret) {
        config.appName = appName;
        config.clientId = oAuthClientId;
        config.clientSecret = oAuthClientSecret;

        Configuration config = getConfig();
        config.put("appName", appName);
        config.put("clientId", oAuthClientId);
        config.put("clientSecret", oAuthClientSecret);
        updateConfiguration(config);
    }

    protected void setupApp(String eventCallbackUri, String oauthRedirectUri) throws SmartThingsException {
        Optional<SmartThingsApp> appOptional = Optional.empty();
        if (!config.appName.isBlank()) {
            try {
                appOptional = smartthingsApi.getAppByName(config.appName);
            } catch (SmartThingsException e) {
                logger.debug("Unable to look up existing SmartThings app {}", config.appName, e);
            }
        }

        SmartThingsException lastExp = null;
        if (appOptional.isPresent()) {
            smartthingsApi.updateApp(appOptional.get().appId, config.appName, eventCallbackUri, oauthRedirectUri);
        } else {
            int retry = 0;
            boolean success = false;
            AppResponse appResponse = null;
            while (!success && retry < 3) {
                String appName = config.appName;
                if (retry > 0 || "".equals(appName)) {
                    appName = "openhab" + new Random().nextInt(65536);
                }

                try {
                    appResponse = smartthingsApi.createApp(appName, eventCallbackUri, oauthRedirectUri);

                    updateConfig(appName, appResponse.oauthClientId, appResponse.oauthClientSecret);

                    // We need to update oAuth as we create new application with clientId/clientSecret
                    success = true;
                } catch (SmartThingsException ex) {
                    logger.debug("failed to create app {}", appName);
                    lastExp = ex;
                }

                retry++;
            }

            if (!success) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.unable-to-create-app");
                if (lastExp != null) {
                    throw new SmartThingsException("Unable to create SmartThings app", lastExp);
                } else {
                    throw new SmartThingsException("Unable to create SmartThings app");
                }
            }
        }
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
    }

    @Nullable
    public SmartThingsApi getSmartThingsApi() {
        return smartthingsApi;
    }

    @Override
    public void dispose() {
        removeRefreshTask();
        removeCloudWebhooks();

        SmartThingsServlet s = servlet;
        if (s != null) {
            s.deactivate();
            servlet = null;
        }
        releaseOAuthService();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        removeOAuthServiceAndToken();
        super.handleRemoval();
    }

    private String getOAuthServiceHandle() {
        return thing.getUID().getAsString();
    }

    private void releaseOAuthService() {
        OAuthClientService service = oAuthService;
        if (service != null) {
            service.removeAccessTokenRefreshListener(this);
            oAuthFactory.ungetOAuthService(getOAuthServiceHandle());
            oAuthService = null;
        }
    }

    private void removeOAuthServiceAndToken() {
        OAuthClientService service = oAuthService;
        if (service != null) {
            service.removeAccessTokenRefreshListener(this);
            oAuthService = null;
        }
        oAuthFactory.deleteServiceAndAccessToken(getOAuthServiceHandle());
    }

    protected boolean validateConfig(SmartThingsBridgeConfig config) {
        return true;
    }

    public SmartThingsHandlerFactory getSmartThingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public boolean isAuthorized() throws SmartThingsException {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    public @Nullable AccessTokenResponse getAccessTokenResponse() throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            AccessTokenResponse response = oAuthService == null ? null : oAuthService.getAccessTokenResponse();
            if (response != null) {
                String installedAppId = response.getExtraFields().get("installed_app_id");
                if (installedAppId != null) {
                    this.installedAppId = installedAppId;
                }
                return response;
            }
            return null;
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            throw new SmartThingsException("Error during getAccessTokenResponse", e);
        }
    }

    public @Nullable AccessTokenResponse refreshToken() throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.refreshToken();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            throw new SmartThingsException("Error during refreshToken", e);
        }
    }

    public @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if ("".equals(config.appName)) {
                return oAuthService == null ? null
                        : oAuthService
                                .getAccessTokenByClientCredentials(SmartThingsBindingConstants.SMARTTHINGS_SCOPES);
            } else {
                return oAuthService == null ? null
                        : oAuthService
                                .getAccessTokenByClientCredentials(SmartThingsBindingConstants.SMARTTHINGS_SCOPES_ST);
            }
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            logger.debug("Make call to SmartThings to get access token.");
            oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode, redirectUri);
            return reqCode;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SmartThingsException("unable to authorize request", e);
        } catch (final OAuthResponseException e) {
            throw new SmartThingsException("unable to authorize request", e);
        }
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri, String state, boolean useCli) throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }

            String authorizationUri = oAuthService.getAuthorizationUrl(redirectUri, null, state);

            // SmartThings CLI need this additional parameters for authorization
            if (useCli) {
                authorizationUri = authorizationUri + "&client_type=USER_LEVEL";
            }

            return authorizationUri;
        } catch (final OAuthException e) {
            throw new SmartThingsException("Error constructing authorization URL", e);
        }
    }

    public void setInstalledAppId(String installedAppId) {
        this.installedAppId = installedAppId;
    }

    public String getInstalledAppId() {
        return this.installedAppId;
    }

    public SmartThingsNetworkConnector getNetworkConnector() {
        return this.networkConnector;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SmartThingsDiscoveryService.class);
    }

    public SmartThingsTypeRegistry getSmartThingsTypeRegistry() {
        return this.typeRegistry;
    }
}
