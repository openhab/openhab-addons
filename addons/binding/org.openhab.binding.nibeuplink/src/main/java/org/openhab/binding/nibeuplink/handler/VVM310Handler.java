/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.CustomChannels;
import org.openhab.binding.nibeuplink.internal.model.VVM310Channels;

/**
 * VVM320 specific implementation part of handler logic
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class VVM310Handler extends GenericUplinkHandler {

    public VVM310Handler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected Channel getThingSpecificChannel(String id) {
        Channel specific = VVM310Channels.fromId(id);
        if (specific == null) {
            return CustomChannels.fromId(id);
        }
        return specific;
    }

    @Override
    public List<Channel> getChannels() {
        return getChannels(VVM310Channels.values());
    }

}
