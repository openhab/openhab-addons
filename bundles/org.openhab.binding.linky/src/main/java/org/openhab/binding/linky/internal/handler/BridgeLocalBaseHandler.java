/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * {@link BridgeLocalBaseHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeLocalBaseHandler extends BaseBridgeHandler {
    private List<String> registeredPrmId = new ArrayList<>();

    protected final Gson gson;

    public BridgeLocalBaseHandler(Bridge bridge, Gson gson) {
        super(bridge);
        this.gson = gson;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public synchronized void initialize() {
    }

    public void registerNewPrmId(String prmId) {
        if (!registeredPrmId.contains(prmId)) {
            registeredPrmId.add(prmId);
        }
    }

    public Gson getGson() {
        return gson;
    }

    public @Nullable ThingLinkyLocalHandler getHandlerForIdd2l(long idd2l) {
        List<Thing> lThing = getThing().getThings();

        for (Thing th : lThing) {
            ThingLinkyLocalHandler handler = (ThingLinkyLocalHandler) th.getHandler();

            if (handler != null) {
                long thingIdd2l = handler.getIdd2l();

                if (idd2l == thingIdd2l) {
                    return handler;
                }
            }
        }

        return null;
    }

    public @Nullable ThingLinkyLocalHandler getHandlerForPrmId(String prmId) {
        List<Thing> lThing = getThing().getThings();

        for (Thing th : lThing) {
            ThingLinkyLocalHandler handler = (ThingLinkyLocalHandler) th.getHandler();

            if (handler != null) {
                String thingPrmId = handler.getPrmId();

                if (prmId.equals(thingPrmId)) {
                    return handler;
                }
            }
        }

        return null;
    }

}
