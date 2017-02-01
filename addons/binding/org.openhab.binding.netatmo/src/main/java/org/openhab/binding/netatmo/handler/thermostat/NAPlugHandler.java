/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;

import io.swagger.client.model.NADeviceListBody;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAPlugHandler extends NetatmoDeviceHandler {

    public NAPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateChannels() {
        try {
            NADeviceListBody deviceList = bridgeHandler.getThermostatApi().devicelist(actualApp, getId(), false)
                    .getBody();
            device = deviceList.getDevices().get(0);

            super.updateChannels();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

}
