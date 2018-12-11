/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.http.internal.HttpChannelState;
import org.openhab.binding.http.internal.model.HttpChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.openhab.binding.http.internal.HttpBindingConstants.CHANNEL_TYPE_ID_IMAGE;
import static org.openhab.binding.http.internal.HttpBindingConstants.MAX_IMAGE_RESPONSE_BODY_LEN;
import static org.openhab.binding.http.internal.HttpBindingConstants.MAX_RESPONSE_BODY_LEN;

/**
 * Handler for HTTP Things.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
public class HttpThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<ChannelUID, HttpChannelState> channels = new HashMap<>();

    private final HttpClient httpClient;

    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    public HttpThingHandler(final Thing thing,
                            final HttpClient httpClient,
                            final ItemChannelLinkRegistry itemChannelLinkRegistry)
    {
        super(thing);
        this.httpClient = httpClient;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void initialize() {
        try {
            for (final Channel channel : getThing().getChannels()) {
                final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null) {
                    final int maxHttpResponseBodyLen;
                    if (CHANNEL_TYPE_ID_IMAGE.equals(channelTypeUID.getId())) {
                        maxHttpResponseBodyLen = MAX_IMAGE_RESPONSE_BODY_LEN;
                    } else {
                        maxHttpResponseBodyLen = MAX_RESPONSE_BODY_LEN;
                    }

                    final HttpChannelConfig config = channel.getConfiguration().as(HttpChannelConfig.class);
                    final HttpChannelState channelState = new HttpChannelState(
                            channel.getUID(),
                            channelTypeUID,
                            this.httpClient,
                            maxHttpResponseBodyLen,
                            config.getStateRequest(bundleContext),
                            scheduler,
                            config.getCommandRequest(bundleContext),
                            this::updateState,
                            this::communicationsError
                    );
                    channels.put(channel.getUID(), channelState);
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (final IllegalArgumentException e) {
            disposeChannels();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (channels.containsKey(channelUID)) {
            channels.get(channelUID).handleCommand(command);
        }
    }

    @Override
    protected void updateStatus(final ThingStatus status, final ThingStatusDetail statusDetail, @Nullable final String description) {
        final ThingStatusInfo curStatusInfo = getThing().getStatusInfo();
        if (!Objects.equals(status, curStatusInfo.getStatus()) || !Objects.equals(statusDetail, curStatusInfo.getStatusDetail()) || !Objects.equals(description, curStatusInfo.getDescription())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    protected void updateState(final ChannelUID channelUID, final State state) {
        final boolean needsUpdate = this.itemChannelLinkRegistry.getLinkedItems(channelUID).stream()
                .anyMatch(item -> !state.equals(item.getState()));
        if (needsUpdate) {
            updateStatus(ThingStatus.ONLINE);
            super.updateState(channelUID, state);
        }
    }

    @Override
    public void dispose() {
        disposeChannels();
    }

    private void communicationsError(final ChannelUID channelUID, final ThingStatusDetail errorDetail, final String errorDescription) {
        if (channels.size() == 1) {
            // if we only have one channel, treat an error on that channel as offline for the entire thing
            updateStatus(ThingStatus.OFFLINE, errorDetail, errorDescription);
        }
    }

    private void disposeChannels() {
        this.channels.values().forEach(HttpChannelState::close);
        this.channels.clear();
    }
}
