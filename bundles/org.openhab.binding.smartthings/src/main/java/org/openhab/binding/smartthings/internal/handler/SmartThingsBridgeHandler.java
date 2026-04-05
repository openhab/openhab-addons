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
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsAccountHandler;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartThingsServlet;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnector;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnectorImpl;
import org.openhab.binding.smartthings.internal.discovery.SmartThingsDiscoveryService;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.io.openhabcloud.WebhookService;
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
        implements SmartThingsAccountHandler, AccessTokenRefreshListener {
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
    private @Nullable final WebhookService webHookService;

    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartThingsNetworkConnector networkConnector;
    private final OAuthFactory oAuthFactory;
    private String installedAppId = "";
    private @Nullable SmartThingsServlet servlet;
    private @Nullable ScheduledFuture<?> webhookRefreshTask;
    private @Nullable String cloudWebHook;

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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Reference
    public void registerDiscoveryListener(SmartThingsDiscoveryService disco) {
        this.discoService = disco;
        this.discoService.setSmartThingsTypeRegistry(typeRegistry);
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

        registerOAuth(false);
        registerCloudWebhook();

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
                updateStatus(ThingStatus.ONLINE);
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

        String msg = text + " <a " + "onclick=\"event.stopPropagation(); " + "var w = 600, h = 500;\r\n"
                + "var left = (screen.width - w) / 2;\r\n" + "var top = (screen.height - h) / 2;\r\n"
                + "window.open('/smartthings', 'popup', 'width=${w},height=${h},top=${top},left=${left}');"
                + "return false;\">/smartthings</a>. ";

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
    }

    public void registerOAuth(boolean forceCli) {
        OAuthClientService oAuthService;

        // if no user app created, use the smarthings cli end point to create the app
        if ("".equals(config.clientId) || forceCli) {
            config.clientId = SmartThingsBindingConstants.CLIENT_ID;
            oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                    SmartThingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                    SmartThingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, null,
                    SmartThingsBindingConstants.SMARTTHINGS_SCOPES, true);

        }
        // if the user app created, use the clientId/clientSecret from the app
        else {
            oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                    SmartThingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                    SmartThingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, config.clientSecret,
                    SmartThingsBindingConstants.SMARTTHINGS_SCOPES_ST, true);

        }

        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(SmartThingsBridgeHandler.this);
    }

    public void finishOAuth(String eventCallbackuri, String code, String verifier) throws SmartThingsException {
        org.openhab.core.auth.client.oauth2.OAuthClientService srv = oAuthService;
        if (srv != null) {
            try {
                srv.addExtraAuthField("code_verifier", verifier);
                org.openhab.core.auth.client.oauth2.AccessTokenResponse response = srv
                        .getAccessTokenResponseByAuthorizationCode(code, SmartThingsBindingConstants.REDIRECT_URI);
                if (response.getAccessToken() != null) {
                    logger.info("token: {}", response.getAccessToken());
                    setupClient(eventCallbackuri);
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

    protected void setupClient(@Nullable String eventCallbackuri) throws SmartThingsException {
        final org.openhab.core.auth.client.oauth2.OAuthClientService oAuthService = this.oAuthService;

        if (oAuthService != null) {
            this.networkConnector = new SmartThingsNetworkConnectorImpl(httpClientFactory, oAuthService);
            smartthingsApi = new SmartThingsApi(httpClientFactory, this, networkConnector, clientBuilder,
                    eventSourceFactory);

            if (eventCallbackuri != null) {
                setupApp(eventCallbackuri);
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
            SmartThingsServlet s = new SmartThingsServlet(this, authService, translationProvider, httpService);
            s.activate();
            servlet = s;
        } catch (Exception e) {
            throw new SmartThingsException("Error during SmartThings servlet startup", e);
        }
    }

    public void registerCloudWebhook() {
        Map<String, String> properties = thing.getProperties();

        if (config.useCloudWebhook) {
            cloudWebHook = properties.get(SmartThingsBindingConstants.WEBHOOK_URL);
            if (cloudWebHook == null || "".equals(cloudWebHook)) {
                cloudWebHook = setupWebHookUrl();
            }

            // Schedule daily refresh to keep the 30-day TTL from expiring
            webhookRefreshTask = scheduler.scheduleWithFixedDelay(() -> {
                setupWebHookUrl();
            }, WEBHOOK_REFRESH_INTERVAL_HOURS, WEBHOOK_REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);

            if (cloudWebHook != null) {
                updateWebhookProperties(cloudWebHook);
            }
        } else {
            removeRefreshTask();
            removeCloudWebhooks();
            updateWebhookProperties(null);
        }
    }

    public @Nullable String getCloudWebhookUrl() {
        return cloudWebHook;
    }

    private void removeRefreshTask() {
        ScheduledFuture<?> task = webhookRefreshTask;
        if (task != null) {
            task.cancel(true);
            webhookRefreshTask = null;
        }
    }

    public @Nullable String setupWebHookUrl() {
        logger.info("try register webhook");

        try {
            String result = this.webHookService.requestWebhook(SmartThingsBindingConstants.SMARTTHINGS_CB_ALIAS).get();

            logger.info("try register webhook, result={}", result);
            return result;

        } catch (Exception ex) {
            logger.warn("try register webhook failed", ex);
            return null;
        }
    }

    private void removeCloudWebhooks() {
        this.webHookService.removeWebhook(SmartThingsBindingConstants.SMARTTHINGS_CB_ALIAS).join();
    }

    private void updateWebhookProperties(@Nullable String webHookUrl) {
        Map<String, @Nullable String> properties = new HashMap<>(editProperties());
        if (webHookUrl == null) {
            properties.put(SmartThingsBindingConstants.WEBHOOK_URL, "");
        } else {
            properties.put(SmartThingsBindingConstants.WEBHOOK_URL, webHookUrl);
        }
        updateProperties(properties);
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

    protected void setupApp(String eventCallbackuri) throws SmartThingsException {
        boolean appExist = smartthingsApi.isAppExist(config.appName);
        SmartThingsException lastExp = null;
        if (!appExist) {
            int retry = 0;
            boolean success = false;
            AppResponse appResponse = null;
            while (!success && retry < 3) {
                String appName = config.appName;
                if (retry > 0 || "".equals(appName)) {
                    appName = "openhab" + new Random().nextInt(65536);
                }

                try {
                    appResponse = smartthingsApi.createApp(appName, eventCallbackuri);

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
                    throw new SmartThingsException("unable to create SmartThins app", lastExp);
                } else {
                    throw new SmartThingsException("unable to create SmartThins app");
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
        }
        super.dispose();
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
            throw new SmartThingsException("Error durring getAccessTokenResponse", e);
        }
    }

    public @Nullable AccessTokenResponse refreshToken() throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.refreshToken();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            throw new SmartThingsException("Error durring refreshToken", e);
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
            throw new SmartThingsException("Error constructing AuthorizationUr", e);
        }
    }

    public void setInstalledAppId(String installedAppId) {
        this.installedAppId = installedAppId;
    }

    public String getdInstalledAppId() {
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
