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
package org.openhab.binding.hpprinter.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import static org.openhab.binding.hpprinter.internal.HPPrinterBindingConstants.*;
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
        final HPPrinterConfiguration config = getConfigAs(HPPrinterConfiguration.class);

        if (config != null && !"".equals(config.ipAddress)) {
            binder = new HPPrinterBinder(this, httpClient, scheduler, config);
            binder.dynamicallyAddChannels(thing.getUID());
            binder.retrieveProperties();
            binder.open();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "You must set an IP Address");
        }
    }

    @Override
    public void dispose() {
        if (binder != null) {
            binder.close();
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

    protected void updateStatus(final ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        binder.channelsChanged();
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        binder.channelsChanged();
    }

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

    protected ThingBuilder editThing() {
        return super.editThing();
    }

    protected @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    protected void updateProperties(final Map<String, String> properties) {
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
