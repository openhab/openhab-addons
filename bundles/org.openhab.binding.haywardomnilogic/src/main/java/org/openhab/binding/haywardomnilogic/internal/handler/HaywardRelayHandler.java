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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardThingHandler;

/**
 * The Relay Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardRelayHandler extends HaywardThingHandler {

    public HaywardRelayHandler(Thing thing) {
        super(thing);
    }

    public void getTelemetry(String xmlResponse) throws Exception {
        List<String> data = new ArrayList<>();
        List<String> systemIDs = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            systemIDs = bridgehandler.evaluateXPath("//Relay/@systemId", xmlResponse);
            String thingSystemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // State
                    data = bridgehandler.evaluateXPath("//Sensor/@relayState", xmlResponse);
                    // for (int i = 0; i < systemIDs.size(); i++) {
                    // handleHaywardTelemetry(HaywardTypeToRequest.RELAY, systemIDs.get(i),
                    // HaywardBindingConstants.CHANNEL_RELAY_STATE, data.get(i));
                    // updateData(HaywardBindingConstants.CHANNEL_RELAY_STATE, data);

                    // }
                }
            }
        }
    }
}
