/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.thermostat;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.NATherm1StateDescriptionProvider;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
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
    private final Logger logger = LoggerFactory.getLogger(NATherm1Handler.class);
    private final NATherm1StateDescriptionProvider stateDescriptionProvider;

    public NATherm1Handler(@NonNull Thing thing, NATherm1StateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected void updateProperties(NAThermostat moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType());
    }

    @Override
    public void updateChannels(Object moduleObject) {
        if (isRefreshRequired()) {
            measurableChannels.getAsCsv().ifPresent(csvParams -> {
                ThermostatApi thermostatApi = getBridgeHandler().getThermostatApi();
                NAMeasureResponse measures = thermostatApi.getmeasure(getParentId(), "max", csvParams, getId(), null,
                        null, 1, true, true);
                measurableChannels.setMeasures(measures);
            });
            setRefreshRequired(false);
        }
        super.updateChannels(moduleObject);

        if (module != null) {
            updateStateDescription(module);
        }
    }

    private void updateStateDescription(NAThermostat thermostat) {
        List<StateOption> options = new ArrayList<>();
        for (NAThermProgram planning : thermostat.getThermProgramList()) {
            options.add(new StateOption(planning.getProgramId(), planning.getName()));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PLANNING), options);
    }

    @SuppressWarnings("null")
    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_THERM_ORIENTATION:
                return module != null ? toDecimalType(module.getThermOrientation()) : UnDefType.UNDEF;
            case CHANNEL_THERM_RELAY:
                return module != null ? module.getThermRelayCmd() == 100 ? OnOffType.ON : OnOffType.OFF
                        : UnDefType.UNDEF;
            case CHANNEL_TEMPERATURE:
                return module != null ? toQuantityType(module.getMeasured().getTemperature(), API_TEMPERATURE_UNIT)
                        : UnDefType.UNDEF;
            case CHANNEL_SETPOINT_TEMP:
                return getCurrentSetpoint();
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
            case CHANNEL_SETPOINT_MODE:
                return getSetpoint();
            case CHANNEL_PLANNING: {
                String currentPlanning = "-";
                if (module != null) {
                    for (NAThermProgram program : module.getThermProgramList()) {
                        if (program.getSelected() == Boolean.TRUE) {
                            currentPlanning = program.getProgramId();
                        }
                    }
                    return toStringType(currentPlanning);
                }
            }
        }
        return super.getNAThingProperty(channelId);
    }

    @SuppressWarnings("null")
    private State getSetpoint() {
        return module != null
                ? module.getSetpoint() != null ? toStringType(module.getSetpoint().getSetpointMode()) : UnDefType.NULL
                : UnDefType.UNDEF;
    }

    @SuppressWarnings("null")
    private State getCurrentSetpoint() {
        if (module != null && module.getSetpoint() != null) {
            NASetpoint setPoint = module.getSetpoint();
            String currentMode = setPoint.getSetpointMode();

            NAThermProgram currentProgram = module.getThermProgramList().stream()
                    .filter(p -> p.getSelected() != null && p.getSelected()).findFirst().get();
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
                    return UnDefType.UNDEF;
            }
        }
        return UnDefType.NULL;
    }

    private NAZone getZone(List<NAZone> zones, int searchedId) {
        return zones.stream().filter(z -> z.getId() == searchedId).findFirst().get();
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

        Optional<NAThermProgram> currentProgram = thermProgramList.stream()
                .filter(p -> p.getSelected() != null && p.getSelected()).findFirst();

        if (currentProgram.isPresent()) {
            Stream<NATimeTableItem> pastPrograms = currentProgram.get().getTimetable().stream()
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
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_SETPOINT_MODE: {
                        String targetMode = command.toString();
                        if (CHANNEL_SETPOINT_MODE_MANUAL.equals(targetMode)) {
                            logger.info(
                                    "Switching to manual mode is done by assigning a setpoint temperature - command dropped");
                            updateState(channelUID, getSetpoint());
                        } else {
                            pushSetpointUpdate(targetMode, null, null);
                        }
                        break;
                    }
                    case CHANNEL_SETPOINT_TEMP: {
                        BigDecimal spTemp = null;
                        if (command instanceof QuantityType) {
                            @SuppressWarnings("unchecked")
                            QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command)
                                    .toUnit(API_TEMPERATURE_UNIT);
                            if (quantity != null) {
                                spTemp = quantity.toBigDecimal().setScale(1, RoundingMode.HALF_UP);
                            }
                        } else {
                            spTemp = new BigDecimal(command.toString()).setScale(1, RoundingMode.HALF_UP);
                        }
                        if (spTemp != null) {
                            pushSetpointUpdate(CHANNEL_SETPOINT_MODE_MANUAL, getSetpointEndTime(), spTemp.floatValue());
                        }

                        break;
                    }
                    case CHANNEL_PLANNING: {
                        getBridgeHandler().getThermostatApi().switchschedule(getParentId(), getId(),
                                command.toString());
                        updateState(channelUID, new StringType(command.toString()));
                        invalidateParentCacheAndRefresh();
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }

    private void pushSetpointUpdate(String target_mode, Integer setpointEndtime, Float setpointTemp) {
        getBridgeHandler().getThermostatApi().setthermpoint(getParentId(), getId(), target_mode, setpointEndtime,
                setpointTemp);
        invalidateParentCacheAndRefresh();
    }

    private int getSetpointEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, getSetPointDefaultDuration());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    private int getSetPointDefaultDuration() {
        // TODO : this informations could be sourced from Netatmo API instead of a local configuration element
        return ((Number) config.get(SETPOINT_DEFAULT_DURATION)).intValue();
    }

}
