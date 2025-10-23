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
package org.openhab.binding.unifiaccess.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

/**
 * Base class for all UniFi Access device and door handlers.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class UnifiAccessBaseHandler extends BaseThingHandler {
    protected Map<String, State> stateCache = new HashMap<>();

    public UnifiAccessBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateState(String channelUID, State state) {
        super.updateState(channelUID, state);
        stateCache.put(channelUID, state);
    }

    protected void refreshState(String channelId) {
        State state = stateCache.get(channelId);
        if (state != null) {
            super.updateState(channelId, state);
        }
    }

    protected @Nullable UnifiAccessBridgeHandler getBridgeHandler() {
        var b = getBridge();
        if (b == null) {
            return null;
        }
        var h = b.getHandler();
        return (h instanceof UnifiAccessBridgeHandler) ? (UnifiAccessBridgeHandler) h : null;
    }
}
