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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CONTACT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.windowcontact.dto.ShutterContactState;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link BoschSHCHandler} is responsible for handling Bosch window/door contacts.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class WindowContactHandler extends BoschSHCHandler {

    public WindowContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command for: {} - {}", channelUID.getThingUID(), command);
        if (command instanceof RefreshType && CHANNEL_CONTACT.equals(channelUID.getId())) {
            ShutterContactState state = this.getState("ShutterContact", ShutterContactState.class);
            if (state != null) {
                updateShutterContactState(state);
            }
        }
    }

    void updateShutterContactState(ShutterContactState state) {
        State contact = state.value.equals("CLOSED") ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        updateState(CHANNEL_CONTACT, contact);

        logger.debug("Parsed shutter contact state state of {}: {}", this.getBoschID(), state.value);
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        logger.debug("WindowContact: received update: {} {}", id, state);
        try {
            updateShutterContactState(gson.fromJson(state, ShutterContactState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in window contact handler: {}", state);
        }
    }
}
