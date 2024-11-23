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
package org.openhab.binding.hpprinter.internal;

import static org.openhab.binding.hpprinter.internal.HPPrinterBindingConstants.CGROUP_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link HPPrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterHandler extends BaseThingHandler {
    private final HttpClient httpClient;
    private @Nullable HPPrinterBinder binder;

    public HPPrinterHandler(final Thing thing, final HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void thingUpdated(final Thing thing) {
        super.thingUpdated(thing);

        final HPPrinterBinder printerBinder = this.binder;
        if (printerBinder != null) {
            printerBinder.dynamicallyAddChannels(thing.getUID());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    @Override
    public void initialize() {
        scheduler.submit(() -> initializeScheduled());
    }

    /**
     * Scheduled initialization task which will be executed on a separate thread
     */
    private void initializeScheduled() {
        final HPPrinterConfiguration config = getConfigAs(HPPrinterConfiguration.class);

        if (!"".equals(config.ipAddress)) {
            final HPPrinterBinder localBinder = binder = new HPPrinterBinder(this, httpClient, scheduler, config);

            localBinder.dynamicallyAddChannels(thing.getUID());
            localBinder.retrieveProperties();
            localBinder.open();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "You must set an IP Address");
        }
    }

    @Override
    public void dispose() {
        final HPPrinterBinder localBinder = binder;
        if (localBinder != null) {
            localBinder.close();
            binder = null;
        }
    }

    protected Boolean areStatusChannelsLinked(final String[] channels) {
        for (int i = 0; i < channels.length; i++) {
            if (isLinked(new ChannelUID(thing.getUID(), CGROUP_STATUS, channels[i]))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateStatus(final ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        final HPPrinterBinder localBinder = binder;
        if (localBinder != null) {
            localBinder.channelsChanged();
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        final HPPrinterBinder localBinder = binder;
        if (localBinder != null) {
            localBinder.channelsChanged();
        }
    }

    @Override
    protected void updateStatus(final ThingStatus status, final ThingStatusDetail thingStatusDetail,
            @Nullable final String message) {
        super.updateStatus(status, thingStatusDetail, message);
    }

    public void updateState(final String group, final String channel, final State state) {
        super.updateState(new ChannelUID(thing.getUID(), group, channel), state);
    }

    public void binderAddChannels(final List<Channel> channels) {
        final List<Channel> thingChannels = new ArrayList<>(getThing().getChannels());

        for (final Channel channel : channels) {
            addOrUpdateChannel(channel, thingChannels);
        }

        updateThing(editThing().withChannels(thingChannels).build());
    }

    @Override
    protected ThingBuilder editThing() {
        return super.editThing();
    }

    @Override
    protected @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    @Override
    protected void updateProperties(final @Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

    private static void addOrUpdateChannel(final Channel newChannel, final List<Channel> thingChannels) {
        removeChannelByUID(thingChannels, newChannel.getUID());
        thingChannels.add(newChannel);
    }

    private static void removeChannelByUID(final List<Channel> thingChannels, final ChannelUID channelUIDtoRemove) {
        final Predicate<Channel> channelPredicate = c -> c.getUID().getId().equals(channelUIDtoRemove.getId());
        thingChannels.removeIf(channelPredicate);
    }
}
