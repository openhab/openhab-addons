/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.HomeConnectSseClient;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.configuration.ApiBridgeConfiguration;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.LogWriter;
import org.openhab.binding.homeconnect.internal.logger.Type;
import org.openhab.binding.homeconnect.internal.servlet.BridgeConfigurationServlet;
import org.openhab.core.OpenHAB;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.event.Level;

/**
 * The {@link HomeConnectBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectBridgeHandler extends BaseBridgeHandler {

    private static final int REINITIALIZATION_DELAY = 120;
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_ID = "clientId";

    private final OAuthFactory oAuthFactory;
    private final BridgeConfigurationServlet bridgeConfigurationServlet;
    private final LogWriter logger;
    private final EmbeddedLoggingService loggingService;

    private @Nullable ScheduledFuture<?> reinitializationFuture;

    private @NonNullByDefault({}) OAuthClientService oAuthClientService;
    private @NonNullByDefault({}) String oAuthServiceHandleId;
    private @NonNullByDefault({}) HomeConnectApiClient apiClient;
    private @NonNullByDefault({}) HomeConnectSseClient sseClient;

    public HomeConnectBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory,
            BridgeConfigurationServlet bridgeConfigurationServlet, EmbeddedLoggingService loggingService) {
        super(bridge);

        this.oAuthFactory = oAuthFactory;
        this.bridgeConfigurationServlet = bridgeConfigurationServlet;
        this.logger = loggingService.getLogger(HomeConnectBridgeHandler.class);
        this.loggingService = loggingService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not used for bridge
    }

    @Override
    public void initialize() {
        Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
        String openHabVersion = OpenHAB.getVersion();
        logger.debugWithLabel(getThing().getLabel(), "Initialize bridge (Bundle: {}, openHAB: {})", version.toString(),
                openHabVersion);
        // let the bridge configuration servlet know about this handler
        bridgeConfigurationServlet.addBridgeHandler(this);

        // create oAuth service
        ApiBridgeConfiguration config = getConfiguration();
        String tokenUrl = (config.isSimulator() ? API_SIMULATOR_BASE_URL : API_BASE_URL) + OAUTH_TOKEN_PATH;
        String authorizeUrl = (config.isSimulator() ? API_SIMULATOR_BASE_URL : API_BASE_URL) + OAUTH_AUTHORIZE_PATH;
        String oAuthServiceHandleId = thing.getUID().getAsString() + (config.isSimulator() ? "simulator" : "");

        oAuthClientService = oAuthFactory.createOAuthClientService(oAuthServiceHandleId, tokenUrl, authorizeUrl,
                config.getClientId(), config.getClientSecret(), OAUTH_SCOPE, true);
        this.oAuthServiceHandleId = oAuthServiceHandleId;
        logger.log(Type.DEFAULT, Level.DEBUG, null, getThing().getLabel(),
                Arrays.asList("tokenUrl: " + tokenUrl, "authorizeUrl: " + authorizeUrl,
                        "oAuthServiceHandleId: " + oAuthServiceHandleId, "scope: " + OAUTH_SCOPE.toString(),
                        "oAuthClientService: " + oAuthClientService.toString()),
                null, null, "Initialize oAuth client service.");

        // create api client
        apiClient = new HomeConnectApiClient(oAuthClientService, config.isSimulator(), loggingService);
        sseClient = new HomeConnectSseClient(oAuthClientService, config.isSimulator(), loggingService);

        try {
            AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();

            if (accessTokenResponse == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Please authenticate your account at http(s)://[YOUROPENHAB]:[YOURPORT]/homeconnect (e.g. http://192.168.178.100:8080/homeconnect).");
                logger.infoWithLabel(getThing().getLabel(),
                        "Configuration is pending. Please authenticate your account at http(s)://[YOUROPENHAB]:[YOURPORT]/homeconnect (e.g. http://192.168.178.100:8080/homeconnect).");
            } else {
                apiClient.getHomeAppliances();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (OAuthException | IOException | OAuthResponseException | CommunicationException
                | AuthorizationException e) {
            ZonedDateTime nextReinitializeDateTime = ZonedDateTime.now().plusSeconds(REINITIALIZATION_DELAY);

            String infoMessage = "Home Connect service is not reachable or a problem occurred! Retrying at "
                    + nextReinitializeDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME) + " (" + e.getMessage()
                    + ").";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, infoMessage);
            logger.infoWithLabel(getThing().getLabel(), infoMessage);

            scheduleReinitialize(REINITIALIZATION_DELAY);
        }
    }

    @Override
    public void dispose() {
        logger.debugWithLabel(getThing().getLabel(), "Dispose bridge");

        stopReinitializer();
        cleanup();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isModifyingCurrentConfig(configurationParameters)) {
            List<String> parameters = configurationParameters.entrySet().stream().map((entry) -> {
                if (CLIENT_ID.equals(entry.getKey()) || CLIENT_SECRET.equals(entry.getKey())) {
                    return entry.getKey() + ": ***";
                }
                return entry.getKey() + ": " + entry.getValue();
            }).collect(Collectors.toList());

            logger.log(Type.DEFAULT, Level.INFO, null, getThing().getLabel(), parameters, null, null,
                    "Update bridge configuration.");

            validateConfigurationParameters(configurationParameters);
            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }

            // invalidate oAuth credentials
            try {
                logger.infoWithLabel(getThing().getLabel(), "Clear oAuth credential store.");
                if (oAuthClientService != null) {
                    oAuthClientService.remove();
                }
            } catch (OAuthException e) {
                logger.errorWithHaId(getThing().getLabel(), "Could not clear oAuth credentials.", e);
            }

            if (isInitialized()) {
                // persist new configuration and reinitialize handler
                dispose();
                updateConfiguration(configuration);
                initialize();
            } else {
                // persist new configuration and notify Thing Manager
                updateConfiguration(configuration);
                ThingHandlerCallback callback = getCallback();
                if (callback != null) {
                    callback.configurationUpdated(this.getThing());
                } else {
                    logger.warn(
                            "Handler {} tried updating its configuration although the handler was already disposed.",
                            this.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Allow clients to register (start) SSE listener.
     *
     * @param abstractHomeConnectThingHandler
     */
    public void registerServerSentEventListener(AbstractHomeConnectThingHandler abstractHomeConnectThingHandler) {
        if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
            try {
                sseClient.registerServerSentEventListener(abstractHomeConnectThingHandler.getThingHaId(),
                        abstractHomeConnectThingHandler);
            } catch (CommunicationException | AuthorizationException e) {
                logger.errorWithLabel(getThing().getLabel(),
                        "Could not start SSE connection for child handler. handler={} error={}",
                        abstractHomeConnectThingHandler, e.getMessage());
            }
        }
    }

    /**
     * Unregister SSE listener.
     *
     * @param abstractHomeConnectThingHandler
     */
    public void unregisterServerSentEventListener(AbstractHomeConnectThingHandler abstractHomeConnectThingHandler) {
        sseClient.unregisterServerSentEventListener(abstractHomeConnectThingHandler);
    }

    /**
     * Get {@link HomeConnectApiClient}.
     *
     * @return api client instance
     */
    public HomeConnectApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Get {@link ApiBridgeConfiguration}.
     *
     * @return bridge configuration (clientId, clientSecret, etc.)
     */
    public ApiBridgeConfiguration getConfiguration() {
        return getConfigAs(ApiBridgeConfiguration.class);
    }

    /**
     * Get {@link OAuthClientService} instance.
     *
     * @return oAuth client service instance
     */
    public OAuthClientService getOAuthClientService() {
        return oAuthClientService;
    }

    private void cleanup() {
        sseClient.dispose();

        if (oAuthServiceHandleId != null) {
            oAuthFactory.ungetOAuthService(oAuthServiceHandleId);
        }

        bridgeConfigurationServlet.removeBridgeHandler(this);
    }

    private synchronized void scheduleReinitialize(int seconds) {
        ScheduledFuture<?> reinitializationFuture = this.reinitializationFuture;
        if (reinitializationFuture != null && !reinitializationFuture.isDone()) {
            logger.debugWithLabel(getThing().getLabel(),
                    "Reinitialization is already scheduled. Starting in {} seconds.",
                    reinitializationFuture.getDelay(TimeUnit.SECONDS));
        } else {
            this.reinitializationFuture = scheduler.schedule(() -> {
                cleanup();
                initialize();
            }, seconds, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopReinitializer() {
        ScheduledFuture<?> reinitializationFuture = this.reinitializationFuture;
        if (reinitializationFuture != null) {
            reinitializationFuture.cancel(true);
        }
    }

}
