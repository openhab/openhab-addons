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
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.HeosBindingConstants.CH_TYPE_FAVORITE;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelManager} provides the functions to
 * add and remove channels from the channel list provided by the thing
 * The generation of the individual channels has to be done by the thingHandler
 * itself. Only for the favorites a function is provided which generates the
 * individual channels for each favorite.
 *
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosChannelManager {
    private final Logger logger = LoggerFactory.getLogger(HeosChannelManager.class);

    private ThingHandler handler;

    private List<Channel> channelList = new ArrayList<>();


    public HeosChannelManager(ThingHandler handler) {
        this.handler = handler;
    }

    public List<Channel> addSingleChannel(Channel channel) {
        getChannelsFromThing();
        addChannel(channel);
        return channelList;
    }

    public List<Channel> addMultibleChannels(List<Channel> channels) {
        getChannelsFromThing();
        channels.forEach(channel -> addChannel(channel));
        return channelList;
    }

    public List<Channel> removeSingelChannel(String channelIdentifyer) {
        getChannelsFromThing();
        removeChannel(generateChannelUID(channelIdentifyer));
        return channelList;
    }

    public List<Channel> removeSingleChannel(ChannelUID uid) {
        getChannelsFromThing();
        removeChannel(uid);
        return channelList;
    }

    public List<Channel> removeMutlibleChannels(List<String> channelIdentifyer) {
        getChannelsFromThing();
        channelIdentifyer.forEach(identifyer -> removeChannel(generateChannelUID(identifyer)));
        return channelList;
    }

    public List<Channel> removeMultibleChannels(List<ChannelUID> channelUIDs) {
        getChannelsFromThing();
        channelUIDs.forEach(uid -> removeChannel(uid));
        return channelList;
    }

    public List<Channel> removeChannelsByType(ChannelTypeUID channelType) {
        getChannelsFromThing();
        Iterator<Channel> channelIterator = channelList.iterator();
        while (channelIterator.hasNext()) {
            if (channelType.equals(channelIterator.next().getChannelTypeUID())) {
                channelIterator.remove();
            }
        }
        return channelList;
    }

    public List<Channel> removeAllChannels() {
        getChannelsFromThing();
        channelList.clear();
        return channelList;
    }

    public List<Channel> addFavoriteChannels(List<Map<String, String>> favoritesList) {
        List<Channel> channelList = new ArrayList<Channel>();
        favoritesList.forEach(element -> channelList.add(generateFavoriteChannel(element)));
        return addMultibleChannels(channelList);
    }

    private Channel generateFavoriteChannel(Map<String, String> properties) {
        return ChannelBuilder.create(generateChannelUID(properties.get(MID)), "Switch").withLabel(properties.get(NAME))
                .withType(CH_TYPE_FAVORITE).withProperties(properties).build();
    }

    /*
     * Gets the channels from the Thing and makes the channel
     * list editable.
     */
    private void getChannelsFromThing() {
        List<Channel> channelListFromThing = handler.getThing().getChannels();
        channelList.clear();
        channelList.addAll(channelListFromThing);
    }

    private ChannelUID generateChannelUID(String channelIdentifyer) {
        return new ChannelUID(handler.getThing().getUID(), channelIdentifyer);
    }

    private void removeChannel(ChannelUID uid) {
        Channel channelToBeRemoved = channelList.stream().filter(channel -> uid.equals(channel.getUID())).findFirst()
                .orElse(null);
        if (channelToBeRemoved != null) {
            channelList.remove(channelToBeRemoved);
        }
    }

    /*
     * Function to add an channel to the channel list.
     * Checks first if channel already exists.
     * If so, updated the channel by removing it first and
     * add it again.
     */
    private void addChannel(Channel channel) {
        // If channel already exists remove it first
        removeChannel(channel.getUID());
        // Then add the new/updated channel to the list
        channelList.add(channel);
        logger.debug("Addding Channel: {}", channel.getLabel());
    }
}
