/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import java.util.Calendar;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.api.ThermostatApi;
import io.swagger.client.model.NAMeasureResponse;
import io.swagger.client.model.NASetpoint;
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
public class NATherm1Handler extends NetatmoModuleHandler<NAThermostat> {

    public NATherm1Handler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void updateChannels(Object module) {
        measurableChannels.getAsCsv().ifPresent(csvParams -> {
            ThermostatApi thermostatApi = getBridgeHandler().getThermostatApi();
            NAMeasureResponse measures = thermostatApi.getmeasure(getParentId(), "max", csvParams, getId(), null, null,
                    1, true, true);
            measurableChannels.setMeasures(measures);
        });
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_THERM_ORIENTATION:
                return module != null ? toDecimalType(module.getThermOrientation()) : UnDefType.UNDEF;
            case CHANNEL_THERM_RELAY:
                return module != null ? module.getThermRelayCmd() == 100 ? OnOffType.ON : OnOffType.OFF
                        : UnDefType.UNDEF;
            case CHANNEL_TEMPERATURE:
                return module != null ? toDecimalType(module.getMeasured().getTemperature()) : UnDefType.UNDEF;
            case CHANNEL_SETPOINT_TEMP:
                return module != null ? toDecimalType(module.getMeasured().getSetpointTemp()) : UnDefType.UNDEF;
            case CHANNEL_TIMEUTC:
                return module != null ? toDateTimeType(module.getMeasured().getTime()) : UnDefType.UNDEF;
            case CHANNEL_SETPOINT_END_TIME: {
                if (module != null) {
                    NASetpoint setpoint = module.getSetpoint();
                    if (setpoint != null) {
                        Integer endTime = setpoint.getSetpointEndtime();
                        if (endTime == null) {
                            endTime = getNextSchedule(module.getThermProgramList());
                        }
                        return toDateTimeType(endTime);
                    }
                    return UnDefType.NULL;
                }
                return UnDefType.UNDEF;
            }
            case CHANNEL_SETPOINT_MODE: {
                return module != null ? module.getSetpoint() != null
                        ? ChannelTypeUtils.toStringType(module.getSetpoint().getSetpointMode())
                        : UnDefType.NULL : UnDefType.UNDEF;
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
                        getBridgeHandler().getThermostatApi().setthermpoint(getParentId(), getId(), command.toString(),
                                null, null);

                        updateState(channelUID, new StringType(command.toString()));
                        requestParentRefresh();
                        break;
                    }
                    case CHANNEL_SETPOINT_TEMP: {
                        // Switch the thermostat to manual mode on the desired setpoint for given duration
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, getSetpointDefaultDuration());
                        getBridgeHandler().getThermostatApi().setthermpoint(getParentId(), getId(), "manual",
                                (int) (cal.getTimeInMillis() / 1000), Float.parseFloat(command.toString()));
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

    private Integer getSetpointDefaultDuration() {
        return (Integer) config.get(SETPOINT_DEFAULT_DURATION);
    }

}
