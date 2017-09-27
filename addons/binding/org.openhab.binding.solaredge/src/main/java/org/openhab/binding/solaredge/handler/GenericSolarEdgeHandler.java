/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.solaredge.internal.model.AggregateDataChannels;
import org.openhab.binding.solaredge.internal.model.Channel;
import org.openhab.binding.solaredge.internal.model.LiveDataChannels;

/**
 * generic thing handler for solaredge
 *
 * @author afriese
 *
 */
public class GenericSolarEdgeHandler extends SolarEdgeBaseHandler {

    public GenericSolarEdgeHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> result = new ArrayList<>();
        Collections.addAll(result, LiveDataChannels.values());
        Collections.addAll(result, AggregateDataChannels.values());
        return result;
    }

    @Override
    protected Channel getThingSpecificChannel(String fqName) {
        Channel live = LiveDataChannels.fromFQName(fqName);
        Channel agg = AggregateDataChannels.fromFQName(fqName);
        return live != null ? live : agg;
    }

}
