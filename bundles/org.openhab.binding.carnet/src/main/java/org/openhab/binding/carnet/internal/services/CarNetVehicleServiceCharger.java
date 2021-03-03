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
package org.openhab.binding.carnet.internal.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_REMOTE_BATTERY_CHARGE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApi;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus.CNChargerStatus.CarNetChargerStatusData;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * {@link CarNetVehicleServiceCharger} implements the charger service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetVehicleServiceCharger extends CarNetVehicleBaseService {
    public CarNetVehicleServiceCharger(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(thingHandler, api);
        serviceId = CNAPI_SERVICE_REMOTE_BATTERY_CHARGE;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) {
        try {
            api.getChargerStatus();
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_STATUS, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_PWR_STATE, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_CHG_STATE, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_FLOW, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_BAT_STATE, ITEMT_PERCENT, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_REMAINING, ITEMT_TIME, QMINUTES, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_PLUG_STATE, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_LOCK_STATE, ITEMT_STRING, null, false, true);
            addChannel(ch, CHANNEL_GROUP_CHARGER, CHANNEL_CHARGER_ERROR, ITEMT_NUMBER, null, false, true);
            return true;
        } catch (CarNetException e) {
        }
        return false;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        try {
            CarNetChargerStatus cs = api.getChargerStatus();
            if ((cs.status == null) || (cs.status.chargingStatusData == null)) {
                return false;
            }
            CarNetChargerStatusData sd = cs.status.chargingStatusData;
            if (sd != null) {
                String group = CHANNEL_GROUP_CHARGER;
                updateChannel(group, CHANNEL_CHARGER_CURRENT,
                        cs.settings != null ? getDecimal(cs.settings.maxChargeCurrent.content) : UnDefType.UNDEF,
                        Units.AMPERE);
                if (sd.chargingState != null) {
                    updateChannel(group, CHANNEL_CHARGER_CHG_STATE, getStringType(sd.chargingState.content));
                    updateChannel(group, CHANNEL_CHARGER_STATUS, getStringType(sd.chargingState.content));
                }
                if (sd.chargingStateErrorCode != null) {
                    updateChannel(group, CHANNEL_CHARGER_ERROR, getDecimal(sd.chargingStateErrorCode.content));
                }
                if (sd.externalPowerSupplyState != null) {
                    updateChannel(group, CHANNEL_CHARGER_PWR_STATE, getStringType(sd.externalPowerSupplyState.content));
                }
                if (sd.energyFlow != null) {
                    updateChannel(group, CHANNEL_CHARGER_FLOW, getStringType(sd.energyFlow.content));
                }
                if (cs.status.batteryStatusData != null) {
                    updateChannel(group, CHANNEL_CHARGER_BAT_STATE,
                            new QuantityType<>(getInteger(cs.status.batteryStatusData.stateOfCharge.content), PERCENT));
                    if (cs.status.batteryStatusData.remainingChargingTime != null) {
                        int remaining = getDecimal(cs.status.batteryStatusData.remainingChargingTime.content)
                                .intValue();
                        updateChannel(group, CHANNEL_CHARGER_REMAINING,
                                remaining == 65535 ? UnDefType.UNDEF
                                        : new QuantityType<>(
                                                getDecimal(cs.status.batteryStatusData.remainingChargingTime.content),
                                                QMINUTES));
                    }
                }
                if (cs.status.plugStatusData != null) {
                    updateChannel(group, CHANNEL_CHARGER_PLUG_STATE,
                            getStringType(cs.status.plugStatusData.plugState.content));
                    updateChannel(group, CHANNEL_CHARGER_LOCK_STATE,
                            getStringType(cs.status.plugStatusData.lockState.content));
                }
                return true;
            }
        } catch (CarNetException e) {

        }
        return false;
    }
}
