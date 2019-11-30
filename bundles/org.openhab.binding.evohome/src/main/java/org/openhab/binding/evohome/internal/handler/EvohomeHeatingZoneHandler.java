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
package org.openhab.binding.evohome.internal.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.models.v2.response.DailySchedule;
import org.openhab.binding.evohome.internal.api.models.v2.response.DailySchedules;
import org.openhab.binding.evohome.internal.api.models.v2.response.Switchpoint;
import org.openhab.binding.evohome.internal.api.models.v2.response.ZoneStatus;

/**
 * The {@link EvohomeHeatingZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Neil Renaud - Working implementation
 * @author Jasper van Zuijlen - Refactor + Permanent Zone temperature setting
 * @author James Kinsman - Added schedule based set point setting
 */
public class EvohomeHeatingZoneHandler extends BaseEvohomeHandler {

    private static final int CANCEL_SET_POINT_OVERRIDE = 0;
    private ThingStatus tcsStatus;
    private ZoneStatus zoneStatus;

    public EvohomeHeatingZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(ThingStatus tcsStatus, ZoneStatus zoneStatus) {
        this.tcsStatus = tcsStatus;
        this.zoneStatus = zoneStatus;

        // Make the zone offline when the related display is offline
        // If the related display is not a thing, ignore this
        if (tcsStatus != null && tcsStatus.equals(ThingStatus.OFFLINE)) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Display Controller offline");
        } else if (zoneStatus == null) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Status not found, check the zone id");
        } else if (!handleActiveFaults(zoneStatus)) {
            updateEvohomeThingStatus(ThingStatus.ONLINE);

            updateState(EvohomeBindingConstants.ZONE_TEMPERATURE_CHANNEL,
                    new DecimalType(zoneStatus.getTemperature().getTemperature()));
            updateState(EvohomeBindingConstants.ZONE_SET_POINT_STATUS_CHANNEL,
                    new StringType(zoneStatus.getHeatSetpoint().getSetpointMode()));
            updateState(EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL,
                    new DecimalType(zoneStatus.getHeatSetpoint().getTargetTemperature()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            update(tcsStatus, zoneStatus);
        } else {
            EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
            if (bridge != null) {
                String channelId = channelUID.getId();
                if (EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL.equals(channelId)
                        && command instanceof DecimalType) {
                    double newTemp = ((DecimalType) command).doubleValue();
                    // Get a local copy of schedule when needed, saves on api calls
                    DailySchedules schedule = getDailySchedules();
                    double expectedTemp = getCurrentSetPoint(schedule);
                    if (newTemp == CANCEL_SET_POINT_OVERRIDE || newTemp == expectedTemp) {
                        bridge.cancelSetPointOverride(getEvohomeThingConfig().id);
                        updateState(EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL, new DecimalType(expectedTemp));
                        return;
                    } else if (newTemp < 5) {
                        newTemp = 5;
                    }
                    if (newTemp >= 5 && newTemp <= 35) {
                        int overrideMode = getOverrideMode();
                        switch (overrideMode) {
                            case EvohomeBindingConstants.SETPOINT_OVERRIDE_TEMPORARY_SCHEDULE_NEXT:
                                bridge.setTemporarySetPoint(getEvohomeThingConfig().id, newTemp,
                                        getNextSetPointChange(schedule));
                                break;
                            case EvohomeBindingConstants.SETPOINT_OVERRIDE_TEMPORARY_TIME_ADD:
                                bridge.setTemporarySetPoint(getEvohomeThingConfig().id, newTemp,
                                        LocalDateTime.now().plusMinutes(getOverrideTime()));
                                break;
                            default:
                                bridge.setPermanentSetPoint(getEvohomeThingConfig().id, newTemp);
                                break;
                        }
                    }
                }
            }
        }
    }

    private DailySchedules getDailySchedules() {
        return this.getEvohomeBridge().getZoneSchedule("temperatureZone", this.getId());
    }

    private LocalDateTime getNextSetPointChange() {
        return getNextSetPointChange(this.getDailySchedules());
    }

    private LocalDateTime getNextSetPointChange(DailySchedules schedule) {
        if (schedule != null) {
            int currentWeekDay = LocalDateTime.now().getDayOfWeek().getValue() - 1;
            LocalDateTime nextSetPointChange = null;
            for (DailySchedule daily : schedule.getSchedules()) {
                // handle wrap around
                int dayDelta = 0;
                if (daily.getWeekday() < currentWeekDay) {
                    // the schedule day is from earlier in the week so add 7 to get to the same day next week
                    dayDelta = daily.getWeekday() + 7 - currentWeekDay;
                } else {
                    dayDelta = daily.getWeekday() - currentWeekDay;
                }
                LocalDate setPointDay = LocalDate.now().plusDays(dayDelta);
                for (Switchpoint sp : daily.getSwitchpoints()) {
                    LocalDateTime spNextOccurance = LocalDateTime.of(setPointDay,
                            LocalTime.parse(sp.getTimeOfDay().toString()));
                    if (spNextOccurance.isAfter(LocalDateTime.now())) {
                        // setPoint is after now we are interested in it.
                        if (nextSetPointChange == null || nextSetPointChange.isAfter(spNextOccurance)) {
                            nextSetPointChange = spNextOccurance;
                        }
                    }
                }
            }
            if (nextSetPointChange == null) {
                return LocalDateTime.now().plusMinutes(getOverrideTime());
            }
            return nextSetPointChange;
        } else {
            return LocalDateTime.now().plusMinutes(getOverrideTime());
        }
    }

    private double getCurrentSetPoint() {
        return getCurrentSetPoint(this.getDailySchedules());
    }

    private double getCurrentSetPoint(DailySchedules schedule) {
        if (schedule != null) {
            int currentWeekDay = LocalDateTime.now().getDayOfWeek().getValue() - 1;
            LocalDateTime previousSetPointChange = null;
            double previousSetPoint = 0;
            for (DailySchedule daily : schedule.getSchedules()) {
                // handle wrap around backwards
                int dayDelta = 0;
                if (daily.getWeekday() > currentWeekDay) {
                    dayDelta = daily.getWeekday() - 7 + currentWeekDay;
                } else {
                    dayDelta = daily.getWeekday() - currentWeekDay;
                }
                LocalDate setPointDay = LocalDate.now().plusDays(dayDelta);
                for (Switchpoint sp : daily.getSwitchpoints()) {
                    LocalDateTime spNextOccurance = LocalDateTime.of(setPointDay,
                            LocalTime.parse(sp.getTimeOfDay().toString()));
                    if (spNextOccurance.isBefore(LocalDateTime.now())) {
                        // setPoint is before now we are interested in it.
                        if (previousSetPointChange == null || previousSetPointChange.isBefore(spNextOccurance)) {
                            previousSetPointChange = spNextOccurance;
                            previousSetPoint = sp.getHeatSetpoint();
                        }
                    }
                }
            }
            if (previousSetPointChange == null) {
                return 0;
            }
            return previousSetPoint;
        } else {
            return 0;
        }
    }

    private boolean handleActiveFaults(ZoneStatus zoneStatus) {
        if (zoneStatus.hasActiveFaults()) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    zoneStatus.getActiveFault(0).getFaultType());
            return true;
        }
        return false;
    }

}
