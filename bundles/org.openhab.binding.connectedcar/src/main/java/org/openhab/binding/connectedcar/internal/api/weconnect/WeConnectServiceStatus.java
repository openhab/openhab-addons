/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCMultiStatusItem;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCSingleStatusItem;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus.WCMaintenanceStatus;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeConnectServiceStatus} implements the Status Service for WeConnect.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WeConnectServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(WeConnectServiceStatus.class);
    static Map<String, String> MAP_DOOR_NAME = new HashMap<>();
    static Map<String, String> MAP_WINDOW_NAME = new HashMap<>();
    static {
        MAP_DOOR_NAME.put("bonnet", "hood");
        MAP_DOOR_NAME.put("frontLeft", "doorFrontLeft");
        MAP_DOOR_NAME.put("frontRight", "doorFrontRight");
        MAP_DOOR_NAME.put("rearLeft", "doorRearLeft");
        MAP_DOOR_NAME.put("rearRight", "doorRearRight");
        MAP_DOOR_NAME.put("trunk", "trunkLid");

        MAP_WINDOW_NAME.put("frontLeft", "windowFrontLeft");
        MAP_WINDOW_NAME.put("frontRight", "windowFrontRight");
        MAP_WINDOW_NAME.put("rearLeft", "windowRearLeft");
        MAP_WINDOW_NAME.put("rearRight", "windowRearRight");
        MAP_WINDOW_NAME.put("roofCover", "roofFrontCover");
        MAP_WINDOW_NAME.put("sunRoof", "sunRoofCover");
    }
    String thingId = API_BRAND_VWID;

    public WeConnectServiceStatus(ThingBaseHandler thingHandler, ApiBase api) {
        super(API_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
        thingId = getConfig().vehicle.vin;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // Try to query status information from vehicle
        VehicleStatus data = api.getVehicleStatus();
        WCVehicleStatus status = data.wcStatus;
        if (status == null) {
            logger.warn("{}: Unable to read vehicle status, can't create channels!", thingId);
            return false;
        }

        addChannels(channels, true, CHANNEL_STATUS_ERROR);
        addChannels(channels, status.rangeStatus != null, CHANNEL_RANGE_TOTAL, CHANNEL_RANGE_PRANGE);
        addChannels(channels, status.batteryStatus != null, CHANNEL_CHARGER_CHGLVL);
        addChannels(channels, status.chargingStatus != null, CHANNEL_CONTROL_CHARGER, CHANNEL_CHARGER_CHG_STATE,
                CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_REMAINING, CHANNEL_CHARGER_MAXCURRENT, CHANNEL_CONTROL_TARGETCHG,
                CHANNEL_CHARGER_POWER, CHANNEL_CHARGER_RATE);
        addChannels(channels, status.plugStatus != null, CHANNEL_CHARGER_PLUG_STATE, CHANNEL_CHARGER_LOCK_STATE);
        addChannels(channels, status.climatisationStatus != null, CHANNEL_CLIMATER_GEN_STATE,
                CHANNEL_CLIMATER_REMAINING);
        addChannels(channels, status.climatisationSettings != null, CHANNEL_CONTROL_CLIMATER,
                CHANNEL_CONTROL_TARGET_TEMP);
        addChannels(channels, status.climatisationTimer != null, CHANNEL_STATUS_TIMEINCAR);
        addChannels(channels, status.windowHeatingStatus != null, CHANNEL_CONTROL_WINHEAT);
        addChannels(channels, status.maintenanceStatus != null, CHANNEL_STATUS_ODOMETER, CHANNEL_MAINT_DISTINSP,
                CHANNEL_MAINT_DISTTIME, CHANNEL_MAINT_OILDIST, CHANNEL_MAINT_OILINTV);
        addChannels(channels, status.lightStatus != null, CHANNEL_STATUS_LIGHTS);
        addChannels(channels, data.vehicleLocation.isValid(), CHANNEL_LOCATTION_GEO, CHANNEL_LOCATTION_ADDRESS,
                CHANNEL_LOCATTION_TIME);
        addChannels(channels, data.parkingPosition.isValid(), CHANNEL_PARK_LOCATION, CHANNEL_PARK_ADDRESS,
                CHANNEL_PARK_TIME);
        if (status.accessStatus != null) {
            addChannels(channels, status.accessStatus.overallStatus != null, CHANNEL_STATUS_LOCKED);
            addChannels(channels, status.accessStatus.doors != null, CHANNEL_DOORS_FLSTATE, CHANNEL_DOORS_FLLOCKED,
                    CHANNEL_DOORS_FRSTATE, CHANNEL_DOORS_FRLOCKED, CHANNEL_DOORS_RLSTATE, CHANNEL_DOORS_RLLOCKED,
                    CHANNEL_DOORS_RRSTATE, CHANNEL_DOORS_RRLOCKED, CHANNEL_DOORS_HOODSTATE, CHANNEL_DOORS_TRUNKLSTATE,
                    CHANNEL_DOORS_TRUNKLLOCKED);
            addChannels(channels, status.accessStatus.windows != null, CHANNEL_WIN_FLSTATE, CHANNEL_WIN_RLSTATE,
                    CHANNEL_WIN_FRSTATE, CHANNEL_WIN_RRSTATE, CHANNEL_WIN_FROOFSTATE, CHANNEL_WIN_SROOFSTATE);
        }
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean updated = false;

        VehicleStatus data = api.getVehicleStatus();
        WCVehicleStatus status = data.wcStatus;
        if (status != null) {
            logger.debug("{}: Vehicle Status:\n{}", thingId, status);

            String group = CHANNEL_GROUP_STATUS;
            updated |= updateChannel(group, CHANNEL_STATUS_ERROR, getStringType(status.error));
            updated |= updateAccess(status);
            updated |= updateRange(status);
            updated |= updateChargingStatus(status);
            updated |= updateClimatisationStatus(status);
            updated |= updateWindowHeatStatus(status);
            updated |= updateMaintenanceStatus(status);
            updated |= updatLightStatus(status);
            updated |= updatePosition(data);
        }
        return updated;
    }

    private boolean updateRange(WCVehicleStatus status) {
        boolean updated = false;
        if (status.rangeStatus != null) {
            String group = CHANNEL_GROUP_RANGE;
            updated |= updateChannel(group, CHANNEL_RANGE_TOTAL,
                    toQuantityType(getInteger(status.rangeStatus.totalRange_km), 1, KILOMETRE));
            updated |= updateChannel(group, CHANNEL_RANGE_PRANGE,
                    toQuantityType(getInteger(status.rangeStatus.primaryEngine.remainingRange_km), 1, KILOMETRE));
        }
        return updated;
    }

    private boolean updateChargingStatus(WCVehicleStatus status) {
        boolean updated = false;
        String group = CHANNEL_GROUP_CHARGER;
        if (status.chargingStatus != null) {
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CHARGER,
                    "charging".equalsIgnoreCase(getString(status.chargingStatus.chargingState)) ? OnOffType.ON
                            : OnOffType.OFF);
            updated |= updateChannel(group, CHANNEL_CHARGER_CHG_STATE,
                    getStringType(status.chargingStatus.chargingState));
            updated |= updateChannel(group, CHANNEL_CHARGER_MODE, getStringType(status.chargingStatus.chargeMode));
            updated |= updateChannel(group, CHANNEL_CHARGER_REMAINING, toQuantityType(
                    getInteger(status.chargingStatus.remainingChargingTimeToComplete_min), 0, Units.MINUTE));
            updated |= updateChannel(group, CHANNEL_CHARGER_POWER, getDecimal(status.chargingStatus.chargePower_kW));
            updated |= updateChannel(group, CHANNEL_CHARGER_RATE, getDecimal(status.chargingStatus.chargeRate_kmph));
            updated |= updateChannel(group, CHANNEL_CONTROL_TARGETCHG,
                    toQuantityType(getInteger(status.chargingSettings.targetSOC_pct), 0, PERCENT));
            String maxCurrent = getString(status.chargingSettings.maxChargeCurrentAC);
            if ("maximum".equalsIgnoreCase(maxCurrent)) {
                maxCurrent = "255";
            }
            if (Character.isDigit(maxCurrent.charAt(0))) {
                updated |= updateChannel(CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_MAXCURRENT,
                        getDecimal(Integer.parseInt(maxCurrent)));
            } else {
                logger.debug("{}: MaxCurrent returned String {}", thingId, maxCurrent);
            }
        }
        if (status.batteryStatus != null) {
            updated |= updateChannel(group, CHANNEL_CHARGER_CHGLVL,
                    toQuantityType(getInteger(status.batteryStatus.currentSOC_pct), 0, PERCENT));
        }
        if (status.plugStatus != null) {
            updated |= updateChannel(group, CHANNEL_CHARGER_LOCK_STATE,
                    getOnOff("locked".equals(getString(status.plugStatus.plugLockState))));
            updated |= updateChannel(group, CHANNEL_CHARGER_PLUG_STATE,
                    getStringType(status.plugStatus.plugConnectionState));
        }
        return updated;
    }

    private boolean updateClimatisationStatus(WCVehicleStatus status) {
        boolean updated = false;
        String group = CHANNEL_GROUP_CLIMATER;
        if (status.climatisationStatus != null) {
            updated |= updateChannel(group, CHANNEL_CLIMATER_GEN_STATE,
                    getOnOffType(status.climatisationStatus.climatisationState));
            updated |= updateChannel(group, CHANNEL_CLIMATER_REMAINING, toQuantityType(
                    getInteger(status.climatisationStatus.remainingClimatisationTime_min), 0, Units.MINUTE));
        }
        if (status.climatisationSettings != null) {
            updated |= updateChannel(group, CHANNEL_CONTROL_TARGET_TEMP,
                    toQuantityType(getDouble(status.climatisationSettings.targetTemperature_C), 0, SIUnits.CELSIUS));
        }
        if (status.climatisationTimer != null) {
            updated |= updateChannel(CHANNEL_STATUS_TIMEINCAR, getDateTime(status.climatisationTimer.timeInCar));
        }
        return updated;
    }

    private boolean updateWindowHeatStatus(WCVehicleStatus status) {
        boolean updated = false;
        if (status.windowHeatingStatus != null) {
            // show only aggregated status
            boolean on = false;
            for (int i = 0; i < status.windowHeatingStatus.windowHeatingStatus.size(); i++) {
                on |= "on".equals(getString(status.windowHeatingStatus.windowHeatingStatus.get(i).windowHeatingState));
            }
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_WINHEAT, on ? OnOffType.ON : OnOffType.OFF);
        }
        return updated;
    }

    private boolean updateMaintenanceStatus(WCVehicleStatus status) {
        boolean updated = false;
        WCMaintenanceStatus data = status.maintenanceStatus;
        if (status.maintenanceStatus != null) {
            int odometer = getInteger(data.mileageKm); // sometimes the API returns 0
            updated |= updateChannel(CHANNEL_STATUS_ODOMETER, odometer > 0 ? getDecimal(odometer) : UnDefType.UNDEF);
            updated |= updateChannel(CHANNEL_MAINT_DISTINSP, getDecimal(data.inspectionDueKm));
            updated |= updateChannel(CHANNEL_MAINT_DISTTIME, getDecimal(data.inspectionDueDays));
            updated |= updateChannel(CHANNEL_MAINT_OILDIST, getDecimal(data.oilServiceDueKm));
            updated |= updateChannel(CHANNEL_MAINT_OILINTV, getDecimal(data.oilServiceDueDays));
        }
        return updated;
    }

    private boolean updatLightStatus(WCVehicleStatus status) {
        boolean updated = false;
        if (status.lightStatus != null) {
            boolean lightsOn = false;
            for (WCSingleStatusItem light : status.lightStatus.lights) {
                lightsOn |= "on".equalsIgnoreCase(light.status);
            }
            updated |= updateChannel(CHANNEL_STATUS_LIGHTS, getOnOff(lightsOn));
        }
        return updated;
    }

    private boolean updatePosition(VehicleStatus status) {
        boolean updated = false;
        if (status.vehicleLocation.isValid()) {
            PointType point = status.vehicleLocation.asPointType();
            updated |= updateChannel(CHANNEL_LOCATTION_GEO, point);
            updated |= updateLocationAddress(point, CHANNEL_LOCATTION_ADDRESS);
            updated |= updateChannel(CHANNEL_LOCATTION_TIME, getDateTime(status.vehicleLocation.parkingTimeUTC));
        }
        if (status.parkingPosition.isValid()) {
            PointType point = status.parkingPosition.asPointType();
            updated |= updateChannel(CHANNEL_PARK_LOCATION, point);
            updated |= updateLocationAddress(point, CHANNEL_PARK_ADDRESS);
            updated |= updateChannel(CHANNEL_PARK_TIME, getDateTime(status.parkingPosition.parkingTimeUTC));
        }
        return updated;
    }

    private boolean updateAccess(WCVehicleStatus status) {
        boolean updated = false;
        if (status.accessStatus != null) {
            updated |= updateChannel(CHANNEL_STATUS_LOCKED,
                    getOnOff(status.accessStatus.overallStatus.equalsIgnoreCase("safe")));

            if (status.accessStatus.doors != null) {
                // Map door status to channels
                for (WCMultiStatusItem door : status.accessStatus.doors) {
                    String channelPre = MAP_DOOR_NAME.get(door.name);
                    if (channelPre == null) {
                        // unknown name
                        continue;
                    }
                    String channelSuf = "";
                    State value = UnDefType.UNDEF;
                    for (String s : door.status) {
                        switch (s) {
                            case "locked":
                            case "unlocked":
                                channelSuf = "Locked";
                                value = getOnOff("locked".equalsIgnoreCase(s));
                                break;
                            case "open:":
                            case "closed":
                                channelSuf = "State";
                                value = "closed".equalsIgnoreCase(s) ? OpenClosedType.CLOSED : OpenClosedType.CLOSED;
                                break;
                            case "unsupported":
                                channelSuf = "State";
                                value = UnDefType.UNDEF;
                                break;
                            default:
                                continue;
                        }
                        updated |= updateChannel(channelPre + channelSuf, value);
                    }
                }
            }

            if (status.accessStatus.windows != null) {
                // Map window status to channels
                for (WCMultiStatusItem window : status.accessStatus.windows) {
                    String channelPre = MAP_WINDOW_NAME.get(window.name);
                    if (channelPre == null) {
                        // unknown name
                        continue;
                    }
                    String channelSuf = "";
                    State value = UnDefType.UNDEF;
                    for (String s : window.status) {
                        switch (s) {
                            case "open:":
                            case "closed":
                                channelSuf = "State";
                                value = "closed".equalsIgnoreCase(s) ? OpenClosedType.CLOSED : OpenClosedType.CLOSED;
                                break;
                            case "unsupported":
                                channelSuf = "State";
                                value = UnDefType.UNDEF;
                                break;
                            default:
                                continue;
                        }
                        updated |= updateChannel(channelPre + channelSuf, value);
                    }
                }
            }
        }

        return updated;
    }
}
