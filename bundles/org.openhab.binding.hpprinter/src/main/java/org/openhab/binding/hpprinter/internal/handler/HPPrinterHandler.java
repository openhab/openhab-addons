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
package org.openhab.binding.hpprinter.internal.handler;

import org.eclipse.jetty.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hpprinter.internal.HPPrinterConfiguration;
import org.openhab.binding.hpprinter.internal.binder.HPPrinterBinder;
import org.openhab.binding.hpprinter.internal.binder.HPPrinterBinderEvent;

/**
 * The {@link HPPrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterHandler extends BaseThingHandler implements HPPrinterBinderEvent {

    private @NonNullByDefault({}) HPPrinterConfiguration config;
    private @Nullable HttpClient httpClient = null;
    private @Nullable HPPrinterBinder binder;

    public HPPrinterHandler(Thing thing, @Nullable HttpClient httpClient) {
        super(thing);

        if (httpClient != null)
            this.httpClient = httpClient;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        binder.dynamicallyAddChannels(thing.getUID());
    }

    @Override
    public void handleRemoval() {
        if (binder != null) {
            binder.close();
            binder = null;
        }

        super.handleRemoval();
    }

    @Override
    public void dispose() {
        if (binder != null) {
            binder.close();
            binder = null;
        }
        
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(HPPrinterConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        if (config != null && config.ipAddress != "") {
            binder = new HPPrinterBinder(this, httpClient, scheduler, config);

            binder.dynamicallyAddChannels(thing.getUID());

            binder.open();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "You must set an IP Address");
        }

    }

    @Override
    public void binderStatus(ThingStatus status) {
        updateStatus(status);
    }

    @Override
    public void binderChannel(String group, String channel, State state) {
        updateState(new ChannelUID(thing.getUID(), group, channel), state);
    }

    @Override
    public void binderAddChannels(List<Channel> channels) {
        List<Channel> thingChannels = new ArrayList<>();
            thingChannels.addAll(getThing().getChannels());

        for (Channel channel : channels) {
            addOrUpdateChannel(channel, thingChannels);
        }

        updateThing(editThing().withChannels(thingChannels).build());
    }

    private static void addOrUpdateChannel(Channel newChannel, List<Channel> thingChannels) {
        removeChannelByUID(thingChannels, newChannel.getUID());
        thingChannels.add(newChannel);
    }

    private static void removeChannelByUID(List<Channel> thingChannels, ChannelUID channelUIDtoRemove) {
        Predicate<Channel> channelPredicate = c -> c.getUID().getId().equals(channelUIDtoRemove.getId());
        thingChannels.removeIf(channelPredicate);
    }
}
