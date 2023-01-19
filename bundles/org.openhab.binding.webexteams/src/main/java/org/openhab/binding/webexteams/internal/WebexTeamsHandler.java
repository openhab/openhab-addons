/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.webexteams.internal;

import static org.openhab.binding.webexteams.internal.WebexTeamsBindingConstants.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.webexteams.internal.api.Message;
import org.openhab.binding.webexteams.internal.api.Person;
import org.openhab.binding.webexteams.internal.api.WebexTeamsApi;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebexTeamsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexTeamsHandler extends BaseThingHandler implements AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(WebexTeamsHandler.class);

    // Object to synchronize refresh on
    private final Object refreshSynchronization = new Object();

    private @NonNullByDefault({}) WebexTeamsConfiguration config;

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private @Nullable WebexTeamsApi client;

    private @Nullable OAuthClientService authService;

    private boolean configured; // is the handler instance properly configured?
    private volatile boolean active; // is the handler instance active?
    String accountType = ""; // bot or person?

    private @Nullable Future<?> refreshFuture;

    public WebexTeamsHandler(Thing thing, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(thing);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported on any channel
    }

    // creates list of available Actions
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(WebexTeamsActions.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", this.getThing().getUID());
        active = true;
        this.configured = false;
        config = getConfigAs(WebexTeamsConfiguration.class);

        final String token = config.token;
        final String clientId = config.clientId;
        final String clientSecret = config.clientSecret;

        if (!token.isBlank()) { // For bots
            logger.debug("I think I'm a bot.");
            try {
                createBotOAuthClientService(config);
            } catch (WebexTeamsException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorNotAuth");
                return;
            }
        } else if (!clientId.isBlank()) { // For integrations
            logger.debug("I think I'm a person.");
            if (clientSecret.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorNoSecret");
                return;
            }
            createIntegrationOAuthClientService(config);
        } else { // If no bot or integration credentials, go offline
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorTokenOrId");
            return;
        }

        OAuthClientService localAuthService = this.authService;
        if (localAuthService == null) {
            logger.warn("authService not properly initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "authService not properly initialized");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        this.client = new WebexTeamsApi(localAuthService, httpClient);

        // Start with update status by calling Webex. If no credentials available no polling should be started.
        scheduler.execute(this::startRefresh);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", this.getThing().getUID());
        active = false;
        OAuthClientService authService = this.authService;
        if (authService != null) {
            authService.removeAccessTokenRefreshListener(this);
        }
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
        cancelSchedulers();
    }

    private void createIntegrationOAuthClientService(WebexTeamsConfiguration config) {
        String thingUID = this.getThing().getUID().getAsString();
        logger.debug("Creating OAuth Client Service for {}", thingUID);
        OAuthClientService service = oAuthFactory.createOAuthClientService(thingUID, OAUTH_TOKEN_URL, OAUTH_AUTH_URL,
                config.clientId, config.clientSecret, OAUTH_SCOPE, false);
        service.addAccessTokenRefreshListener(this);
        this.authService = service;
        this.configured = true;
    }

    private void createBotOAuthClientService(WebexTeamsConfiguration config) throws WebexTeamsException {
        String thingUID = this.getThing().getUID().getAsString();
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(config.token);
        response.setScope(OAUTH_SCOPE);
        response.setTokenType("Bearer");
        response.setExpiresIn(Long.MAX_VALUE); // Bot access tokens don't expire
        logger.debug("Creating OAuth Client Service for {}", thingUID);
        OAuthClientService service = oAuthFactory.createOAuthClientService(thingUID, OAUTH_TOKEN_URL,
                OAUTH_AUTHORIZATION_URL, "not used", null, OAUTH_SCOPE, false);
        try {
            service.importAccessTokenResponse(response);
        } catch (OAuthException e) {
            throw new WebexTeamsException("Failed to create oauth client with bot token", e);
        }
        this.authService = service;
        this.configured = true;
    }

    boolean isConfigured() {
        return configured;
    }

    protected String authorize(String redirectUri, String reqCode) throws WebexTeamsException {
        try {
            logger.debug("Make call to Webex to get access token.");

            // Not doing anything with the token. It's used indirectly through authService.
            OAuthClientService authService = this.authService;
            if (authService != null) {
                authService.getAccessTokenResponseByAuthorizationCode(reqCode, redirectUri);
            }

            startRefresh();
            final String user = getUser();
            logger.info("Authorized for user: {}", user);

            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new WebexTeamsException("Failed to authorize", e);
        } catch (final OAuthResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new WebexTeamsException("OAuth exception", e);
        }
    }

    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();
        if (accessTokenResponse == null) {
            return false;
        }

        if ("person".equals(this.accountType)) {
            return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                    && accessTokenResponse.getRefreshToken() != null;
        } else {
            // bots don't need no refreshToken!
            return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null;
        }
    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService authService = this.authService;
            return authService == null ? null : authService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public boolean equalsThingUID(String thingUID) {
        return getThing().getUID().getAsString().equals(thingUID);
    }

    public String formatAuthorizationUrl(String redirectUri) {
        try {
            if (this.configured) {
                OAuthClientService authService = this.authService;
                if (authService != null) {
                    return authService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
                } else {
                    logger.warn("AuthService not properly initialized");
                    return "";
                }
            } else {
                return "";
            }
        } catch (final OAuthException e) {
            logger.warn("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    // mainly used to refresh the auth token when using OAuth
    private boolean refresh() {
        synchronized (refreshSynchronization) {
            Person person;
            try {
                WebexTeamsApi client = this.client;
                if (client == null) {
                    logger.warn("Client not properly initialized");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Client not properly initialized");
                    return false;
                }
                person = client.getPerson();
                String type = person.getType();
                if (type == null) {
                    type = "?";
                }
                updateProperty(PROPERTY_WEBEX_TYPE, type);
                this.accountType = type;
                updateProperty(PROPERTY_WEBEX_NAME, person.getDisplayName());

                // Only when the identity is a person:
                if ("person".equalsIgnoreCase(person.getType())) {
                    String status = person.getStatus();
                    updateState(CHANNEL_STATUS, StringType.valueOf(status));
                    DateFormat df = new SimpleDateFormat(ISO8601_FORMAT);
                    String lastActivity = df.format(person.getLastActivity());
                    if (lastActivity != null) {
                        updateState(CHANNEL_LASTACTIVITY, new DateTimeType(lastActivity));
                    }
                }
                updateStatus(ThingStatus.ONLINE);
                return true;
            } catch (WebexTeamsException e) {
                logger.warn("Failed to refresh: {}.  Did you authorize?", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            return false;
        }
    }

    private void startRefresh() {
        synchronized (refreshSynchronization) {
            if (refresh()) {
                cancelSchedulers();
                if (active) {
                    refreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshPeriod,
                            TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * Cancels all running schedulers.
     */
    private synchronized void cancelSchedulers() {
        Future<?> future = this.refreshFuture;
        if (future != null) {
            future.cancel(true);
            this.refreshFuture = null;
        }
    }

    public String getUser() {
        return thing.getProperties().getOrDefault(PROPERTY_WEBEX_NAME, "");
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public String getLabel() {
        return Objects.requireNonNullElse(thing.getLabel(), "");
    }

    /**
     * Sends a message to the default room.
     *
     * @param msg markdown text string to be sent
     *
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendMessage(String msg) {
        Message message = new Message();
        message.setRoomId(config.roomId);
        message.setMarkdown(msg);
        logger.debug("Sending message to default room ({})", config.roomId);
        return sendMessage(message);
    }

    /**
     * Sends a message with file attachment to the default room.
     *
     * @param msg markdown text string to be sent
     * @param attach URL of the attachment
     *
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendMessage(String msg, String attach) {
        Message message = new Message();
        message.setRoomId(config.roomId);
        message.setMarkdown(msg);
        message.setFile(attach);
        logger.debug("Sending message with attachment to default room ({})", config.roomId);
        return sendMessage(message);
    }

    /**
     * Send a message to a specific room
     * 
     * @param roomId roomId of the room to send to
     * @param msg markdown text string to be sent
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendRoomMessage(String roomId, String msg) {
        Message message = new Message();
        message.setRoomId(roomId);
        message.setMarkdown(msg);
        logger.debug("Sending message to room {}", roomId);
        return sendMessage(message);
    }

    /**
     * Send a message to a specific room, with attachment
     * 
     * @param roomId roomId of the room to send to
     * @param msg markdown text string to be sent
     * @param attach URL of the attachment
     * 
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendRoomMessage(String roomId, String msg, String attach) {
        Message message = new Message();
        message.setRoomId(roomId);
        message.setMarkdown(msg);
        message.setFile(attach);
        logger.debug("Sending message with attachment to room {}", roomId);
        return sendMessage(message);
    }

    /**
     * Sends a message to a specific person, identified by email
     * 
     * @param personEmail email address of the person to send to
     * @param msg markdown text string to be sent
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendPersonMessage(String personEmail, String msg) {
        Message message = new Message();
        message.setToPersonEmail(personEmail);
        message.setMarkdown(msg);
        logger.debug("Sending message to {}", personEmail);
        return sendMessage(message);
    }

    /**
     * Sends a message to a specific person, identified by email, with attachment
     * 
     * @param personEmail email address of the person to send to
     * @param msg markdown text string to be sent
     * @param attach URL of the attachment*
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendPersonMessage(String personEmail, String msg, String attach) {
        Message message = new Message();
        message.setToPersonEmail(personEmail);
        message.setMarkdown(msg);
        message.setFile(attach);
        logger.debug("Sending message to {}", personEmail);
        return sendMessage(message);
    }

    /**
     * Sends a <code>Message</code>
     * 
     * @param msg the <code>Message</code> to be sent
     * @return <code>true</code>, if sending the message has been successful and
     *         <code>false</code> in all other cases.
     */
    private boolean sendMessage(Message msg) {
        try {
            WebexTeamsApi client = this.client;
            if (client != null) {
                client.sendMessage(msg);
                return true;
            } else {
                logger.warn("Client not properly initialized");
                return false;
            }
        } catch (WebexTeamsException e) {
            logger.warn("Failed to send message: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
    }
}
