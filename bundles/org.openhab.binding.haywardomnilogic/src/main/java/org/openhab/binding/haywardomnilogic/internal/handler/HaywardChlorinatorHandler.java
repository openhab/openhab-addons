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
 * The Chlorinator Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardChlorinatorHandler extends HaywardThingHandler {

    public HaywardChlorinatorHandler(Thing thing) {
        super(thing);
    }

    public void getTelemetry(String xmlResponse) throws Exception {
        List<String> data = new ArrayList<>();
        List<String> systemIDs = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            systemIDs = bridgehandler.evaluateXPath("//Chlorinator/@systemId", xmlResponse);
            String thingSystemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // Operating Mode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(0));

                    // Timed Percent
                    data = bridgehandler.evaluateXPath("//Chlorinator/@Timed-Percent", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT, data.get(0));
                    bridgehandler.chlorTimedPercent = data.get(0);

                    // scMode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@scMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_SCMODE, data.get(0));

                    // Error
                    data = bridgehandler.evaluateXPath("//Chlorinator/@chlrError", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ERROR, data.get(0));

                    // Alert
                    data = bridgehandler.evaluateXPath("//Chlorinator/@chlrAlert", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ALERT, data.get(0));

                    // Average Salt Level
                    data = bridgehandler.evaluateXPath("//Chlorinator/@avgSaltLevel", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_AVGSALTLEVEL, data.get(0));

                    // Instant Salt Level
                    data = bridgehandler.evaluateXPath("//Chlorinator/@instantSaltLevel", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_INSTANTSALTLEVEL, data.get(0));

                    // Status
                    data = bridgehandler.evaluateXPath("//Chlorinator/@status", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_STATUS, data.get(0));

                    if (data.get(0).equals("0")) {
                        updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, "0");
                        // chlorState is used to set the chlorinator cfgState in the timedPercent command
                        bridgehandler.chlorState = "2";
                    } else {
                        updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, "1");
                        // chlorState is used to set the chlorinator cfgState in the timedPercent command
                        bridgehandler.chlorState = "3";
                    }
                }
            }
        }
    }
}
