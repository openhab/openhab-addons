/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.energy;

import io.rudolph.netatmo.api.common.model.MeasureRequestResponse;
import io.rudolph.netatmo.api.common.model.Scale;
import io.rudolph.netatmo.api.common.model.ScaleType;
import io.rudolph.netatmo.api.energy.EnergyConnector;
import io.rudolph.netatmo.api.energy.model.ThermPointMode;
import io.rudolph.netatmo.api.energy.model.Timetable;
import io.rudolph.netatmo.api.energy.model.Zone;
import io.rudolph.netatmo.api.energy.model.module.SetPoint;
import io.rudolph.netatmo.api.energy.model.module.ThermProgram;
import io.rudolph.netatmo.api.energy.model.module.ThermostatModule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ThermosthatStateDescriptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Temperature;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

/**
 * {@link ThermosthatHandler} is the class used to handle the energy
 * module of a energy set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 */
public class ThermosthatHandler extends NetatmoModuleHandler<ThermostatModule> {
    private final Logger logger = LoggerFactory.getLogger(ThermosthatHandler.class);
    private final ThermosthatStateDescriptionProvider stateDescriptionProvider;

    public ThermosthatHandler(@NonNull Thing thing, ThermosthatStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected void updateProperties(ThermostatModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType().getValue());
    }

    @Override
    public void updateChannels(Object moduleObject) {
        if (isRefreshRequired()) {

            EnergyConnector thermostatApi = getBridgeHandler().api.getEnergyApi();
            List<MeasureRequestResponse> measures = thermostatApi.getMeasure(getId(),
                    getParentId(),
                    Scale.MAX,
                    ScaleType.TEMPERATURE,
                    null,
                    null,
                    null,
                    true,
                    true).executeSync();

            measurableChannels.setMeasures(measures);
        }
        setRefreshRequired(false);

        super.updateChannels(moduleObject);

        if (module != null) {
            updateStateDescription(module);
        }
    }

    private void updateStateDescription(ThermostatModule thermostat) {
        List<StateOption> options = new ArrayList<>();
        for (ThermProgram planning : thermostat.getThermProgramList()) {
            options.add(new StateOption(planning.getProgramId(), planning.getName()));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PLANNING), options);
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
                return module != null ? toQuantityType(module.getMeasure().getTemperature(), API_TEMPERATURE_UNIT)
                        : UnDefType.UNDEF;
            case CHANNEL_ROOM_SETPOINT_TEMPERATURE:
                return getCurrentSetpoint();
            case CHANNEL_TIMEUTC:
                return module != null ? toDateTimeType((int) module.getMeasure().getTime().toEpochSecond(ZoneOffset.UTC)) : UnDefType.UNDEF;
            case CHANNEL_ROOM_SETPOINT_END_TIME: {
                if (module != null) {
                    SetPoint setpoint = module.getSetPoint();
                    if (setpoint != null) {
                        LocalDateTime endDateTime = setpoint.getSetPointEndTime();
                        final int endTime;
                        if (endDateTime == null) {
                            endTime = getNextProgramTime(module.getThermProgramList());
                        } else {
                            endTime = (int) endDateTime.toEpochSecond(ZoneOffset.UTC);
                        }
                        return toDateTimeType(endTime);
                    }
                    return UnDefType.NULL;
                }
                return UnDefType.UNDEF;
            }
            case CHANNEL_ROOM_SETPOINT_MODE:
                return getSetPoint();
            case CHANNEL_PLANNING: {
                String currentPlanning = "-";
                if (module != null) {
                    for (ThermProgram program : module.getThermProgramList()) {
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

    private State getSetPoint() {
        return module != null
                ? module.getSetPoint() != null ? toStringType(module.getSetPoint().getSetPointMode()) : UnDefType.NULL
                : UnDefType.UNDEF;
    }

    private State getCurrentSetpoint() {
        if (module != null && module.getSetPoint() != null) {
            SetPoint setPoint = module.getSetPoint();
            String currentMode = setPoint.getSetPointMode();

            ThermProgram currentProgram = module.getThermProgramList().stream()
                    .filter(ThermProgram::getSelected).findFirst().get();
            switch (currentMode) {
                case CHANNEL_SETPOINT_MODE_MANUAL:
                    return toDecimalType(setPoint.getSetpointTemp());
                case CHANNEL_SETPOINT_MODE_AWAY:
                    Zone zone = getZone(currentProgram.getZones(), 2);
                    return toDecimalType(zone.getTemp());
                case CHANNEL_SETPOINT_MODE_HG:
                    Zone zone1 = getZone(currentProgram.getZones(), 3);
                    return toDecimalType(zone1.getTemp());
                case CHANNEL_SETPOINT_MODE_PROGRAM:
                    Timetable currentProgramMode = getCurrentProgramMode(module.getThermProgramList());
                    if (currentProgramMode != null) {
                        Zone zone2 = getZone(currentProgram.getZones(), currentProgramMode.getZoneId());
                        return toDecimalType(zone2.getTemp());
                    }
                case CHANNEL_SETPOINT_MODE_OFF:
                case CHANNEL_SETPOINT_MODE_MAX:
                    return UnDefType.UNDEF;
            }
        }
        return UnDefType.NULL;
    }

    private Zone getZone(List<Zone> zones, int searchedId) {
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

    private Timetable getCurrentProgramMode(List<ThermProgram> thermProgramList) {
        Timetable lastProgram = null;
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        Optional<ThermProgram> currentProgram = thermProgramList.stream()
                .filter(ThermProgram::getSelected).findFirst();

        if (currentProgram.isPresent()) {
            Stream<Timetable> pastPrograms = currentProgram.get().getTimetable().stream()
                    .filter(t -> t != null && t.getMOffset() != null && t.getMOffset() < diff);
            lastProgram = pastPrograms.reduce((first, second) -> second).orElse(null);
        }

        return lastProgram;
    }

    private int getNextProgramTime(List<ThermProgram> thermProgramList) {
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() - getNetatmoProgramBaseTime()) / 1000 / 60;

        int result = -1;

        for (ThermProgram thermProgram : thermProgramList) {
            if (thermProgram.getSelected()) {
                // By default we'll use the first slot of next week - this case will be true if
                // we are in the last schedule of the week so below loop will not exit by break
                int next = thermProgram.getTimetable().get(0).getMOffset() + (7 * 24 * 60);

                for (Timetable timeTable : thermProgram.getTimetable()) {
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
                    case CHANNEL_ROOM_SETPOINT_MODE: {
                        String target_mode = command.toString();
                        if (CHANNEL_SETPOINT_MODE_MANUAL.equals(target_mode)) {
                            logger.info(
                                    "Switching to manual mode is done by assigning a setpoint temperature - command dropped");
                            updateState(channelUID, getSetPoint());
                        } else {
                            pushSetpointUpdate(target_mode, null, null);
                        }
                        break;
                    }
                    case CHANNEL_ROOM_SETPOINT_TEMPERATURE: {
                        BigDecimal spTemp = null;
                        if (command instanceof QuantityType) {
                            QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command)
                                    .toUnit(API_TEMPERATURE_UNIT);
                            if (quantity != null) {
                                spTemp = quantity.toBigDecimal().setScale(1, RoundingMode.HALF_UP);
                            }
                        } else {
                            spTemp = new BigDecimal(command.toString()).setScale(1, RoundingMode.HALF_UP);
                        }
                        if (spTemp != null) {
                            pushSetpointUpdate(CHANNEL_SETPOINT_MODE_MANUAL, getSetPointEndTime(), spTemp.floatValue());
                        }

                        break;
                    }
                    case CHANNEL_PLANNING: {
                        getBridgeHandler().api.getEnergyApi().switchHomeSchedule(getId(), getParentId()).executeSync();
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
        getBridgeHandler().api.getEnergyApi().setRoomThermPoint(getParentId(), getId(), ThermPointMode.valueOf(target_mode), setpointTemp, LocalDateTime.ofEpochSecond(setpointEndtime, 0, ZoneOffset.UTC));
        invalidateParentCacheAndRefresh();
    }

    private int getSetPointEndTime() {
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
