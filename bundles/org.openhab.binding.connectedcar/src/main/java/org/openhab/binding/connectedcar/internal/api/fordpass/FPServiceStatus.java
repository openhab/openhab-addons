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
package org.openhab.binding.connectedcar.internal.api.fordpass;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus.FPDoorStatus;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus.FPStatusStringValue;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus.FPTpms;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus.FPWindowPosition;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FPServiceStatus} implements the Status Service for Skoda Enyak.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class FPServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(FPServiceStatus.class);
    private String thingId = API_BRAND_FORD;

    public FPServiceStatus(ThingBaseHandler thingHandler, ApiBase api) {
        super(API_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
        thingId = getConfig().vehicle.vin;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // We need one status poll to dynamically determine required channels
        VehicleStatus data = api.getVehicleStatus();
        FPVehicleStatusData fpStatus = data.fpStatus;
        if (fpStatus == null || fpStatus.vehiclestatus == null) {
            throw new ApiException(thingId + "Can't read vehicle data, channels not created!");
        }

        FPVehicleStatus status = fpStatus.vehiclestatus;
        // Control & Status channels
        addChannels(channels, true, CHANNEL_CONTROL_ENGINE, CHANNEL_CONTROL_LOCK, CHANNEL_STATUS_LOCKED,
                CHANNEL_STATUS_ODOMETER, CHANNEL_STATUS_SWUPDATE, CHANNEL_STATUS_DEEPSLEEP);

        // Location
        addChannels(channels, status.gps != null, CHANNEL_LOCATTION_GEO, CHANNEL_LOCATTION_ADDRESS,
                CHANNEL_LOCATTION_TIME);

        // Range
        addChannels(channels, true, CHANNEL_RANGE_TOTAL, CHANNEL_RANGE_PRANGE, CHANNEL_RANGE_FUEL);
        // 2nd engines is electric
        addChannels(channels, status.fuel != null && status.elVehDTE != null, CHANNEL_RANGE_SRANGE);

        // Group maintenance
        addChannels(channels, status.fuel != null, CHANNEL_MAINT_OILPERC, CHANNEL_MAINT_OILWARNLVL);

        // Charger
        addChannels(channels, status.elVehDTE != null, CHANNEL_CHARGER_CHG_STATE, CHANNEL_CHARGER_CHGLVL,
                CHANNEL_CHARGER_PLUG_STATE);

        // Doors
        addChannels(channels, status.doorStatus != null, CHANNEL_STATUS_DOORSCLOSED, CHANNEL_DOORS_FLSTATE,
                CHANNEL_DOORS_FRSTATE, CHANNEL_DOORS_RLSTATE, CHANNEL_DOORS_RRSTATE, CHANNEL_DOORS_TRUNKLSTATE,
                CHANNEL_DOORS_ITAILGSTATE);

        // Windows
        addChannels(channels, status.windowPosition != null, CHANNEL_STATUS_WINCLOSED, CHANNEL_WIN_FLSTATE,
                CHANNEL_WIN_FRSTATE, CHANNEL_WIN_RLSTATE, CHANNEL_WIN_RRSTATE);

        // Tire channels
        addChannels(channels, status.tirePressure != null, CHANNEL_STATUS_TIRESOK, CHANNEL_TIREP_FRONTLEFT,
                CHANNEL_TIREP_FRONTRIGHT, CHANNEL_TIREP_REARLEFT, CHANNEL_TIREP_REARRIGHT);
        addChannels(channels, status.tpms != null && status.tpms.innerLeftRearTirePressure != null,
                CHANNEL_TIREP_INNERREARLEFT, CHANNEL_TIREP_INNERREARRIGHT); // extra tires
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean updated = false;

        FPVehicleStatusData data = api.getVehicleStatus().fpStatus;
        if (data != null) {
            FPVehicleStatus status = data.vehiclestatus;
            if (status != null) {
                boolean locked = "LOCK".equalsIgnoreCase(getString(status.lockStatus.value));
                updated |= updateChannel(CHANNEL_CONTROL_LOCK, getOnOff(locked));
                updated |= updateChannel(CHANNEL_STATUS_ODOMETER, getDecimal(milesToKM(status.odometer.value)));
                updated |= updateChannel(CHANNEL_STATUS_SWUPDATE, getOnOff(status.firmwareUpgInProgress.value));
                updated |= updateChannel(CHANNEL_STATUS_DEEPSLEEP, getOnOff(status.deepSleepInProgress.value));

                boolean ignition = "On".equalsIgnoreCase(status.ignitionStatus.value);
                updated |= updateChannel(CHANNEL_CONTROL_ENGINE, getOnOff(ignition));

                updated |= updateLocation(status);
                updated |= updateRangeStatus(status);
                updated |= updateMaintenance(status);
                updated |= updateChargingStatus(status);
                updated |= updateDoorWindowStatus(status);
                updated |= updateTireStatus(status);
            }
        }
        return updated;
    }

    private boolean updateLocation(FPVehicleStatus status) {
        boolean updated = false;
        if (status.gps != null) {
            PointType gps = new PointType(getDecimal(status.gps.latitude), getDecimal(status.gps.longitude));
            updated |= updateChannel(CHANNEL_LOCATTION_GEO, gps);
            updated |= updateChannel(CHANNEL_LOCATTION_TIME, getDateTime(getString(status.gps.timestamp)));
            updated |= updateLocationAddress(gps, CHANNEL_LOCATTION_ADDRESS);
        }
        return updated;
    }

    private boolean updateRangeStatus(FPVehicleStatus status) {
        boolean updated = false;
        // assume primary engine is alway non-electrical
        // electrical will be secondary, or primary if electrical-only
        double frange = 0.0, erange = 0.0;
        if (status.fuel != null) {
            frange = milesToKM(status.fuel.distanceToEmpty);
            updated |= updateChannel(CHANNEL_RANGE_PRANGE, getDecimal(frange));
            updated |= updateChannel(CHANNEL_RANGE_FUEL, getDecimal(status.fuel.fuelLevel));
        }
        if (status.elVehDTE != null) {
            erange = getDouble(status.elVehDTE.value);
            String chRange = status.fuel != null ? CHANNEL_RANGE_SRANGE : CHANNEL_RANGE_PRANGE;
            updated |= updateChannel(chRange, getDecimal(erange));
        }
        updated |= updateChannel(CHANNEL_RANGE_TOTAL, getDecimal(frange + erange));
        return updated;
    }

    private boolean updateMaintenance(FPVehicleStatus status) {
        boolean updated = false;
        if (status.oil != null) {
            updated |= updateChannel(CHANNEL_MAINT_OILPERC, getDecimal(status.oil.oilLifeActual));
            updated |= updateChannel(CHANNEL_MAINT_OILWARNLVL,
                    getOnOff(!"STATUS_GOOD".equalsIgnoreCase(status.oil.oilLife)));
        }
        return updated;
    }

    private boolean updateChargingStatus(FPVehicleStatus status) {
        boolean updated = false;
        if (status.elVehDTE != null) {
            updated |= updateChannel(CHANNEL_CHARGER_PLUG_STATE, getStringType("" + status.plugStatus.value));
            updated |= updateChannel(CHANNEL_CHARGER_CHG_STATE, getStringType(status.chargingStatus.value));
            updated |= updateChannel(CHANNEL_CHARGER_CHGLVL, getDecimal(status.batteryFillLevel.value));
        }
        return updated;
    }

    private boolean updateDoorWindowStatus(FPVehicleStatus status) {
        boolean updated = false;

        FPWindowPosition windows = status.windowPosition;
        boolean winClosed = true;
        if (windows != null) {
            boolean fl = isWinClosed(windows.driverWindowPosition), fr = isWinClosed(windows.passWindowPosition),
                    rl = isWinClosed(windows.rearDriverWindowPos), rr = isWinClosed(windows.rearPassWindowPos);
            winClosed = fl && fr && rl && rr;
            updateChannel(CHANNEL_WIN_FLSTATE, fl ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateChannel(CHANNEL_WIN_FRSTATE, fr ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateChannel(CHANNEL_WIN_RLSTATE, rl ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateChannel(CHANNEL_WIN_RRSTATE, rr ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateChannel(CHANNEL_STATUS_WINCLOSED, getOnOff(winClosed));
        }

        FPDoorStatus doors = status.doorStatus;
        boolean doorsClosed = true;
        if (doors != null) {
            boolean fl = isDoorClosed(doors.driverDoor), fr = isDoorClosed(doors.passengerDoor),
                    rl = isDoorClosed(doors.leftRearDoor), rr = isDoorClosed(doors.rightRearDoor),
                    hd = isDoorClosed(doors.hoodDoor), tg = isDoorClosed(doors.tailgateDoor),
                    itg = isDoorClosed(doors.innerTailgateDoor);
            doorsClosed = fl && fr && rl && rr && hd && tg && itg;
            updated |= updateChannel(CHANNEL_DOORS_FLSTATE, fl ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_FRSTATE, fr ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_RLSTATE, rl ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_RRSTATE, rr ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_HOODSTATE, hd ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_TRUNKLSTATE, tg ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updated |= updateChannel(CHANNEL_DOORS_ITAILGSTATE, itg ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateChannel(CHANNEL_STATUS_DOORSCLOSED, getOnOff(winClosed));
        }

        boolean locked = "LOCKED".equalsIgnoreCase(getString(status.lockStatus.value)) && winClosed && doorsClosed;
        updateChannel(CHANNEL_STATUS_LOCKED, getOnOff(locked));
        return updated;
    }

    private boolean isWinClosed(@Nullable FPStatusStringValue status) {
        return status == null || "Fully_Closed".equalsIgnoreCase(getString(status.value))
                || "Undefined".equalsIgnoreCase(getString(status.value));
    }

    private boolean isDoorClosed(@Nullable FPStatusStringValue status) {
        return status == null || "Closed".equalsIgnoreCase(getString(status.value))
                || "Undefined".equalsIgnoreCase(getString(status.value));
    }

    private boolean updateTireStatus(FPVehicleStatus data) {
        boolean updated = false;
        FPTpms status = data.tpms;
        if (status == null
                || !"Systm_Activ_Composite_Stat".equalsIgnoreCase(getString(status.tirePressureSystemStatus.value))) {
            return false;
        }

        updated |= updateChannel(CHANNEL_TIREP_FRONTLEFT, getOnOff(isTireOk(status.leftFrontTireStatus)));
        updated |= updateChannel(CHANNEL_TIREP_FRONTRIGHT, getOnOff(isTireOk(status.rightFrontTireStatus)));
        updated |= updateChannel(CHANNEL_TIREP_REARLEFT, getOnOff(isTireOk(status.outerLeftRearTirePressure)));
        updated |= updateChannel(CHANNEL_TIREP_REARRIGHT, getOnOff(isTireOk(status.outerRightRearTireStatus)));
        updated |= updateChannel(CHANNEL_TIREP_INNERREARLEFT, getOnOff(isTireOk(status.innerLeftRearTireStatus)));
        updated |= updateChannel(CHANNEL_TIREP_INNERREARRIGHT, getOnOff(isTireOk(status.innerRightRearTireStatus)));
        updated |= updateChannel(CHANNEL_STATUS_TIRESOK,
                getOnOff("STATUS_GOOD".equalsIgnoreCase(data.tirePressure.value)));
        return updated;
    }

    private boolean isTireOk(@Nullable FPStatusStringValue status) {
        return status == null || "Normal".equalsIgnoreCase(getString(status.value))
                || "Not_Supported".equalsIgnoreCase(getString(status.value));
    }
}
