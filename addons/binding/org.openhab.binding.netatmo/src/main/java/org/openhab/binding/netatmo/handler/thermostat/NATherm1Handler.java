/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADevice;
import io.swagger.client.model.NADeviceListBody;
import io.swagger.client.model.NAModule;
import io.swagger.client.model.NAThermStateBody;
import io.swagger.client.model.NAThermStateResponse;

/**
 * {@link NATherm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NATherm1Handler extends NetatmoModuleHandler {
    private NAThermStateBody thermStateBody;
    private Integer setpointDefaultDuration = null;

    public NATherm1Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateChannels() {
        try {
            NAThermStateResponse thermState = bridgeHandler.getThermostatApi().getthermstate(getParentId(), getId());
            thermStateBody = thermState.getBody();

            NADeviceListBody deviceList = bridgeHandler.getThermostatApi().devicelist(actualApp, getParentId(), false)
                    .getBody();
            for (NAModule module : deviceList.getModules()) {
                if (module.getId().equalsIgnoreCase(getId())) {
                    this.module = module;
                    super.updateChannels();
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }

    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_SETPOINT_MODE:
                if (thermStateBody.getSetpoint() != null) {
                    return new StringType(thermStateBody.getSetpoint().getSetpointMode());
                } else {
                    return null;
                }
            case CHANNEL_SETPOINT_TEMP:
                return new DecimalType(thermStateBody.getMeasured().getSetpointTemp());
            case CHANNEL_BOILER_ON:
                return new DecimalType(dashboard.getBoilerOn());
            case CHANNEL_BOILER_OFF:
                return new DecimalType(dashboard.getBoilerOff());
            default:
                return super.getNAThingProperty(chanelId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SETPOINT_MODE:
                    bridgeHandler.getThermostatApi().setthermpoint(getParentId(), getId(), command.toString(), null,
                            null);
                    break;
                case CHANNEL_SETPOINT_TEMP:

                    if (setpointDefaultDuration == null) {
                        NADeviceListBody deviceListBody = bridgeHandler.getThermostatApi()
                                .devicelist(actualApp, getParentId(), false).getBody();
                        NADevice plugDevice = deviceListBody.getDevices().get(0);
                        setpointDefaultDuration = plugDevice.getSetpointDefaultDuration();
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, setpointDefaultDuration);
                    bridgeHandler.getThermostatApi().setthermpoint(getParentId(), getId(), "manual",
                            (int) (cal.getTimeInMillis() / 1000), Float.parseFloat(command.toString()));
                    break;
            }

            updateChannels();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

}
