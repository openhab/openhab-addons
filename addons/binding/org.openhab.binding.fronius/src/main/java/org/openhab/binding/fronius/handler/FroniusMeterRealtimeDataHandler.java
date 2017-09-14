/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.fronius.FroniusBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

/**
 * Handler for Fronius Meter Realtime Data.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusMeterRealtimeDataHandler extends FroniusDeviceThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusMeterRealtimeDataHandler.class);
    private final JsonParser parser = new JsonParser();

    public FroniusMeterRealtimeDataHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getServiceDescription() {
        return FroniusBindingConstants.METER_REALTIME_DATA_DESCRIPTION;
    }

    @Override
    protected String getServiceUrl() {
        return FroniusBindingConstants.METER_REALTIME_DATA_URL;
    }

    @Override
    protected void updateData(String data) {
        logger.debug("Refresh data {}", data);
    }

}
