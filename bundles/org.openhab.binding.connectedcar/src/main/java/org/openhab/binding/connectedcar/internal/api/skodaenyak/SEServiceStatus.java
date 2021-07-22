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
package org.openhab.binding.connectedcar.internal.api.skodaenyak;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_VEHICLE_STATUS_REPORT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleSettings.SEChargerSettings;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleSettings.SEClimaterSettings;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEChargerStatus;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEClimaterStatus;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleStatusData.SEVehicleStatus.SEClimaterStatus.SEHeatingStatus;
import org.openhab.binding.connectedcar.internal.handler.VehicleBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SEServiceStatus} implements the Status Service for Skoda Enyak.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class SEServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(SEServiceStatus.class);

    public SEServiceStatus(VehicleBaseHandler thingHandler, ApiBase api) {
        super(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // Try to query status information from vehicle
        // SEVehicleStatusData status = api.getVehicleStatus().seStatus;
        // addChannels(channels, true, CHANNEL_STATUS_PBRAKE, CHANNEL_STATUS_LIGHTS, CHANNEL_STATUS_ERROR);
        // addChannels(channels, status.rangeStatus != null, CHANNEL_RANGE_TOTAL, CHANNEL_RANGE_PRANGE,
        // CHANNEL_RANGE_PFUELTYPE);
        // addChannels(channels, status.charger != null, CHANNEL_CONTROL_CHARGER, CHANNEL_CHARGER_CHG_STATE,
        // CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_REMAINING, CHANNEL_CONTROL_MAXCURRENT, CHANNEL_CONTROL_TARGETCHG,
        // CHANNEL_CHARGER_KMPH);
        // addChannels(channels, status.climatisationTimer != null, CHANNEL_GENERAL_TIMEINCAR);
        addChannels(channels, true, CHANNEL_CONTROL_CHARGER, CHANNEL_CHARGER_CHG_STATE, CHANNEL_CHARGER_MODE,
                CHANNEL_CONTROL_TARGETCHG, CHANNEL_CHARGER_CHGLVL, CHANNEL_CHARGER_REMAINING, CHANNEL_CHARGER_RATE,
                CHANNEL_RANGE_TOTAL, CHANNEL_CHARGER_PLUG_STATE, CHANNEL_CHARGER_LOCK_STATE);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MAXCURRENT, ITEMT_STRING, null, false, true);

        addChannels(channels, true, CHANNEL_CLIMATER_GEN_STATE, CHANNEL_CLIMATER_REMAINING);
        addChannels(channels, true, CHANNEL_CONTROL_CLIMATER, CHANNEL_CLIMATER_TARGET_TEMP);
        addChannels(channels, true, CHANNEL_CONTROL_WINHEAT);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean updated = false;

        SEVehicleStatusData status = api.getVehicleStatus().seStatus;
        if (status != null) {
            updated |= updateRangeStatus(status);
            updated |= updateChargingStatus(status);
            updated |= updateClimatisationStatus(status);
            updated |= updateWindowHeatStatus(status);
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
        if (data.status.charger != null) {
            SEChargerStatus s = data.status.charger;
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

        if (data.settings.charger != null) {
            SEChargerSettings s = data.settings.charger;
            updated |= updateChannel(group, CHANNEL_CONTROL_TARGETCHG,
                    toQuantityType(getInteger(s.targetStateOfChargeInPercent), 0, PERCENT));
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MAXCURRENT,
                    getStringType(s.maxChargeCurrentAc));
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
            updated |= updateChannel(group, CHANNEL_CLIMATER_TARGET_TEMP, toQuantityType(tempC, 1, SIUnits.CELSIUS));
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
}
