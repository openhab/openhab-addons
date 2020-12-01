/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.dial;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.AbstractThingHandler;
import org.openhab.binding.sony.internal.LoginUnsuccessfulResponse;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thing handler for a Sony DIAL device. This is the entry point provides a full two interaction between openhab
 * and a DIAL system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DialHandler extends AbstractThingHandler<DialConfig> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(DialHandler.class);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<@Nullable DialProtocol<ThingCallback<String>>> protocolHandler = new AtomicReference<>();

    /**
     * Constructs the handler from the {@link Thing}.
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public DialHandler(final Thing thing) {
        super(thing, DialConfig.class);
    }

    @Override
    protected void handleRefreshCommand(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        Objects.requireNonNull(channelUID, "channelId cannot be null");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final DialProtocol<ThingCallback<String>> localProtocolHandler = protocolHandler.get();
        if (localProtocolHandler == null) {
            logger.debug("Trying to handle a refresh command before a protocol handler has been created");
            return;
        }

        final String channelId = channelUID.getId();
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.debug("Channel wasn't found for {}", channelUID);
            return;
        }

        final String applId = channel.getProperties().get(DialConstants.CHANNEL_PROP_APPLID);
        if (StringUtils.isEmpty(applId)) {
            logger.debug("Called with an empty applicationid - ignoring: {}", channelUID);
            return;
        }

        if (channelId.endsWith(DialConstants.CHANNEL_STATE)) {
            localProtocolHandler.refreshState(channelId, applId);
        } else if (channelId.endsWith(DialConstants.CHANNEL_TITLE)) {
            localProtocolHandler.refreshName(channelId, applId);
        } else if (channelId.endsWith(DialConstants.CHANNEL_ICON)) {
            localProtocolHandler.refreshIcon(channelId, applId);
        }
    }

    @Override
    protected void handleSetCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final DialProtocol<ThingCallback<String>> localProtocolHandler = protocolHandler.get();
        if (localProtocolHandler == null) {
            logger.debug("Trying to handle a channel command before a protocol handler has been created");
            return;
        }

        final String channelId = channelUID.getId();
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.debug("Channel wasn't found for {}", channelUID);
            return;
        }

        final String applId = channel.getProperties().get(DialConstants.CHANNEL_PROP_APPLID);
        if (StringUtils.isEmpty(applId)) {
            logger.debug("Called with an empty applicationid - ignoring: {}", channelUID);
            return;
        }

        if (channelId.endsWith(DialConstants.CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                localProtocolHandler.setState(channelId, applId, OnOffType.ON == command);
            } else {
                logger.debug("Received a STATE channel command with a non OnOffType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", channelUID);
        }
    }

    @Override
    protected void connect() {
        final DialConfig config = getSonyConfig();

        try {
            // Validate the device URL
            config.getDeviceUrl();
        } catch (final MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device URL (in configuration) was missing or malformed");
            return;
        }

        logger.debug("Attempting connection to DIAL device...");
        try {
            SonyUtil.checkInterrupt();
            final DialProtocol<ThingCallback<String>> localProtocolHandler = new DialProtocol<>(config,
                    new ThingCallback<String>() {
                        @Override
                        public void statusChanged(final ThingStatus state, final ThingStatusDetail detail,
                                final @Nullable String msg) {
                            updateStatus(state, detail, msg);
                        }

                        @Override
                        public void stateChanged(final String channelId, final State newState) {
                            updateState(channelId, newState);
                        }

                        @Override
                        public void setProperty(final String propertyName, final @Nullable String propertyValue) {
                            getThing().setProperty(propertyName, propertyValue);
                        }
                    });

            SonyUtil.checkInterrupt();
            final LoginUnsuccessfulResponse response = localProtocolHandler.login();
            if (response == null) {
                final ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(
                        DialUtil.generateChannels(getThing().getUID(), localProtocolHandler.getDialApps().values()));
                updateThing(thingBuilder.build());

                SonyUtil.checkInterrupt();
                SonyUtil.close(protocolHandler.getAndSet(localProtocolHandler));
                updateStatus(ThingStatus.ONLINE);

                SonyUtil.checkInterrupt();
                logger.debug("DIAL System now connected");
            } else {
                updateStatus(ThingStatus.OFFLINE, response.getThingStatusDetail(), response.getMessage());
            }
        } catch (IOException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to DIAL device (may need to turn it on manually): " + e.getMessage());
        } catch (final InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Initialization was interrupted");
        }
    }

    @Override
    protected void refreshState(boolean initial) {
        final DialProtocol<ThingCallback<String>> protocol = protocolHandler.get();
        if (protocol != null) {
            getThing().getChannels().stream().forEach(chn -> {
                final String channelId = chn.getUID().getId();
                if (StringUtils.endsWithIgnoreCase(channelId, DialConstants.CHANNEL_STATE)) {
                    final String applId = chn.getProperties().get(DialConstants.CHANNEL_PROP_APPLID);
                    if (StringUtils.isEmpty(applId)) {
                        logger.debug("Unknown application id for channel {}", channelId);
                    } else {
                        protocol.refreshState(channelId, applId);
                    }
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        SonyUtil.close(protocolHandler.getAndSet(null));
    }
}
