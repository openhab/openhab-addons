/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.APIUtils.*;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NATherm1StateDescriptionProvider;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.api.ThermostatApi;
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
@NonNullByDefault
public class NATherm1Handler extends NetatmoModuleHandler<NAThermostat> {
    private final Logger logger = LoggerFactory.getLogger(NATherm1Handler.class);
    private final NATherm1StateDescriptionProvider stateDescriptionProvider;

    public NATherm1Handler(Thing thing, NATherm1StateDescriptionProvider stateDescriptionProvider,
            final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected void updateProperties(NAThermostat moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType());
    }

    @Override
    public void updateChannels(Object moduleObject) {
        super.updateChannels(moduleObject);
        getModule().ifPresent(this::updateStateDescription);
    }

    private void updateStateDescription(NAThermostat thermostat) {
        List<StateOption> options = new ArrayList<>();
        for (NAThermProgram planning : nonNullList(thermostat.getThermProgramList())) {
            options.add(new StateOption(planning.getProgramId(), planning.getName()));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PLANNING), options);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        Optional<NAThermostat> thermostat = getModule();
        switch (channelId) {
            case CHANNEL_THERM_ORIENTATION:
                return thermostat.map(m -> toDecimalType(m.getThermOrientation())).orElse(UnDefType.UNDEF);
            case CHANNEL_THERM_RELAY:
                return thermostat.map(m -> m.getThermRelayCmd() == 100 ? (State) OnOffType.ON : (State) OnOffType.OFF)
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_TEMPERATURE:
                return thermostat.map(m -> toQuantityType(m.getMeasured().getTemperature(), API_TEMPERATURE_UNIT))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_SETPOINT_TEMP:
                return getCurrentSetpoint();
            case CHANNEL_TIMEUTC:
                return thermostat.map(m -> toDateTimeType(m.getMeasured().getTime(), timeZoneProvider.getTimeZone()))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_SETPOINT_END_TIME: {
                if (thermostat.isPresent()) {
                    NASetpoint setpoint = thermostat.get().getSetpoint();
                    if (setpoint != null) {
                        Integer endTime = setpoint.getSetpointEndtime();
                        if (endTime == null) {
                            endTime = getNextProgramTime(nonNullList(thermostat.get().getThermProgramList()));
                        }
                        return toDateTimeType(endTime, timeZoneProvider.getTimeZone());
                    }
                    return UnDefType.NULL;
                }
                return UnDefType.UNDEF;
            }
            case CHANNEL_SETPOINT_MODE:
                return getSetpoint();
            case CHANNEL_PLANNING: {
                String currentPlanning = "-";
                if (thermostat.isPresent()) {
                    for (NAThermProgram program : nonNullList(thermostat.get().getThermProgramList())) {
                        if (Boolean.TRUE.equals(program.isSelected())) {
                            currentPlanning = program.getProgramId();
                        }
                    }
                    return toStringType(currentPlanning);
                }
                return UnDefType.UNDEF;
            }
        }
        return super.getNAThingProperty(channelId);
    }

    private State getSetpoint() {
        return getModule()
                .map(m -> m.getSetpoint() != null ? toStringType(m.getSetpoint().getSetpointMode()) : UnDefType.NULL)
                .orElse(UnDefType.UNDEF);
    }

    private State getCurrentSetpoint() {
        Optional<NAThermostat> thermostat = getModule();
        if (thermostat.isPresent()) {
            NASetpoint setPoint = thermostat.get().getSetpoint();
            if (setPoint != null) {
                String currentMode = setPoint.getSetpointMode();

                NAThermProgram currentProgram = nonNullStream(thermostat.get().getThermProgramList())
                        .filter(p -> p.isSelected() != null && p.isSelected()).findFirst().get();
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
                        NATimeTableItem currentProgramMode = getCurrentProgramMode(
                                nonNullList(thermostat.get().getThermProgramList()));
                        if (currentProgramMode != null) {
                            NAZone zone2 = getZone(currentProgram.getZones(), currentProgramMode.getId());
                            return toDecimalType(zone2.getTemp());
                        }
                    case CHANNEL_SETPOINT_MODE_OFF:
                    case CHANNEL_SETPOINT_MODE_MAX:
                        return UnDefType.UNDEF;
                }
            }
        }
        return UnDefType.NULL;
    }

    private NAZone getZone(List<NAZone> zones, int searchedId) {
        return nonNullStream(zones).filter(z -> z.getId() == searchedId).findFirst().get();
    }

    private long getNetatmoProgramBaseTime() {
        Calendar mondayZero = Calendar.getInstance();
        mondayZero.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        mondayZero.set(Calendar.HOUR_OF_DAY, 0);
        mondayZero.set(Calendar.MINUTE, 0);
        mondayZero.set(Calendar.SECOND, 0);
        return mondayZero.getTimeInMillis();
    }

    private @Nullable NATimeTableItem getCurrentProgramMode(List<NAThermProgram> thermProgramList) {
        NATimeTableItem lastProgram = null;
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        Optional<NAThermProgram> currentProgram = thermProgramList.stream()
                .filter(p -> p.isSelected() != null && p.isSelected()).findFirst();

        if (currentProgram.isPresent()) {
            Stream<NATimeTableItem> pastPrograms = nonNullStream(currentProgram.get().getTimetable())
                    .filter(t -> t.getMOffset() < diff);
            Optional<NATimeTableItem> program = pastPrograms.reduce((first, second) -> second);
            if (program.isPresent()) {
                lastProgram = program.get();
            }
        }

        return lastProgram;
    }

    private int getNextProgramTime(List<NAThermProgram> thermProgramList) {
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        int result = -1;

        for (NAThermProgram thermProgram : thermProgramList) {
            if (thermProgram.isSelected() != null && thermProgram.isSelected()) {
                // By default we'll use the first slot of next week - this case will be true if
                // we are in the last schedule of the week so below loop will not exit by break
                List<NATimeTableItem> timetable = thermProgram.getTimetable();
                int next = timetable.get(0).getMOffset() + (7 * 24 * 60);

                for (NATimeTableItem timeTable : timetable) {
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
                        getApi().ifPresent(api -> {
                            api.switchschedule(getParentId(), getId(), command.toString());
                            updateState(channelUID, new StringType(command.toString()));
                            invalidateParentCacheAndRefresh();
                        });
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }

    private void pushSetpointUpdate(String target_mode, @Nullable Integer setpointEndtime,
            @Nullable Float setpointTemp) {
        getApi().ifPresent(api -> {
            api.setthermpoint(getParentId(), getId(), target_mode, setpointEndtime, setpointTemp);
            invalidateParentCacheAndRefresh();
        });
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
        Configuration conf = config;
        Object defaultDuration = conf != null ? conf.get(SETPOINT_DEFAULT_DURATION) : null;
        if (defaultDuration instanceof BigDecimal) {
            return ((BigDecimal) defaultDuration).intValue();
        }
        return 60;
    }

    private Optional<ThermostatApi> getApi() {
        return getBridgeHandler().flatMap(handler -> handler.getThermostatApi());
    }
}
