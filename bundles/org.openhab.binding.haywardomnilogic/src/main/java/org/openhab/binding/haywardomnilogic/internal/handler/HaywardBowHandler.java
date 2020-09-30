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
 * The Body of Water Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardBowHandler extends HaywardThingHandler {

    public HaywardBowHandler(Thing thing) {
        super(thing);
    }

    public void getTelemetry(String xmlResponse, String systemID) throws Exception {
        List<String> data = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            // Flow
            data = bridgehandler.evaluateXPath("//BodyOfWater/@flow", xmlResponse);
            updateData(systemID, HaywardBindingConstants.CHANNEL_BOW_FLOW, data.get(0));

            // Water Temp
            data = bridgehandler.evaluateXPath("//BodyOfWater/@waterTemp", xmlResponse);
            updateData(systemID, HaywardBindingConstants.CHANNEL_BOW_WATERTEMP, data.get(0));
        }
    }
}
