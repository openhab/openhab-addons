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
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCMaintenanceStatus;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCMultiStatusItem;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCSingleStatusItem;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeConnectServiceStatus} implements the Status Service for
 * WeConnect.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
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
        WCVehicleStatusData status = data.wcStatus;
        if (status == null) {
            logger.warn("{}: Unable to read vehicle status, can't create channels!", thingId);
            return false;
        }

        addChannels(channels, CHANNEL_GROUP_STATUS, true, CHANNEL_STATUS_ERROR);
        addChannels(channels, CHANNEL_GROUP_RANGE, status.fuelStatus != null && status.fuelStatus.rangeStatus != null
                && status.fuelStatus.rangeStatus.value != null, CHANNEL_RANGE_TOTAL, CHANNEL_RANGE_PRANGE);
        addChannels(channels, CHANNEL_GROUP_CHARGER, status.charging != null && status.charging.batteryStatus != null,
                CHANNEL_CHARGER_CHGLVL);

        addChannels(channels, CHANNEL_GROUP_CHARGER, status.charging != null && status.charging.chargingStatus != null,
                CHANNEL_CONTROL_CHARGER, CHANNEL_CHARGER_CHG_STATE, CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_REMAINING,
                CHANNEL_CHARGER_MAXCURRENT, CHANNEL_CONTROL_TARGETCHG, CHANNEL_CHARGER_POWER, CHANNEL_CHARGER_RATE);
        addChannels(channels, CHANNEL_GROUP_CHARGER, status.charging != null && status.charging.plugStatus != null,
                CHANNEL_CHARGER_PLUG_STATE, CHANNEL_CHARGER_LOCK_STATE);
        addChannels(channels, CHANNEL_GROUP_CLIMATER,
                status.climatisation != null && status.climatisation.climatisationStatus != null,
                CHANNEL_CLIMATER_GEN_STATE, CHANNEL_CLIMATER_REMAINING);
        addChannels(channels, CHANNEL_GROUP_CONTROL,
                status.climatisation != null && status.climatisation.climatisationSettings != null,
                CHANNEL_CONTROL_CLIMATER, CHANNEL_CONTROL_TARGET_TEMP);
        addChannels(channels, CHANNEL_GROUP_CLIMATER,
                status.climatisation != null && status.climatisation.climatisationTimer != null,
                CHANNEL_STATUS_TIMEINCAR);
        addChannels(channels, CHANNEL_GROUP_CONTROL,
                status.climatisation != null && status.climatisation.windowHeatingStatus != null,
                CHANNEL_CONTROL_WINHEAT);

        addChannels(channels, CHANNEL_GROUP_MAINT,
                status.vehicleHealthInspection != null
                        && status.vehicleHealthInspection.maintenanceStatus.value != null,
                CHANNEL_STATUS_ODOMETER, CHANNEL_MAINT_DISTINSP, CHANNEL_MAINT_DISTTIME, CHANNEL_MAINT_OILDIST,
                CHANNEL_MAINT_OILINTV);
        addChannels(
                channels, CHANNEL_GROUP_STATUS, status.vehicleLights != null
                        && status.vehicleLights.lightsStatus != null && status.vehicleLights.lightsStatus.value != null,
                CHANNEL_STATUS_LIGHTS);
        addChannels(channels, CHANNEL_GROUP_LOCATION, data.vehicleLocation.isValid(), CHANNEL_LOCATTION_GEO,
                CHANNEL_LOCATTION_ADDRESS, CHANNEL_LOCATTION_TIME);
        addChannels(channels, CHANNEL_GROUP_LOCATION, data.parkingPosition.isValid(), CHANNEL_PARK_LOCATION,
                CHANNEL_PARK_ADDRESS, CHANNEL_PARK_TIME);
        if (status.access != null && status.access.accessStatus != null && status.access.accessStatus.value != null) {
            addChannels(channels, CHANNEL_GROUP_STATUS, status.access.accessStatus.value.overallStatus != null,
                    CHANNEL_STATUS_LOCKED);
            addChannels(channels, CHANNEL_GROUP_DOORS, status.access.accessStatus.value.doors != null,
                    CHANNEL_DOORS_FLSTATE, CHANNEL_DOORS_FLLOCKED, CHANNEL_DOORS_FRSTATE, CHANNEL_DOORS_FRLOCKED,
                    CHANNEL_DOORS_RLSTATE, CHANNEL_DOORS_RLLOCKED, CHANNEL_DOORS_RRSTATE, CHANNEL_DOORS_RRLOCKED,
                    CHANNEL_DOORS_HOODSTATE, CHANNEL_DOORS_TRUNKLSTATE, CHANNEL_DOORS_TRUNKLLOCKED);
            addChannels(channels, CHANNEL_GROUP_WINDOWS, status.access.accessStatus.value.windows != null,
                    CHANNEL_WIN_FLSTATE, CHANNEL_WIN_RLSTATE, CHANNEL_WIN_FRSTATE, CHANNEL_WIN_RRSTATE,
                    CHANNEL_WIN_FROOFSTATE, CHANNEL_WIN_SROOFSTATE);
        }
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean updated = false;
        api.setConfig(this.getConfig());
        api.getHttp().setConfig(this.getConfig());
        VehicleStatus data = api.getVehicleStatus();
        WCVehicleStatusData status = data.wcStatus;
        if (status != null) {
            logger.debug("{}: Vehicle Status:\n{}", thingId, status);

            // updated |= updateChannel(CHANNEL_STATUS_ERROR, getStringType(status.error));
            updated |= updateAccess(status);
            updated |= updateRange(status);
            updated |= updateChargingStatus(status);
            updated |= updateClimatisationStatus(status);
            updated |= updateWindowHeatStatus(status);
            updated |= updateMaintenanceStatus(status);
            updated |= updateLightStatus(status);
            updated |= updatePosition(data);
        }
        return updated;
    }

    private boolean updateRange(WCVehicleStatusData status) {
        boolean updated = false;
        if (status.fuelStatus != null && status.fuelStatus.rangeStatus != null
                && status.fuelStatus.rangeStatus.value != null) {
            updated |= updateChannel(CHANNEL_RANGE_TOTAL,
                    getDecimal(status.fuelStatus.rangeStatus.value.totalRange_km));
            updated |= updateChannel(CHANNEL_RANGE_PRANGE,
                    getDecimal(status.fuelStatus.rangeStatus.value.primaryEngine.remainingRange_km));
        }
        return updated;
    }

    private boolean updateChargingStatus(WCVehicleStatusData status) {
        boolean updated = false;
        if (status.charging != null) {
            if (status.charging.chargingStatus != null && status.charging.chargingStatus.value != null) {
                updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CHARGER,
                        "charging".equalsIgnoreCase(getString(status.charging.chargingStatus.value.chargingState))
                                ? OnOffType.ON
                                : OnOffType.OFF);
                updated |= updateChannel(CHANNEL_CHARGER_CHG_STATE,
                        getStringType(status.charging.chargingStatus.value.chargingState));
                updated |= updateChannel(CHANNEL_CHARGER_MODE,
                        getStringType(status.charging.chargingStatus.value.chargeMode));
                updated |= updateChannel(CHANNEL_CHARGER_REMAINING,
                        getDecimal(status.charging.chargingStatus.value.remainingChargingTimeToComplete_min));
                updated |= updateChannel(CHANNEL_CHARGER_POWER,
                        getDecimal(status.charging.chargingStatus.value.chargePower_kW));
                updated |= updateChannel(CHANNEL_CHARGER_RATE,
                        getDecimal(status.charging.chargingStatus.value.chargeRate_kmph));
            }
            if (status.charging.chargingSettings != null && status.charging.chargingSettings.value != null) {
                updated |= updateChannel(CHANNEL_CONTROL_TARGETCHG,
                        getDecimal(status.charging.chargingSettings.value.targetSOC_pct));
                String maxCurrent = getString(status.charging.chargingSettings.value.maxChargeCurrentAC);
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

            if (status.charging.batteryStatus != null && status.charging.batteryStatus.value != null) {
                updated |= updateChannel(CHANNEL_CHARGER_CHGLVL,
                        getDecimal(status.charging.batteryStatus.value.currentSOC_pct));
            }
            if (status.charging.plugStatus != null && status.charging.plugStatus.value != null) {
                updated |= updateChannel(CHANNEL_CHARGER_LOCK_STATE,
                        getOnOff("locked".equals(getString(status.charging.plugStatus.value.plugLockState))));
                updated |= updateChannel(CHANNEL_CHARGER_PLUG_STATE,
                        getStringType(status.charging.plugStatus.value.plugConnectionState));
            }
        }
        return updated;
    }

    private boolean updateClimatisationStatus(WCVehicleStatusData status) {
        boolean updated = false;
        if (status.climatisation != null && status.climatisation.climatisationStatus != null
                && status.climatisation.climatisationStatus.value != null) {
            updated |= updateChannel(CHANNEL_CLIMATER_GEN_STATE,
                    getStringType(status.climatisation.climatisationStatus.value.climatisationState));

            updated |= updateChannel(CHANNEL_CLIMATER_REMAINING,
                    getDecimal(status.climatisation.climatisationStatus.value.remainingClimatisationTime_min));
        }
        if (status.climatisation != null && status.climatisation.climatisationSettings != null
                && status.climatisation.climatisationSettings.value != null) {
            updated |= updateChannel(CHANNEL_CONTROL_TARGET_TEMP,
                    getDecimal(status.climatisation.climatisationSettings.value.targetTemperature_C));
        }
        if (status.climatisation != null && status.climatisation.climatisationTimer != null
                && status.climatisation.climatisationTimer.value != null) {
            updated |= updateChannel(CHANNEL_STATUS_TIMEINCAR,
                    getDateTime(status.climatisation.climatisationTimer.value.timeInCar));
        }
        return updated;
    }

    private boolean updateWindowHeatStatus(WCVehicleStatusData status) {
        boolean updated = false;
        if (status.climatisation != null && status.climatisation.windowHeatingStatus != null
                && status.climatisation.windowHeatingStatus.value != null) {
            // show only aggregated status
            boolean on = false;
            for (int i = 0; i < status.climatisation.windowHeatingStatus.value.windowHeatingStatus.size(); i++) {
                on |= "on".equals(getString(
                        status.climatisation.windowHeatingStatus.value.windowHeatingStatus.get(i).windowHeatingState));
            }
            updated |= updateChannel(CHANNEL_CONTROL_WINHEAT, on ? OnOffType.ON : OnOffType.OFF);
        }
        return updated;
    }

    private boolean updateMaintenanceStatus(WCVehicleStatusData status) {
        boolean updated = false;
        if (status != null && status.vehicleHealthInspection != null
                && status.vehicleHealthInspection.maintenanceStatus != null
                && status.vehicleHealthInspection.maintenanceStatus.value != null) {
            WCMaintenanceStatus data = status.vehicleHealthInspection.maintenanceStatus.value;
            int odometer = getInteger(data.mileageKm); // sometimes the API returns 0
            updated |= updateChannel(CHANNEL_STATUS_ODOMETER, odometer > 0 ? getDecimal(odometer) : UnDefType.UNDEF);
            updated |= updateChannel(CHANNEL_MAINT_DISTINSP, getDecimal(data.inspectionDueKm));
            updated |= updateChannel(CHANNEL_MAINT_DISTTIME, getDecimal(data.inspectionDueDays));
            updated |= updateChannel(CHANNEL_MAINT_OILDIST, getDecimal(data.oilServiceDueKm));
            updated |= updateChannel(CHANNEL_MAINT_OILINTV, getDecimal(data.oilServiceDueDays));
        }
        return updated;
    }

    private boolean updateLightStatus(WCVehicleStatusData status) {
        boolean updated = false;
        if (status != null && status.vehicleLights != null && status.vehicleLights.lightsStatus != null
                && status.vehicleLights.lightsStatus.value != null) {
            boolean lightsOn = false;
            for (WCSingleStatusItem light : status.vehicleLights.lightsStatus.value.lights) {
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

    private boolean updateAccess(WCVehicleStatusData status) {
        boolean updated = false;
        if (status != null && status.access != null && status.access.accessStatus != null
                && status.access.accessStatus.value != null) {
            updated |= updateChannel(CHANNEL_STATUS_LOCKED,
                    getOnOff(status.access.accessStatus.value.overallStatus.equalsIgnoreCase("safe")));

            if (status.access.accessStatus.value.doors != null) {
                // Map door status to channels
                for (WCMultiStatusItem door : status.access.accessStatus.value.doors) {
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

            if (status.access.accessStatus.value.windows != null) {
                // Map window status to channels
                for (WCMultiStatusItem window : status.access.accessStatus.value.windows) {
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
