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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.EXECUTE_ACTION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link SomfyTahomaActionGroupHandler} is responsible for handling commands,
 * which are sent to one of the channels of the action group thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaActionGroupHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaActionGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean isAlwaysOnline() {
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (EXECUTE_ACTION.equals(channelUID.getId()) && command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                executeActionGroup();
            }
        }
    }
}
