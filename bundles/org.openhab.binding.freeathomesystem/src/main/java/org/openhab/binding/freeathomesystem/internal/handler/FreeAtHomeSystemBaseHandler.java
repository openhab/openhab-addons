/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link FreeAtHomeSystemBaseHandler} is the base class because state updates
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

@NonNullByDefault
public class FreeAtHomeSystemBaseHandler extends BaseThingHandler {

    public FreeAtHomeSystemBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    public void handleEventBasedUpdate(ChannelUID channelUID, State state) {
        this.updateState(channelUID, state);
    }
}
