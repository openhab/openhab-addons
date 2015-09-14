/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ownet.handler;

import static org.openhab.binding.ownet.OWNetBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OWDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dmitry Krasnov - Initial contribution
 */
public class OWDeviceHandler extends BaseThingHandler {
    // public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DEVICE);
    // public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = OW28Handler.SUPPORTED_THING_TYPES_UIDS;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>() {
        {
            add(THING_TYPE12);
            add(THING_TYPE26);
            add(THING_TYPE28);
            add(THING_TYPE29);
            add(THING_TYPE3A);
        }
    };
    // OW3AHandler.SUPPORTED_THING_TYPES_UIDS);
    public static final String PROP_ID = "id";

    private String id = null;

    private Logger logger = LoggerFactory.getLogger(OWDeviceHandler.class);

    public OWDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public static ThingUID uidFromAddress(String sid) {
        int family = Integer.valueOf(sid.substring(14), 16);
        switch (family) {
            case 0x12:
                return new ThingUID(THING_TYPE12, sid);
            case 0x26:
                return new ThingUID(THING_TYPE26, sid);
            case 0x28:
                return new ThingUID(THING_TYPE28, sid);
            case 0x29:
                return new ThingUID(THING_TYPE28, sid);
            case 0x3A:
                return new ThingUID(THING_TYPE3A, sid);
        }
        return null;
    }

    public void update(String channel, State newState) {
        ChannelUID uid = new ChannelUID(getThing().getUID(), channel);
        updateState(uid, newState);
    }
}
