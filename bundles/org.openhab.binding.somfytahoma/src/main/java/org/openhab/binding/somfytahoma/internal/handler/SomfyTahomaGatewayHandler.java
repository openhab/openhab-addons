/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.STATUS;
import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.UNAUTHORIZED;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link SomfyTahomaGatewayHandler} is responsible for handling commands,
 * which are sent to one of the channels of the gateway thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaGatewayHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

    public void updateStatusChannel() {
        Channel ch = thing.getChannel(STATUS);
        if (ch != null) {
            updateChannelState(ch.getUID());
        }
    }

    private void updateChannelState(ChannelUID channelUID) {
        if (STATUS.equals(channelUID.getId())) {
            String id = getGateWayId();
            String tahomaStatus = getTahomaStatus(id);
            if (tahomaStatus != null && !UNAUTHORIZED.equals(tahomaStatus)) {
                updateState(channelUID, new StringType(tahomaStatus));
                //update the firmware property
                String fw = getTahomaVersion(id);
                if (fw != null) {
                    updateProperty(PROPERTY_FIRMWARE_VERSION, fw);
                }
                updateStatus("DISCONNECTED".equals(tahomaStatus) ? ThingStatus.OFFLINE : ThingStatus.ONLINE);
            }
        }
    }

    public String getGateWayId() {
        return getThing().getConfiguration().get("id").toString();
    }

}
