/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.openhab.binding.nibeuplink.internal.model.F1155Channels;

/**
 * VVM320 specific implementation part of handler logic
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class F1155Handler extends GenericUplinkHandler {

    public F1155Handler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected Channel getThingSpecificChannel(String id) {
        return F1155Channels.fromId(id);
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>(F1155Channels.values().length);

        for (F1155Channels channel : F1155Channels.values()) {
            list.add(channel);
        }

        return list;
    }

}
