/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.caddx.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.caddx.internal.CaddxEvent;

/**
 * This is a class for handling a Keypad type Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class ThingHandlerKeypad extends CaddxBaseThingHandler {
    /**
     * Constructor.
     *
     * @param thing
     */
    public ThingHandlerKeypad(Thing thing) {
        super(thing, CaddxThingType.KEYPAD);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void caddxEventReceived(CaddxEvent event, Thing thing) {
        updateStatus(ThingStatus.ONLINE);
    }
}
