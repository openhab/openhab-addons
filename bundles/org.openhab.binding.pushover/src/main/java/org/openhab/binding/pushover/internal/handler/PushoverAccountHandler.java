/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.pushover.internal.handler;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.DEFAULT_SOUND;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pushover.internal.actions.PushoverActions;
import org.openhab.binding.pushover.internal.config.PushoverAccountConfiguration;
import org.openhab.binding.pushover.internal.config.PushoverConfigOptionProvider;
import org.openhab.binding.pushover.internal.connection.PushoverAPIConnection;
import org.openhab.binding.pushover.internal.connection.PushoverCommunicationException;
import org.openhab.binding.pushover.internal.connection.PushoverConfigurationException;
import org.openhab.binding.pushover.internal.connection.PushoverMessageBuilder;
import org.openhab.binding.pushover.internal.dto.Sound;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link PushoverAccountHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverAccountHandler extends BaseThingHandler {

    private static final Collection<Class<? extends ThingHandlerService>> SUPPORTED_THING_ACTIONS = Set
            .of(PushoverActions.class, PushoverConfigOptionProvider.class);

    private final HttpClient httpClient;

    private PushoverAccountConfiguration config = new PushoverAccountConfiguration();
    private @Nullable PushoverAPIConnection connection;

    public PushoverAccountHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing
    }

    @Override
    public void initialize() {
        config = getConfigAs(PushoverAccountConfiguration.class);

        boolean configValid = true;
        final String apikey = config.apikey;
        if (apikey == null || apikey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
            configValid = false;
        }
        final String user = config.user;
        if (user == null || user.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-user");
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);

            connection = new PushoverAPIConnection(httpClient, config);
            scheduler.submit(this::asyncValidateUser);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return SUPPORTED_THING_ACTIONS;
    }

    /**
     * Retrieves the list of current sounds and their descriptions from the Pushover API.
     *
     * @return a list of {@link Sound}s
     */
    public List<Sound> getSounds() {
        try {
            return connection != null ? connection.getSounds() : PushoverAccountConfiguration.DEFAULT_SOUNDS;
        } catch (PushoverCommunicationException e) {
            // do nothing, causing exception is already logged
        } catch (PushoverConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        return PushoverAccountConfiguration.DEFAULT_SOUNDS;
    }

    /**
     * Returns a preconfigured {@link PushoverMessageBuilder}.
     *
     * @param message the message
     * @return a {@link PushoverMessageBuilder} instance
     */
    public PushoverMessageBuilder getDefaultPushoverMessageBuilder(String message) {
        PushoverMessageBuilder builder = PushoverMessageBuilder.getInstance(config.apikey, config.user)
                .withMessage(message) //
                .withTitle(config.title) //
                .withRetry(config.retry) //
                .withExpire(config.expire);
        // specify format if defined
        switch (config.format) {
            case PushoverMessageBuilder.MESSAGE_KEY_HTML:
                builder.withHtmlFormatting();
                break;
            case PushoverMessageBuilder.MESSAGE_KEY_MONOSPACE:
                builder.withMonospaceFormatting();
            default:
                break;
        }
        // add sound, if defined
        if (!DEFAULT_SOUND.equals(config.sound)) {
            builder.withSound(config.sound);
        }
        return builder;
    }

    public boolean sendMessage(PushoverMessageBuilder messageBuilder) {
        if (connection != null) {
            try {
                return connection.sendMessage(messageBuilder);
            } catch (PushoverCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushoverConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return false;
        } else {
            throw new IllegalArgumentException("PushoverAPIConnection is null!");
        }
    }

    public String sendPriorityMessage(PushoverMessageBuilder messageBuilder) {
        if (connection != null) {
            try {
                return connection.sendPriorityMessage(messageBuilder);
            } catch (PushoverCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushoverConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return "";
        } else {
            throw new IllegalArgumentException("PushoverAPIConnection is null!");
        }
    }

    public boolean cancelPriorityMessage(String receipt) {
        if (connection != null) {
            try {
                return connection.cancelPriorityMessage(receipt);
            } catch (PushoverCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushoverConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return false;
        } else {
            throw new IllegalArgumentException("PushoverAPIConnection is null!");
        }
    }

    @SuppressWarnings("null")
    private void asyncValidateUser() {
        try {
            connection.validateUser();
            updateStatus(ThingStatus.ONLINE);
        } catch (PushoverCommunicationException | PushoverConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }
}
