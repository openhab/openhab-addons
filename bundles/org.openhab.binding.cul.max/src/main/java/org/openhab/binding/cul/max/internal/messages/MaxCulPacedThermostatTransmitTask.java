/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages;

import static org.openhab.binding.cul.max.internal.MaxCulBindingConstants.*;

import java.util.TimerTask;

import org.openhab.binding.cul.max.internal.handler.MaxCulCunBridgeHandler;
import org.openhab.binding.cul.max.internal.handler.MaxDevicesHandler;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 */
public class MaxCulPacedThermostatTransmitTask extends TimerTask {

    private MaxCulCunBridgeHandler messageHandler;
    private MaxDevicesHandler maxDevicesHandler;
    private ThermostatControlMode mode;
    private double temperature;

    public MaxCulPacedThermostatTransmitTask(ThermostatControlMode mode, double temperature,
            MaxDevicesHandler maxDevicesHandler, MaxCulCunBridgeHandler messageHandler) {
        this.maxDevicesHandler = maxDevicesHandler;
        this.messageHandler = messageHandler;
        this.temperature = temperature;
        this.mode = mode;
    }

    private void sendToDevices(ThermostatControlMode mode, double temp) {
        saveSendTemperature(maxDevicesHandler, mode, temp);
        /* send temperature to associated devices */
        for (MaxDevicesHandler associatedDevice : maxDevicesHandler.getAssociations()) {
            saveSendTemperature(associatedDevice, mode, temp);
        }
    }

    private void saveSendTemperature(MaxDevicesHandler device, ThermostatControlMode mode, double temp) {
        ThingTypeUID thingTypeUID = device.getThing().getThingTypeUID();
        if (HEATINGTHERMOSTAT_THING_TYPE.equals(thingTypeUID) || HEATINGTHERMOSTATPLUS_THING_TYPE.equals(thingTypeUID)
                || WALLTHERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            messageHandler.sendSetTemperature(device, mode, temp);
        }
    }

    @Override
    public void run() {
        sendToDevices(mode, temperature);
    }
}
