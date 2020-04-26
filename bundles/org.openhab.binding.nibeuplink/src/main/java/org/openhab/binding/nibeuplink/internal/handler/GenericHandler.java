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
package org.openhab.binding.nibeuplink.internal.handler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.ChannelList;
import org.openhab.binding.nibeuplink.internal.model.CustomChannels;

/**
 * generic implementation of handler logic
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericHandler extends UplinkBaseHandler {
    private final ChannelList channelList;

    /**
     * constructor, called by the factory
     *
     * @param thing instance of the thing, passed in by the factory
     * @param httpClient the httpclient that communicates with the API
     * @param channelList the specific channellist
     */
    public GenericHandler(Thing thing, HttpClient httpClient, ChannelList channelList) {
        super(thing, httpClient);
        this.channelList = channelList;
    }

    @Override
    public @Nullable Channel getSpecificChannel(String channelCode) {
        Channel channel = channelList.fromCode(channelCode);

        // check custom channels if no stock channel was found
        if (channel == null) {
            channel = CustomChannels.getInstance().fromCode(channelCode);
        }
        return channel;
    }

    @Override
    public Set<Channel> getChannels() {
        Set<Channel> specificAndCustomChannels = new HashSet<>();
        specificAndCustomChannels.addAll(channelList.getChannels());
        specificAndCustomChannels.addAll(CustomChannels.getInstance().getChannels());
        return specificAndCustomChannels;
    }
}
