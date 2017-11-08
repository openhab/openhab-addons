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
import org.openhab.binding.nibeuplink.internal.model.F1145Channels;

/**
 * VVM320 specific implementation part of handler logic
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class F1145Handler extends GenericUplinkHandler {

    private final ChannelSet channelSet;

    public F1145Handler(@NonNull Thing thing, ChannelSet channelSet) {
        super(thing);
        this.channelSet = channelSet;
    }

    @Override
    protected Channel getThingSpecificChannel(String id) {
        if (!channelSet.equals(ChannelSet.SPECIAL)) {
            return F1145Channels.fromId(id);
        } else {
            return null;
        }
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>(F1145Channels.values().length);

        if (!channelSet.equals(ChannelSet.SPECIAL)) {
            for (F1145Channels channel : F1145Channels.values()) {

                if (channel.getChannelType().equals(ChannelType.SENSOR)) {
                    switch (channelSet) {
                        case ALL:
                        case SENSORS:
                            list.add(channel);
                        default:
                            break;
                    }
                }

                else if (channel.getChannelType().equals(ChannelType.SETTING)) {
                    switch (channelSet) {
                        case ALL:
                        case SETTINGS:
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
