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
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.binding.haywardomnilogic.internal.config.HaywardConfig;
import org.openhab.core.thing.Thing;

/**
 * The Heater Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardHeaterHandler extends HaywardThingHandler {

    HaywardConfig config = getConfig().as(HaywardConfig.class);

    public HaywardHeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void getTelemetry(String xmlResponse) throws Exception {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            systemIDs = bridgehandler.evaluateXPath("//Heater/@systemId", xmlResponse);
            String thingSystemID = getThing().getUID().getId();
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // Operating Mode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(i));

                    // State
                    data = bridgehandler.evaluateXPath("//Heater/@heaterState", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_HEATER_STATE, data.get(i));

                    // Enable
                    data = bridgehandler.evaluateXPath("//Heater/@enable", xmlResponse);
                    if (data.get(i).equals("0")) {
                        updateData(HaywardBindingConstants.CHANNEL_HEATER_ENABLE, "0");
                    } else {
                        updateData(HaywardBindingConstants.CHANNEL_HEATER_ENABLE, "1");
                    }
                }
            }
        }
    }
}
