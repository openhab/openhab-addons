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
package org.openhab.binding.heos.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link HeosChannelManager} provides the functions to
 * add and remove channels from the channel list provided by the thing
 * The generation of the individual channels has to be done by the thingHandler
 * itself.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelManager {
    private final ThingHandler handler;

    public HeosChannelManager(ThingHandler handler) {
        this.handler = handler;
    }

    public synchronized List<Channel> addSingleChannel(Channel channel) {
        ChannelWrapper channelList = getChannelsFromThing();
        channelList.removeChannel(channel.getUID());
        channelList.add(channel);
        return channelList.get();
    }

    public synchronized List<Channel> removeSingleChannel(String channelIdentifier) {
        ChannelWrapper channelWrapper = getChannelsFromThing();
        channelWrapper.removeChannel(generateChannelUID(channelIdentifier));
        return channelWrapper.get();
    }

    /*
     * Gets the channels from the Thing and makes the channel
     * list editable.
     */
    private ChannelWrapper getChannelsFromThing() {
        return new ChannelWrapper(handler.getThing().getChannels());
    }

    private ChannelUID generateChannelUID(String channelIdentifier) {
        return new ChannelUID(handler.getThing().getUID(), channelIdentifier);
    }

    /**
     * Wrap a channel list
     *
     * @author Martin van Wingerden - Initial contribution
     */
    private static class ChannelWrapper {
        private final List<Channel> channels;

        ChannelWrapper(List<Channel> channels) {
            this.channels = new ArrayList<>(channels);
        }

        private void removeChannel(ChannelUID uid) {
            List<Channel> itemsToBeRemoved = channels.stream().filter(Objects::nonNull)
                    .filter(channel -> uid.equals(channel.getUID())).collect(Collectors.toList());

            channels.removeAll(itemsToBeRemoved);
        }

        public void add(Channel channel) {
            channels.add(channel);
        }

        public List<Channel> get() {
            return Collections.unmodifiableList(channels);
        }
    }
}
