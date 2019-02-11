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
package org.openhab.binding.draytonwiser.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;

import java.util.logging.Logger;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Room;
import org.openhab.binding.draytonwiser.internal.config.RoomStat;
import org.openhab.binding.draytonwiser.internal.config.Schedule;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class RoomHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RoomHandler.class);
    private Gson gson;

    @Nullable
    private Room room;

    public RoomHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT)) {
            if (command instanceof QuantityType) {
                int newSetPoint = (int) Math
                        .round(((QuantityType<Temperature>) command).toUnit(CELSIUS).doubleValue() * 10);
                setSetPoint(newSetPoint);
            }
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE)) {
            boolean manualMode = command.toString().toUpperCase().equals("ON");
            setManualMode(manualMode);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_DURATION)) {
            int boostDuration = Math.round((Float.parseFloat(command.toString()) * 60));
            setBoostDuration(boostDuration);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ROOM_WINDOW_STATE_DETECTION)) {
            boolean windowStateDetection = command.toString().toUpperCase().equals("ON");
            setWindowStateDetection(windowStateDetection);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ROOM_MASTER_SCHEDULE)) {
            setMasterScheduleState(command.toString());
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean roomUpdated = updateRoomData();
            if (roomUpdated) {
                updateStatus(ThingStatus.ONLINE);
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_TEMPERATURE),
                        getTemperature());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_HUMIDITY),
                        getHumidity());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT),
                        getSetPoint());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_DEMAND),
                        getDemand());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HEAT_REQUEST),
                        getHeatRequest());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE),
                        getManualModeState());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOSTED),
                        getBoostedState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_REMAINING),
                        getBoostRemainingState());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_ROOM_WINDOW_STATE_DETECTION),
                        getWindowDetectionState());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_WINDOW_STATE),
                        getWindowState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_MASTER_SCHEDULE),
                        getMasterSchedule());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateRoomData() {
        if (bridgeHandler == null) {
            return false;
        }
        room = bridgeHandler.getRoom(getThing().getConfiguration().get("name").toString());
        return room != null;
    }

    private State getSetPoint() {
        if (room != null) {
            return new QuantityType<>((float) room.getCurrentSetPoint() / 10, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }

    private void setSetPoint(Integer setPoint) {
        if (bridgeHandler != null) {
            bridgeHandler.setRoomSetPoint(getThing().getConfiguration().get("name").toString(), setPoint);
        }
    }

    private State getHumidity() {
        if (room != null && room.getRoomStatId() != null) {
            RoomStat roomStat = getRoomStat(room.getRoomStatId());
            if (roomStat != null) {
                return new DecimalType(roomStat.getMeasuredHumidity());
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (room != null) {
            Integer fullScaleTemp = room.getCalculatedTemperature();
            if (fullScaleTemp.equals(DraytonWiserBindingConstants.OFFLINE_TEMPERATURE)) {
                return UnDefType.UNDEF;
            }
            return new QuantityType<>((float) fullScaleTemp / 10, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }

    private State getDemand() {
        if (room != null) {
            return new QuantityType<>(room.getPercentageDemand(), SmartHomeUnits.PERCENT);
        }

        return UnDefType.UNDEF;
    }

    private State getHeatRequest() {
        if (room != null) {
            if (room.getControlOutputState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    @Nullable
    private RoomStat getRoomStat(int id) {
        if (bridgeHandler != null) {
            return bridgeHandler.getRoomStat(id);
        }
        return null;
    }

    private State getManualModeState() {
        if (room != null) {
            if (room.getMode().toUpperCase().equals("MANUAL")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private void setManualMode(Boolean manualMode) {
        if (bridgeHandler != null) {
            bridgeHandler.setRoomManualMode(getThing().getConfiguration().get("name").toString(), manualMode);
        }
    }

    private void setWindowStateDetection(Boolean stateDetection) {
        if (bridgeHandler != null) {
            bridgeHandler.setRoomWindowStateDetection(getThing().getConfiguration().get("name").toString(),
                    stateDetection);
        }
    }

    private State getBoostedState() {
        if (room != null) {
            if (room.getOverrideTimeoutUnixTime() != null && !room.getOverrideType().toUpperCase().equals("NONE")) {
                return OnOffType.ON;
            }
        }
        updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_DURATION),
                new DecimalType(0));

        return OnOffType.OFF;
    }

    private State getBoostRemainingState() {
        if (room != null) {
            if (room.getOverrideTimeoutUnixTime() != null && !room.getOverrideType().toUpperCase().equals("NONE")) {
                return new DecimalType((room.getOverrideTimeoutUnixTime() - (System.currentTimeMillis() / 1000L)) / 60);
            }
        }

        return new DecimalType(0);
    }

    private void setBoostDuration(Integer durationMinutes) {
        if (bridgeHandler != null && room != null) {
            if (durationMinutes > 0) {
                bridgeHandler.setRoomBoostActive(getThing().getConfiguration().get("name").toString(),
                        room.getCalculatedTemperature() + 20, durationMinutes);
            } else {
                bridgeHandler.setRoomBoostInactive(getThing().getConfiguration().get("name").toString());
            }
        }
    }

    private void setMasterScheduleState(String scheduleJSON) {
        if (bridgeHandler != null && room != null) {
            bridgeHandler.setRoomSchedule(getThing().getConfiguration().get("name").toString(), scheduleJSON);
        }
    }

    private State getWindowDetectionState() {
        if (room != null) {
            if (room.getWindowDetectionActive()) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getWindowState() {
        if (room != null) {
            if (room.getWindowState() != null && room.getWindowState().toUpperCase().equals("OPEN")) {
                return OpenClosedType.OPEN;
            }
        }

        return OpenClosedType.CLOSED;
    }

    private State getMasterSchedule() {
        if (room != null && bridgeHandler != null) {
            Integer scheduleId = room.getScheduleId();
            if (scheduleId != null) {
                return new StringType(gson.toJson(bridgeHandler.getSchedule(scheduleId), Schedule.class));
            }
        }

        return new StringType();
    }
}
