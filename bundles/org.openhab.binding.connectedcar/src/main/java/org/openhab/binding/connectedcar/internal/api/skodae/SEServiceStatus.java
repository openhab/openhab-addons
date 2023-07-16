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
package org.openhab.binding.connectedcar.internal.api.skodae;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_SERVICE_VEHICLE_STATUS_REPORT;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleSettings.SEChargerSettings;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleSettings.SEClimaterSettings;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEChargerStatus;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEClimaterStatus;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEClimaterStatus.SEHeatingStatus;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEParkingPositionStatus;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEVehicleStatusV2;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEVehicleStatusV2.SEVehicleStatusRemote.SEVehicleStatusItem;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SEServiceStatus} implements the Status Service for Skoda Enyak.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class SEServiceStatus extends ApiBaseService {
    static Map<String, String> MAP_DOOR_NAME = new HashMap<>();
    static Map<String, String> MAP_WINDOW_NAME = new HashMap<>();
    static {
        MAP_DOOR_NAME.put("BONNET", "hood");
        MAP_DOOR_NAME.put("FRONT_LEFT", "doorFrontLeft");
        MAP_DOOR_NAME.put("FRONT_RIGHT", "doorFrontRight");
        MAP_DOOR_NAME.put("REAR_LEFT", "doorRearLeft");
        MAP_DOOR_NAME.put("REAR_RIGHT", "doorRearRight");
        MAP_DOOR_NAME.put("TRUNK", "trunkLid");

        MAP_WINDOW_NAME.put("FRONT_LEFT", "windowFrontLeft");
        MAP_WINDOW_NAME.put("FRONT_RIGHT", "windowFrontRight");
        MAP_WINDOW_NAME.put("REAR_LEFT", "windowRearLeft");
        MAP_WINDOW_NAME.put("REAR_RIGHT", "windowRearRight");
        MAP_WINDOW_NAME.put("ROOF_COVER", "roofFrontCover");
        MAP_WINDOW_NAME.put("SUN_ROOF", "sunRoofCover");
        MAP_WINDOW_NAME.put("SUN_ROOF_REAR", "roofRearCover");
    }

    public SEServiceStatus(ThingBaseHandler thingHandler, ApiBase api) {
        super(API_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        addChannels(channels, CHANNEL_GROUP_GENERAL, true, CHANNEL_GENERAL_UPDATED, CHANNEL_CONTROL_TARGETCHG);
        addChannels(channels, CHANNEL_GROUP_CONTROL, true, CHANNEL_CONTROL_CLIMATER, CHANNEL_CONTROL_CHARGER,
                CHANNEL_CONTROL_WINHEAT, CHANNEL_CONTROL_TARGET_TEMP);

        addChannels(channels, CHANNEL_GROUP_CHARGER, true, CHANNEL_CHARGER_MAXCURRENT, CHANNEL_CHARGER_CHG_STATE,
                CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_CHGLVL, CHANNEL_CHARGER_REMAINING, CHANNEL_CHARGER_RATE,
                CHANNEL_CHARGER_POWER, CHANNEL_CHARGER_PLUG_STATE, CHANNEL_CHARGER_LOCK_STATE);
        addChannels(channels, CHANNEL_GROUP_RANGE, true, CHANNEL_RANGE_TOTAL);
        addChannels(channels, CHANNEL_GROUP_STATUS, true, CHANNEL_STATUS_LOCKED, CHANNEL_STATUS_ODOMETER,
                CHANNEL_STATUS_DOORSCLOSED, CHANNEL_STATUS_WINCLOSED, CHANNEL_STATUS_LIGHTS);
        addChannels(channels, CHANNEL_GROUP_DOORS, true, CHANNEL_DOORS_HOODSTATE, CHANNEL_DOORS_TRUNKLLOCKED,
                CHANNEL_DOORS_FLLOCKED, CHANNEL_DOORS_FRLOCKED, CHANNEL_DOORS_RLLOCKED, CHANNEL_DOORS_RRLOCKED);
        addChannels(channels, CHANNEL_GROUP_WINDOWS, true, CHANNEL_WIN_FLSTATE, CHANNEL_WIN_RLSTATE,
                CHANNEL_WIN_FRSTATE, CHANNEL_WIN_RRSTATE, CHANNEL_WIN_SROOFSTATE, CHANNEL_WIN_RROOFSTATE);
        addChannels(channels, CHANNEL_GROUP_CLIMATER, true, CHANNEL_CLIMATER_GEN_STATE, CHANNEL_CLIMATER_REMAINING);
        addChannels(channels, CHANNEL_GROUP_LOCATION, true, CHANNEL_CAR_MOVING, CHANNEL_PARK_LOCATION,
                CHANNEL_PARK_ADDRESS, CHANNEL_PARK_TIME);

        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        boolean updated = false;

        SEVehicleStatusData status = api.getVehicleStatus().seStatus;
        if (status != null) {
            updated |= updateRangeStatus(status);
            updated |= updateChargingStatus(status);
            updated |= updateClimatisationStatus(status);
            updated |= updateWindowHeatStatus(status);
            updated |= updatePositionStatus(status);
            updated |= updateVehicleStatus(status);
        }
        return updated;
    }

    private boolean updateRangeStatus(SEVehicleStatusData data) {
        boolean updated = false;
        String group = CHANNEL_GROUP_RANGE;
        SEChargerStatus s = data.status.charger;
        if (s != null) {
            updated |= updateChannel(group, CHANNEL_RANGE_TOTAL,
                    toQuantityType(getLong(s.battery.cruisingRangeElectricInMeters) / 1000.0, 1, KILOMETRE));
        }
        return updated;
    }

    private boolean updateChargingStatus(SEVehicleStatusData data) {
        boolean updated = false;
        String group = CHANNEL_GROUP_CHARGER;
        SEChargerStatus s = data.status.charger;
        if (s != null) {
            if (s.charging != null) {
                String state = getString(s.charging.state);
                updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CHARGER,
                        "charging".equalsIgnoreCase(state) ? OnOffType.ON : OnOffType.OFF);
                updated |= updateChannel(group, CHANNEL_CHARGER_CHG_STATE, getStringType(state));
                updated |= updateChannel(group, CHANNEL_CHARGER_MODE, getStringType(s.charging.chargeMode));
                updated |= updateChannel(group, CHANNEL_CHARGER_REMAINING,
                        toQuantityType(getLong(s.charging.remainingToCompleteInSeconds) / 60.0, 0, Units.MINUTE));
                updated |= updateChannel(group, CHANNEL_CHARGER_POWER,
                        toQuantityType(getDouble(s.charging.chargingPowerInWatts), 0, Units.WATT));
                updated |= updateChannel(group, CHANNEL_CHARGER_RATE,
                        getDecimal(s.charging.chargingRateInKilometersPerHour));
            }

            if (s.battery != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_CHGLVL,
                        toQuantityType(getInteger(s.battery.stateOfChargeInPercent), 0, PERCENT));
            }
            if (s.plug != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_LOCK_STATE, getOnOffType(getString(s.plug.lockState)));
                updated |= updateChannel(group, CHANNEL_CHARGER_PLUG_STATE, getStringType(s.plug.connectionState));
            }
        }

        SEChargerSettings set = data.settings.charger;
        if (set != null) {
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_TARGETCHG,
                    toQuantityType(getInteger(set.targetStateOfChargeInPercent), 0, PERCENT));
            updated |= updateChannel(group, CHANNEL_CHARGER_MAXCURRENT, getStringType(set.maxChargeCurrentAc));
        }

        return updated;
    }

    private boolean updateClimatisationStatus(SEVehicleStatusData data) {
        boolean updated = false;
        String group = CHANNEL_GROUP_CLIMATER;

        SEClimaterStatus status = data.status.climatisation;
        if (status != null) {
            updated |= updateChannel(group, CHANNEL_CLIMATER_GEN_STATE, getOnOffType(status.state));
            updated |= updateChannel(group, CHANNEL_CLIMATER_REMAINING,
                    toQuantityType(getInteger(status.remainingTimeToReachTargetTemperatureInSeconds), 0, Units.SECOND));
        }

        SEClimaterSettings settings = data.settings.climater;
        if (settings != null) {
            Double tempC = Units.KELVIN.getConverterTo(SIUnits.CELSIUS).convert(settings.targetTemperatureInKelvin)
                    .doubleValue();
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_TARGET_TEMP,
                    toQuantityType(tempC, 1, SIUnits.CELSIUS));
        }
        return updated;
    }

    private boolean updateWindowHeatStatus(SEVehicleStatusData data) {
        boolean updated = false;
        SEClimaterStatus status = data.status.climatisation;
        if (status != null) {
            // show only aggregated status
            boolean on = false;
            for (SEHeatingStatus s : status.windowsHeatingStatuses) {
                on |= getOnOffType(s.state) == OnOffType.ON;
            }
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_WINHEAT, getOnOff(on));
        }
        return updated;
    }

    private boolean updatePositionStatus(SEVehicleStatusData data) {
        boolean updated = false;
        String group = CHANNEL_GROUP_LOCATION;
        if (data.status == null) {
            return updated;
        }
        SEParkingPositionStatus s = data.status.parkingPosition;
        if (s != null) {
            PointType point = new PointType(new DecimalType(s.latitude), new DecimalType(s.longitude));
            updated |= updateChannel(group, CHANNEL_PARK_LOCATION, point);
            updated |= updateLocationAddress(point, CHANNEL_PARK_ADDRESS);
            updated |= updateChannel(CHANNEL_PARK_TIME, getDateTime(s.lastUpdatedAt));
        }
        updated |= updateChannel(CHANNEL_CAR_MOVING, data.status.carMoving ? OnOffType.ON : OnOffType.OFF);
        return updated;
    }

    private boolean updateVehicleStatus(SEVehicleStatusData data) {
        boolean updated = false;
        SEVehicleStatusV2 s = data.status.vehicleStatus;
        if (s != null) {
            String group = CHANNEL_GROUP_STATUS;
            if (s.remote != null) {
                if (s.remote.status != null) {
                    updated |= updateChannel(group, CHANNEL_STATUS_LOCKED,
                            getOnOff(!"yes".equalsIgnoreCase(s.remote.status.open)));
                    OnOffType state = getOnOff("no".equalsIgnoreCase(s.remote.status.open));
                    updated |= updateChannel(group, CHANNEL_STATUS_WINCLOSED, state);
                    updated |= updateChannel(group, CHANNEL_STATUS_DOORSCLOSED, state);
                }
                double odometer = s.remote.mileageInKm;
                updated |= updateChannel(group, CHANNEL_STATUS_ODOMETER,
                        odometer > 0 ? getDecimal(odometer) : UnDefType.UNDEF);
                if (s.remote.lights != null) {
                    updated |= updateChannel(group, CHANNEL_STATUS_LIGHTS,
                            getOnOff(s.remote.lights.overallStatus == "ON"));
                }

                group = CHANNEL_GROUP_GENERAL;
                if (s.remote.capturedAt != null) {
                    updated |= updateChannel(group, CHANNEL_GENERAL_UPDATED, getDateTime(s.remote.capturedAt));
                }

                group = CHANNEL_GROUP_DOORS;
                if (s.remote.doors != null) {
                    for (SEVehicleStatusItem door : s.remote.doors) {
                        if (door.name != null && MAP_DOOR_NAME.containsKey(door.name)) {
                            @Nullable
                            String channelPre = MAP_DOOR_NAME.get(door.name);
                            updated |= updateStatusItem(door, group, channelPre, "Locked");
                        }
                    }
                }
                group = CHANNEL_GROUP_WINDOWS;
                if (s.remote.windows != null) {
                    for (SEVehicleStatusItem window : s.remote.windows) {
                        if (window.name != null && MAP_WINDOW_NAME.containsKey(window.name)) {
                            @Nullable
                            String channelPre = MAP_WINDOW_NAME.get(window.name);
                            updated |= updateStatusItem(window, group, channelPre, "Pos");
                        }
                    }
                }
            }
        }
        return updated;
    }

    private boolean updateStatusItem(SEVehicleStatusItem item, String group, @Nullable String channelPre,
            String sufLock) {
        String channelSuf = "";
        State value = UnDefType.UNDEF;
        String state = item.status.toLowerCase();
        switch (state) {
            case "locked":
            case "unlocked":
                channelSuf = sufLock;
                value = getOnOff("locked".equalsIgnoreCase(state));
                break;
            case "open":
            case "closed":
                channelSuf = "State";
                value = "closed".equalsIgnoreCase(state) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            case "unsupported":
                channelSuf = "State";
                value = UnDefType.UNDEF;
                break;
        }
        if (channelPre != null) {
            channelSuf = channelPre + channelSuf;
        }
        return updateChannel(channelSuf, value);
    }
}
