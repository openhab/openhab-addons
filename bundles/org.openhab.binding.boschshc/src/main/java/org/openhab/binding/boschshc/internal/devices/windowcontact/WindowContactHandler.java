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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.devices.BoschSHCConfiguration;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschSHCBridgeHandler;

import com.google.gson.Gson;
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

        BoschSHCConfiguration config = super.getBoschConfig();
        Bridge bridge = this.getBridge();

        if (bridge != null && config != null) {

            logger.debug("Handle command for: {} - {}", config.id, command);
            BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();

            if (bridgeHandler != null) {
                if (command instanceof RefreshType && CHANNEL_CONTACT.equals(channelUID.getId())) {
                    ShutterContactState state = bridgeHandler.refreshState(getThing(), "ShutterContact",
                            ShutterContactState.class);
                    if (state != null) {
                        updateShutterContactState(state);
                    }
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is null");
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
            Gson gson = new Gson();
            updateShutterContactState(gson.fromJson(state, ShutterContactState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in window contact handler: {}", state);
        }
    }
}
