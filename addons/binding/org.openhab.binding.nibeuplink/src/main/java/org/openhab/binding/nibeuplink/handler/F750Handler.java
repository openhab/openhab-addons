/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.ChannelType;
import org.openhab.binding.nibeuplink.internal.model.F750Channels;

/**
 * VVM320 specific implementation part of handler logic
 *
 * @author afriese
 *
 */
public class F750Handler extends GenericUplinkHandler {

    private final ChannelSet channelSet;

    public F750Handler(@NonNull Thing thing, ChannelSet channelSet) {
        super(thing);
        this.channelSet = channelSet;
    }

    @Override
    protected Channel getThingSpecificChannel(String id) {
        if (!channelSet.equals(ChannelSet.Special)) {
            return F750Channels.fromId(id);
        } else {
            return null;
        }
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>(F750Channels.values().length);

        if (!channelSet.equals(ChannelSet.Special)) {
            for (F750Channels channel : F750Channels.values()) {

                if (channel.getChannelType().equals(ChannelType.Sensor)) {
                    switch (channelSet) {
                        case All:
                        case Sensors:
                            list.add(channel);
                        default:
                            break;
                    }
                }

                else if (channel.getChannelType().equals(ChannelType.Setting)) {
                    switch (channelSet) {
                        case All:
                        case Settings:
                            list.add(channel);
                        default:
                            break;
                    }
                }
            }
        }

        return list;
    }

}
