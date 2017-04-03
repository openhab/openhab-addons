/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Calendar;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NATherm1Configuration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.NAModuleAdapter;

import io.swagger.client.CollectionFormats.CSVParams;
import io.swagger.client.api.ThermostatApi;
import io.swagger.client.model.NAThermProgram;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NATimeTableItem;

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
    public void updateChannels(NAModuleAdapter module) {
        if (measuredChannels.size() > 0) {
            ThermostatApi thermostatApi = getBridgeHandler().getThermostatApi();
            String parentId = configuration.getParentId();
            String moduleId = module.getId();
            CSVParams csvParams = new CSVParams(measuredChannels);
            measures = thermostatApi.getmeasure(parentId, "max", csvParams, moduleId, null, null, 1, true, true);
        }
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {

        if (module != null) {
            NAThermostat thermostat = module.getThermostat();
            if (thermostat != null) {
                switch (channelId) {
                    case CHANNEL_THERM_ORIENTATION:
                        return new DecimalType(thermostat.getThermOrientation());
                    case CHANNEL_THERM_RELAY:
                        return thermostat.getThermRelayCmd() == 100 ? OnOffType.ON : OnOffType.OFF;
                    case CHANNEL_TEMPERATURE:
                        return ChannelTypeUtils.toDecimalType(thermostat.getMeasured().getTemperature());
                    case CHANNEL_SETPOINT_TEMP:
                        return ChannelTypeUtils.toDecimalType(thermostat.getMeasured().getSetpointTemp());
                    case CHANNEL_TIMEUTC:
                        return ChannelTypeUtils.toDateTimeType(thermostat.getMeasured().getTime());
                    case CHANNEL_SETPOINT_END_TIME: {
                        if (thermostat.getSetpoint() == null) {
                            return UnDefType.NULL;
                        }

                        Integer endTime = thermostat.getSetpoint().getSetpointEndtime();
                        if (endTime != null) {
                            return ChannelTypeUtils.toDateTimeType(endTime);
                        } else {
                            return ChannelTypeUtils.toDateTimeType(getNextSchedule(thermostat.getThermProgramList()));
                        }

                    }
                    case CHANNEL_SETPOINT_MODE: {
                        return thermostat.getSetpoint() != null
                                ? new StringType(thermostat.getSetpoint().getSetpointMode()) : UnDefType.NULL;
                    }

                }
            }
        }
        return super.getNAThingProperty(channelId);
    }

    private int getNextSchedule(List<NAThermProgram> thermProgramList) {
        Calendar mondayZero = Calendar.getInstance();
        mondayZero.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        mondayZero.set(Calendar.HOUR_OF_DAY, 0);
        mondayZero.set(Calendar.MINUTE, 0);
        mondayZero.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - mondayZero.getTimeInMillis()) / 1000 / 60;

        int result = -1;

        for (NAThermProgram thermProgram : thermProgramList) {
            if (thermProgram.getSelected() != null && thermProgram.getSelected().booleanValue()) {
                // By default we'll use the first slot of next week - this case will be true if
                // we are in the last schedule of the week so below loop will not exit by break
                int next = thermProgram.getTimetable().get(0).getMOffset() + (7 * 24 * 60);

                for (NATimeTableItem timeTable : thermProgram.getTimetable()) {
                    if (timeTable.getMOffset() > diff) {
                        next = timeTable.getMOffset();
                        break;
                    }
                }

                result = (int) (next * 60 + (mondayZero.getTimeInMillis() / 1000));

            }
        }
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!(command instanceof RefreshType)) {
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_SETPOINT_MODE: {
                        getBridgeHandler().getThermostatApi().setthermpoint(configuration.getParentId(),
                                configuration.getEquipmentId(), command.toString(), null, null);

                        updateState(channelUID, new StringType(command.toString()));
                        requestParentRefresh();
                        break;
                    }
                    case CHANNEL_SETPOINT_TEMP: {
                        // Switch the thermostat to manual mode on the desired setpoint for given duration
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, configuration.setpointDefaultDuration);
                        getBridgeHandler().getThermostatApi().setthermpoint(configuration.getParentId(),
                                configuration.getEquipmentId(), "manual", (int) (cal.getTimeInMillis() / 1000),
                                Float.parseFloat(command.toString()));
                        updateState(channelUID, new DecimalType(command.toString()));
                        requestParentRefresh();
                        break;
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }

}
