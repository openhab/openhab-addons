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
package org.openhab.binding.webexteams.internal;

import static org.openhab.binding.webexteams.internal.WebexTeamsBindingConstants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.webexteams.WebexTeamsActions;
import org.openhab.binding.webexteams.internal.api.Message;
import org.openhab.binding.webexteams.internal.api.NotAuthenticatedException;
import org.openhab.binding.webexteams.internal.api.Person;
import org.openhab.binding.webexteams.internal.api.WebexTeamsApi;
import org.openhab.binding.webexteams.internal.api.WebexTeamsApiException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class WebexTeamsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WebexTeamsHandler.class);

    private @Nullable WebexTeamsConfiguration config;

    private final HttpClient httpClient;
    private @Nullable WebexTeamsApi client;

    private ThingStatus status = ThingStatus.UNKNOWN;

    private @Nullable ScheduledFuture<?> refreshService = null;

    public WebexTeamsHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported on any channel
    }

    @Override
    public void handleRemoval() {
        updateStatus(ThingStatus.REMOVED);
    }

    // creates list of available Actions
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(WebexTeamsActions.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing...");
        config = getConfigAs(WebexTeamsConfiguration.class);

        updateStatus(this.status);

        final String token = config.token;
        final String clientId = config.clientId;
        final String clientSecret = config.clientSecret;
        final String redirectUrl = config.redirectUrl;
        // final String authCode = config.authCode; // TODO: can this be removed?

        if (token != null && !token.isEmpty()) {
            logger.debug("Token is configured.");

        } else if (clientId != null && !clientId.isEmpty()) {
            logger.debug("We're a person.");
            if (clientSecret == null || clientSecret.isEmpty()) {
                this.status = ThingStatus.OFFLINE;
                updateStatus(this.status, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorNoSecret");
                return;
            }
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                this.status = ThingStatus.OFFLINE;
                updateStatus(this.status, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorNoRedirectUrl");
                return;
            }
        } else {
            this.status = ThingStatus.OFFLINE;
            updateStatus(this.status, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorTokenOrId");
            return;
        }

        // background initialization:
        scheduler.execute(() -> {
            try {
                this.client = new WebexTeamsApi(this, httpClient);
                logger.debug("Trying to fetch account details");
                Person p = this.client.getPerson();
                logger.debug("Success: {}", p.getDisplayName());
            } catch (NotAuthenticatedException e) {
                logger.error("Failed to initialize client", e);
                this.status = ThingStatus.OFFLINE;
                updateStatus(this.status, ThingStatusDetail.CONFIGURATION_ERROR, "@text/confErrorNotAuth");
                return;
            }

            this.status = ThingStatus.ONLINE;
            updateStatus(this.status);

            refresh();

        });

        // TODO: make interval configurable.
        refreshService = scheduler.scheduleWithFixedDelay(this::refresh, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing...");
        // TODO: need to clean up 'client'?
        final ScheduledFuture<?> job = refreshService;
        if (job != null) {
            job.cancel(true);
            refreshService = null;
        }
    }

    // mainly used to refresh the auth token when using OAuth

    // see: https://github.com/webex/webex-java-sdk/blob/master/src/main/java/com/ciscospark/Client.java
    // TODO: only refresh when we've been able to login once first (in initialize)
    private void refresh() {
        if (status == ThingStatus.ONLINE) {
            Person person;
            try {
                person = client.getPerson();
                updateState(CHANNEL_BOTNAME, StringType.valueOf(person.getDisplayName()));
                updateProperty("personType", person.getType());

                // Only when the identity is a person:
                if (person.getType().equalsIgnoreCase("person")) {
                    String status = person.getStatus();
                    updateState(CHANNEL_STATUS, StringType.valueOf(status));
                    DateFormat df = new SimpleDateFormat(ISO8601_FORMAT);
                    String lastActivity = df.format(person.getLastActivity());
                    updateState(CHANNEL_LASTACTIVITY, new DateTimeType(lastActivity));
                }
            } catch (WebexTeamsApiException e) {
                logger.warn("Failed to update display name: {}", e.getMessage());
            }

        }
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
     * @param msg markdown text string to be sent*
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
     * Send a message to a specific room
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
     * @return
     */
    public boolean sendPersonMessage(String personEmail, String msg) {
        Message message = new Message();
        message.setToPersonEmail(personEmail);
        message.setMarkdown(msg);
        logger.debug("Sending message to {}", personEmail);
        return sendMessage(message);
    }

    /**
     * Sends a message to a specific person, identified by email
     * 
     * @param personEmail email address of the person to send to
     * @param msg markdown text string to be sent
     * @param attach URL of the attachment*
     * @return
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
        client.sendMessage(msg);
        return true;
    }

    @Override
    public Configuration editConfiguration() {
        return super.editConfiguration();
    }

    @Override
    public void updateConfiguration(Configuration config) {
        super.updateConfiguration(config);
    }

    @Override
    public void updateProperty(String name, @Nullable String value) {
        super.updateProperty(name, value);
    }

    @Override
    public Configuration getConfig() {
        return super.getConfig();
    }

    @Override
    public <T> T getConfigAs(Class<T> configurationClass) {
        return super.getConfigAs(configurationClass);
    }
}
