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
package org.openhab.binding.nibeuplink.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

/**
 * generic implementation of handler logic
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericHandler extends UplinkBaseHandler {

    /**
     * constructor, called by the factory
     *
     * @param thing instance of the thing, passed in by the factory
     * @param httpClient the httpclient that communicates with the API
     */
    public GenericHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public @Nullable Channel getSpecificChannel(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            for (ChannelGroupUID channelGroupUID : getRegisteredGroups()) {
                channel = getThing().getChannel(new ChannelUID(channelGroupUID, channelId));
                if (channel != null) {
                    break;
                }
            }
        }
        return channel;
    }

    @Override
    public List<Channel> getChannels() {
        return getThing().getChannels();
    }
}
