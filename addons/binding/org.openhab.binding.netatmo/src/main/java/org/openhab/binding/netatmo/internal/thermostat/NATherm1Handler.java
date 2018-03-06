/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.api.ThermostatApi;
import io.swagger.client.model.NAMeasureResponse;
import io.swagger.client.model.NASetpoint;
import io.swagger.client.model.NAThermProgram;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NATimeTableItem;
import io.swagger.client.model.NAZone;

/**
 * {@link NATherm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NATherm1Handler extends NetatmoModuleHandler<NAThermostat> {
    private Logger logger = LoggerFactory.getLogger(NATherm1Handler.class);

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
                return getCurrentSetpoint();
            case CHANNEL_PROGRAM_NAME:
                return getCurrentProgramName();
            case CHANNEL_TIMEUTC:
                return module != null ? toDateTimeType(module.getMeasured().getTime()) : UnDefType.UNDEF;
            case CHANNEL_SETPOINT_END_TIME: {
                if (module != null) {
                    NASetpoint setpoint = module.getSetpoint();
                    if (setpoint != null) {
                        Integer endTime = setpoint.getSetpointEndtime();
                        if (endTime == null) {
                            endTime = getNextProgramTime(module.getThermProgramList());
                        }
                        return toDateTimeType(endTime);
                    }
                    return UnDefType.NULL;
                }
                return UnDefType.UNDEF;
            }
            case CHANNEL_SETPOINT_MODE: {
                return module != null
                        ? module.getSetpoint() != null ? toStringType(module.getSetpoint().getSetpointMode())
                                : UnDefType.NULL
                        : UnDefType.UNDEF;
            }
        }
        return super.getNAThingProperty(channelId);
    }

    private State getCurrentProgramName() {
        if (module != null && module.getSetpoint() != null) {
            String currentMode = module.getSetpoint().getSetpointMode();

            NAThermProgram currentProgram = module.getThermProgramList().stream().filter(p -> p.getSelected() != null)
                    .findFirst().get();
            if (currentProgram != null && CHANNEL_SETPOINT_MODE_PROGRAM.equals(currentMode)) {
                NATimeTableItem currentProgramMode = getCurrentProgramMode(module.getThermProgramList());
                if (currentProgramMode != null) {
                    NAZone zone = getZone(currentProgram.getZones(), currentProgramMode.getId());
                    return toStringType(zone.getName());
                }
            }
        }
        return UnDefType.UNDEF;
    }

    private State getCurrentSetpoint() {
        if (module != null && module.getSetpoint() != null) {
            NASetpoint setPoint = module.getSetpoint();
            String currentMode = setPoint.getSetpointMode();

            NAThermProgram currentProgram = module.getThermProgramList().stream().filter(p -> p.getSelected() != null)
                    .findFirst().get();
            if (currentProgram != null) {
                switch (currentMode) {
                    case CHANNEL_SETPOINT_MODE_MANUAL:
                        return toDecimalType(setPoint.getSetpointTemp());
                    case CHANNEL_SETPOINT_MODE_AWAY:
                        NAZone zone = getZone(currentProgram.getZones(), 2);
                        return toDecimalType(zone.getTemp());
                    case CHANNEL_SETPOINT_MODE_HG:
                        NAZone zone1 = getZone(currentProgram.getZones(), 3);
                        return toDecimalType(zone1.getTemp());
                    case CHANNEL_SETPOINT_MODE_PROGRAM:
                        NATimeTableItem currentProgramMode = getCurrentProgramMode(module.getThermProgramList());
                        if (currentProgramMode != null) {
                            NAZone zone2 = getZone(currentProgram.getZones(), currentProgramMode.getId());
                            return toDecimalType(zone2.getTemp());
                        }
                    case CHANNEL_SETPOINT_MODE_OFF:
                    case CHANNEL_SETPOINT_MODE_MAX:
                        return UnDefType.NULL;
                }
            }
        }
        return UnDefType.UNDEF;
    }

    private NAZone getZone(List<NAZone> zones, int searchedId) {
        NAZone result = zones.stream().filter(z -> z.getId() == searchedId).findFirst().get();
        return result;
    }

    private long getNetatmoProgramBaseTime() {
        Calendar mondayZero = Calendar.getInstance();
        mondayZero.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        mondayZero.set(Calendar.HOUR_OF_DAY, 0);
        mondayZero.set(Calendar.MINUTE, 0);
        mondayZero.set(Calendar.SECOND, 0);

        return mondayZero.getTimeInMillis();
    }

    private NATimeTableItem getCurrentProgramMode(List<NAThermProgram> thermProgramList) {
        NATimeTableItem lastProgram = null;
        Calendar now = Calendar.getInstance();

        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        NAThermProgram currentProgram = thermProgramList.stream()
                .filter(p -> p.getSelected() != null && p.getSelected()).findFirst().get();
        if (currentProgram != null) {
            Stream<NATimeTableItem> pastPrograms = currentProgram.getTimetable().stream()
                    .filter(t -> t.getMOffset() < diff);
            lastProgram = pastPrograms.reduce((first, second) -> second).orElse(null);

        }

        return lastProgram;
    }

    private int getNextProgramTime(List<NAThermProgram> thermProgramList) {
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        int result = -1;

        for (NAThermProgram thermProgram : thermProgramList) {
            if (thermProgram.getSelected() != null && thermProgram.getSelected()) {
                // By default we'll use the first slot of next week - this case will be true if
                // we are in the last schedule of the week so below loop will not exit by break
                int next = thermProgram.getTimetable().get(0).getMOffset() + (7 * 24 * 60);

                for (NATimeTableItem timeTable : thermProgram.getTimetable()) {
                    if (timeTable.getMOffset() > diff) {
                        next = timeTable.getMOffset();
                        break;
                    }
                }

                result = (int) (next * 60 + (getNetatmoProgramBaseTime() / 1000));
            }
        }
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!(command instanceof RefreshType)) {
            switch (channelUID.getId()) {
                case CHANNEL_SETPOINT_MODE: {
                    String target_mode = command.toString();
                    if (!CHANNEL_SETPOINT_MODE_MANUAL.equals(target_mode)) {
                        pushSetpointUpdate(target_mode, null, null);
                    } else {
                        logger.info("Update the setpoint temperature in order to switch to manual mode");
                    }
                    break;
                }
                case CHANNEL_SETPOINT_TEMP: {
                    pushSetpointUpdate(CHANNEL_SETPOINT_MODE_MANUAL, getSetpointEndTime(),
                            Float.parseFloat(command.toString()));
                    break;
                }
            }
        }
    }

    private void pushSetpointUpdate(String target_mode, Integer setpointEndtime, Float setpointTemp) {
        getBridgeHandler().getThermostatApi().setthermpoint(getParentId(), getId(), target_mode, setpointEndtime,
                setpointTemp);
        // Leave a bit of time to Netatmo Server to get in sync with new values sent
        scheduler.schedule(() -> {
            requestParentRefresh(true);
        }, 1800, TimeUnit.MILLISECONDS);
    }

    private int getSetpointEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, getSetPointDefaultDuration());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    private int getSetPointDefaultDuration() {
        return ((Number) config.get(SETPOINT_DEFAULT_DURATION)).intValue();
    }

}
