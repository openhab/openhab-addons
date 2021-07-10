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
package org.openhab.binding.carnet.internal.api.weconnect;

import static org.openhab.binding.carnet.internal.BindingConstants.*;
import static org.openhab.binding.carnet.internal.CarUtils.*;
import static org.openhab.binding.carnet.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_VEHICLE_STATUS_REPORT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.ApiBase;
import org.openhab.binding.carnet.internal.api.ApiBaseService;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.openhab.binding.carnet.internal.handler.VehicleBaseHandler;
import org.openhab.binding.carnet.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WCServiceStatus} implements the Status Servide for WeConnect.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WCServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(WCServiceStatus.class);

    public WCServiceStatus(VehicleBaseHandler thingHandler, ApiBase api) {
        super(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // Try to query status information from vehicle
        WCVehicleStatus status = api.getVehicleStatus().wcStatus;

        addChannels(channels, true, /* CHANNEL_STATUS_PBRAKE, CHANNEL_STATUS_LIGHTS, */ CHANNEL_STATUS_ERROR);
        addChannels(channels, status.rangeStatus != null, CHANNEL_RANGE_TOTAL, CHANNEL_RANGE_PRANGE,
                CHANNEL_RANGE_PFUELTYPE);
        addChannels(channels, status.batteryStatus != null, CHANNEL_CHARGER_CHGLVL);
        addChannels(channels, status.chargingStatus != null, CHANNEL_CONTROL_CHARGER, CHANNEL_CHARGER_CHG_STATE,
                CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_REMAINING, CHANNEL_CONTROL_MAXCURRENT, CHANNEL_CONTROL_TARGETCHG,
                CHANNEL_CHARGER_KMPH);
        addChannels(channels, status.plugStatus != null, CHANNEL_CHARGER_PLUG_STATE, CHANNEL_CHARGER_LOCK_STATE);
        addChannels(channels, status.climatisationStatus != null, CHANNEL_CLIMATER_GEN_STATE,
                CHANNEL_CLIMATER_REMAINING);
        addChannels(channels, status.climatisationSettings != null, CHANNEL_CONTROL_CLIMATER,
                CHANNEL_CLIMATER_TARGET_TEMP);
        addChannels(channels, status.climatisationTimer != null, CHANNEL_GENERAL_TIMEINCAR);
        addChannels(channels, status.windowHeatingStatus != null, CHANNEL_CONTROL_WINHEAT);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean updated = false;

        WCVehicleStatus status = api.getVehicleStatus().wcStatus;
        if (status != null) {
            logger.debug("{}: Vehicle Status:\n{}", thingId, status);

            String group = CHANNEL_GROUP_STATUS;
            updated |= updateChannel(group, CHANNEL_STATUS_ERROR, getStringType(status.error));

            updated |= updateRange(status);
            updated |= updateChargingStatus(status);
            updated |= updateClimatisationStatus(status);
            updated |= updateWindowHeatStatus(status);
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
            updated |= updateChannel(group, CHANNEL_RANGE_PFUELTYPE,
                    new DecimalType("electric".equals(getString(status.rangeStatus.primaryEngine.type)) ? 3 : 0));
        }
        return updated;
    }

    private boolean updateChargingStatus(WCVehicleStatus status) {
        boolean updated = false;
        String group = CHANNEL_GROUP_CHARGER;
        if (status.chargingStatus != null) {
            updated |= updateChannel(group, CHANNEL_CONTROL_CHARGER,
                    "charging".equals(getString(status.chargingStatus.chargingState)) ? OnOffType.ON : OnOffType.OFF);
            updated |= updateChannel(group, CHANNEL_CHARGER_CHG_STATE,
                    getStringType(status.chargingStatus.chargingState));
            updated |= updateChannel(group, CHANNEL_CHARGER_MODE, getStringType(status.chargingStatus.chargeMode));
            updated |= updateChannel(group, CHANNEL_CHARGER_REMAINING, toQuantityType(
                    getInteger(status.chargingStatus.remainingChargingTimeToComplete_min), 0, Units.MINUTE));
            updated |= updateChannel(group, CHANNEL_CHARGER_KMPH, getDecimal(status.chargingStatus.chargeRate_kmph));
            updated |= updateChannel(group, CHANNEL_CONTROL_TARGETCHG,
                    toQuantityType(getInteger(status.chargingSettings.targetSOC_pct), 0, PERCENT));
            String maxCurrent = getString(status.chargingSettings.maxChargeCurrentAC);
            if ("maximum".equalsIgnoreCase(maxCurrent)) {
                maxCurrent = "255";
            }
            if (Character.isDigit(maxCurrent.charAt(0))) {
                updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MAXCURRENT,
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
                    "locked".equals(getString(status.plugStatus.plugLockState)) ? OnOffType.ON : OnOffType.OFF);
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
            updated |= updateChannel(group, CHANNEL_CLIMATER_TARGET_TEMP,
                    toQuantityType(getDouble(status.climatisationSettings.targetTemperature_C), 0, SIUnits.CELSIUS));
        }
        if (status.climatisationTimer != null) {
            updated |= updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_TIMEINCAR,
                    getDateTime(status.climatisationTimer.timeInCar));
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
}
