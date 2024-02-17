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
package org.openhab.binding.pushsafer.internal.handler;

import static org.openhab.binding.pushsafer.internal.PushsaferBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pushsafer.internal.actions.PushsaferActions;
import org.openhab.binding.pushsafer.internal.config.PushsaferAccountConfiguration;
import org.openhab.binding.pushsafer.internal.config.PushsaferConfigOptionProvider;
import org.openhab.binding.pushsafer.internal.connection.PushsaferAPIConnection;
import org.openhab.binding.pushsafer.internal.connection.PushsaferCommunicationException;
import org.openhab.binding.pushsafer.internal.connection.PushsaferConfigurationException;
import org.openhab.binding.pushsafer.internal.connection.PushsaferMessageBuilder;
import org.openhab.binding.pushsafer.internal.dto.Icon;
import org.openhab.binding.pushsafer.internal.dto.Sound;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link PushsaferAccountHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class PushsaferAccountHandler extends BaseThingHandler {

    private static final Collection<Class<? extends ThingHandlerService>> SUPPORTED_THING_ACTIONS = Set
            .of(PushsaferActions.class, PushsaferConfigOptionProvider.class);

    private final HttpClient httpClient;

    private PushsaferAccountConfiguration config = new PushsaferAccountConfiguration();
    private @Nullable PushsaferAPIConnection connection;

    public PushsaferAccountHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing
    }

    @Override
    public void initialize() {
        config = getConfigAs(PushsaferAccountConfiguration.class);

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

            connection = new PushsaferAPIConnection(httpClient, config);
            scheduler.submit(this::asyncValidateUser);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return SUPPORTED_THING_ACTIONS;
    }

    /**
     * Retrieves the list of current sounds and their descriptions from the Pushsafer API.
     *
     * @return a list of {@link Sound}s
     */
    public List<Sound> getSounds() {
        try {
            return connection != null ? connection.getSounds() : List.of();
        } catch (PushsaferCommunicationException e) {
            // do nothing, causing exception is already logged
        } catch (PushsaferConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        return List.of();
    }

    /**
     * Retrieves the list of current icons from the Pushsafer API.
     *
     * @return a list of {@link Icon}s
     */
    public List<Icon> getIcons() {
        try {
            return connection != null ? connection.getIcons() : List.of();
        } catch (PushsaferCommunicationException e) {
            // do nothing, causing exception is already logged
        } catch (PushsaferConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        return List.of();
    }

    /**
     * Returns a preconfigured {@link PushsaferMessageBuilder}.
     *
     * @param message the message
     * @return a {@link PushsaferMessageBuilder} instance
     */
    public PushsaferMessageBuilder getDefaultPushsaferMessageBuilder(String message)
            throws PushsaferConfigurationException {
        PushsaferMessageBuilder builder = PushsaferMessageBuilder.getInstance(config.apikey, config.device)
                .withMessage(message) //
                .withTitle(config.title) //
                .withRetry(config.retry) //
                .withExpire(config.expire);
        // specify format if defined
        switch (config.format) {
            case PushsaferMessageBuilder.MESSAGE_KEY_HTML:
                builder.withHtmlFormatting();
                break;
            case PushsaferMessageBuilder.MESSAGE_KEY_MONOSPACE:
                builder.withMonospaceFormatting();
            default:
                break;
        }
        // add sound if defined
        if (!DEFAULT_SOUND.equals(config.sound)) {
            builder.withSound(config.sound);
        }
        // add icon if defined
        if (!DEFAULT_ICON.equals(config.icon)) {
            builder.withIcon(config.icon);
        }
        // add color if defined
        if (!DEFAULT_COLOR.equals(config.color)) {
            builder.withColor(config.color);
        }
        // add vibration if defined
        if (!DEFAULT_VIBRATION.equals(config.vibration)) {
            builder.withVibration(config.vibration);
        }
        // add url if defined
        if (!DEFAULT_URL.equals(config.url)) {
            builder.withUrl(config.url);
        }
        // add urlTitle if defined
        if (!DEFAULT_URLTITLE.equals(config.urlTitle)) {
            builder.withUrlTitle(config.urlTitle);
        }
        // add confirm if defined
        if (DEFAULT_CONFIRM != config.confirm) {
            builder.withConfirm(config.confirm);
        }
        // add answer if defined
        if (DEFAULT_ANSWER != config.answer) {
            builder.withAnswer(config.answer);
        }
        // add time2live if defined
        if (DEFAULT_TIME2LIVE != config.time2live) {
            builder.withTime2live(config.time2live);
        }
        return builder;
    }

    public boolean sendPushsaferMessage(PushsaferMessageBuilder messageBuilder) {
        if (connection != null) {
            try {
                return connection.sendPushsaferMessage(messageBuilder);
            } catch (PushsaferCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushsaferConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return false;
        } else {
            throw new IllegalArgumentException("PushsaferAPIConnection is null!");
        }
    }

    public String sendPushsaferPriorityMessage(PushsaferMessageBuilder messageBuilder) {
        if (connection != null) {
            try {
                return connection.sendPushsaferPriorityMessage(messageBuilder);
            } catch (PushsaferCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushsaferConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return "";
        } else {
            throw new IllegalArgumentException("PushsaferAPIConnection is null!");
        }
    }

    public boolean cancelPushsaferPriorityMessage(String receipt) {
        if (connection != null) {
            try {
                return connection.cancelPushsaferPriorityMessage(receipt);
            } catch (PushsaferCommunicationException e) {
                // do nothing, causing exception is already logged
            } catch (PushsaferConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return false;
        } else {
            throw new IllegalArgumentException("PushsaferAPIConnection is null!");
        }
    }

    @SuppressWarnings("null")
    private void asyncValidateUser() {
        try {
            connection.validateUser();
            updateStatus(ThingStatus.ONLINE);
        } catch (PushsaferCommunicationException | PushsaferConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }
}
