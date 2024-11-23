/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal.handler;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.POWER_STATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevice;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link ElroConnectsPowerSocketHandler} represents the thing handler for an ELRO Connects power socket device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsPowerSocketHandler extends ElroConnectsDeviceHandler {

    public ElroConnectsPowerSocketHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer id = deviceId;
        ElroConnectsBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            ElroConnectsDevice device = bridgeHandler.getDevice(id);
            if (device != null) {
                switch (channelUID.getId()) {
                    case POWER_STATE:
                        if (OnOffType.ON.equals(command)) {
                            device.switchState(OnOffType.ON.equals(command) ? true : false);
                        }
                        break;
                }
            }
        }

        super.handleCommand(channelUID, command);
    }
}
