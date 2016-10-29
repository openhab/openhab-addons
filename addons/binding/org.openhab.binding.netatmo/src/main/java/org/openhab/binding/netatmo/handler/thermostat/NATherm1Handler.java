/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NATherm1Configuration;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.NAModuleAdapter;

import io.swagger.client.CollectionFormats.CSVParams;
import io.swagger.client.model.NAThermostat;

/**
 * {@link NATherm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NATherm1Handler extends NetatmoModuleHandler<NATherm1Configuration> {

    public NATherm1Handler(Thing thing) {
        super(thing, NATherm1Configuration.class);

    }

    @Override
    public void updateChannels(NetatmoBridgeHandler bridgeHandler, NAModuleAdapter module) {
        measures = bridgeHandler.getThermostatApi().getmeasure(configuration.getParentId(), "max",
                new CSVParams(measuredChannels), module.getId(), null, null, 1, true, true);
        super.updateChannels(bridgeHandler, module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NAThermostat thermostat = module.getThermostat();
        if (thermostat != null) {
            switch (channelId) {
                case CHANNEL_TEMPERATURE:
                    return toDecimalType(thermostat.getMeasured().getTemperature());
                case CHANNEL_SETPOINT_TEMP:
                    return toDecimalType(thermostat.getMeasured().getSetpointTemp());
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(thermostat.getMeasured().getTime());
                case CHANNEL_SETPOINT_MODE:
                    if (thermostat.getSetpoint() != null) {
                        return new StringType(thermostat.getSetpoint().getSetpointMode());
                    }
            }
        }
        return super.getNAThingProperty(channelId);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        NetatmoBridgeHandler bridgeHandler = (NetatmoBridgeHandler) getBridge().getHandler();
        try {
            if (command == RefreshType.REFRESH) {
                updateChannels(configuration.getParentId());
            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_SETPOINT_MODE:
                        bridgeHandler.getThermostatApi().setthermpoint(configuration.getParentId(),
                                configuration.getEquipmentId(), command.toString(), null, null);
                        break;
                    case CHANNEL_SETPOINT_TEMP:
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, configuration.setpointDefaultDuration);
                        bridgeHandler.getThermostatApi().setthermpoint(configuration.getParentId(),
                                configuration.getEquipmentId(), "manual", (int) (cal.getTimeInMillis() / 1000),
                                Float.parseFloat(command.toString()));
                        break;

                    default:
                        super.handleCommand(channelUID, command);
                }
            }

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

}
