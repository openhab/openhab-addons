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

import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaStatus;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;

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
    public void refresh(String channel) {
        String id = getGateWayId();
        SomfyTahomaStatus status = getTahomaStatus(id);
        String tahomaStatus = status.getStatus();
        Channel ch = thing.getChannel(channel);
        if (ch != null) {
            updateState(ch.getUID(), new StringType(tahomaStatus));
        }
        // update the firmware property
        String fw = status.getProtocolVersion();
        updateProperty(PROPERTY_FIRMWARE_VERSION, fw);

        updateStatus("DISCONNECTED".equals(tahomaStatus) ? ThingStatus.OFFLINE : ThingStatus.ONLINE);
    }

    public String getGateWayId() {
        return getThing().getConfiguration().get("id").toString();
    }
}
