/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.solaredge.internal.model.AggregateDataChannels;
import org.openhab.binding.solaredge.internal.model.Channel;
import org.openhab.binding.solaredge.internal.model.LiveDataChannels;

/**
 * generic thing handler for solaredge
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericSolarEdgeHandler extends SolarEdgeBaseHandler {

    public GenericSolarEdgeHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> result = new ArrayList<>();
        Collections.addAll(result, LiveDataChannels.values());
        Collections.addAll(result, AggregateDataChannels.values());
        return result;
    }
}
