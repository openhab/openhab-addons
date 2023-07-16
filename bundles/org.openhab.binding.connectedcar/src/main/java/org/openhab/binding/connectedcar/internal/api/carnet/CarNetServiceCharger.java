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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_SERVICE_REMOTE_BATTERY_CHARGE;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus.CNChargerStatus.CarNetChargerStatusData;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link CarNetServiceCharger} implements the charger service.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceCharger extends ApiBaseService {
    public CarNetServiceCharger(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_BATTERY_CHARGE, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) {
        addChannels(ch, CHANNEL_GROUP_CHARGER, true, CHANNEL_CHARGER_ERROR, CHANNEL_CONTROL_CHARGER,
                CHANNEL_CHARGER_MAXCURRENT, CHANNEL_CHARGER_PWR_STATE, CHANNEL_CHARGER_CHG_STATE, CHANNEL_CHARGER_FLOW,
                CHANNEL_CHARGER_BAT_STATE, CHANNEL_CHARGER_REMAINING, CHANNEL_CHARGER_PLUG_STATE,
                CHANNEL_CHARGER_LOCK_STATE, CHANNEL_CHARGER_STATUS);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = false;
        CarNetChargerStatus cs = ((CarNetApi) api).getChargerStatus();
        if ((cs.status == null) || (cs.status.chargingStatusData == null)) {
            return false;
        }
        CarNetChargerStatusData sd = cs.status.chargingStatusData;
        if (sd != null) {
            State current = cs.settings != null ? getDecimal(cs.settings.maxChargeCurrent.content) : UnDefType.UNDEF;
            updated |= updateChannel(CHANNEL_CHARGER_POWER, current);
            if (sd.chargingState != null) {
                updated |= updateChannel(CHANNEL_CONTROL_CHARGER, getOnOffType(sd.chargingState.content));
                updated |= updateChannel(CHANNEL_CHARGER_CHG_STATE, getStringType(sd.chargingState.content));
                updated |= updateChannel(CHANNEL_CHARGER_STATUS, getStringType(sd.chargingState.content));
            }
            if (sd.chargingStateErrorCode != null) {
                updated |= updateChannel(CHANNEL_CHARGER_ERROR, getDecimal(sd.chargingStateErrorCode.content));
            }
            if (sd.externalPowerSupplyState != null) {
                updated |= updateChannel(CHANNEL_CHARGER_PWR_STATE, getStringType(sd.externalPowerSupplyState.content));
            }
            if (sd.energyFlow != null) {
                updated |= updateChannel(CHANNEL_CHARGER_FLOW, getStringType(sd.energyFlow.content));
            }
            if (cs.status.batteryStatusData != null) {
                updated |= updateChannel(CHANNEL_CHARGER_BAT_STATE,
                        getDecimal(cs.status.batteryStatusData.stateOfCharge.content));
                if (cs.status.batteryStatusData.remainingChargingTime != null) {
                    int remaining = getDecimal(cs.status.batteryStatusData.remainingChargingTime.content).intValue();
                    updated |= updateChannel(CHANNEL_CHARGER_REMAINING, remaining == 65535 ? UnDefType.UNDEF
                            : getDecimal(cs.status.batteryStatusData.remainingChargingTime.content));
                }
            }
            if (cs.status.plugStatusData != null) {
                updated |= updateChannel(CHANNEL_CHARGER_LOCK_STATE,
                        getOnOff("locked".equals(getString(cs.status.plugStatusData.lockState.content))));
                updated |= updateChannel(CHANNEL_CHARGER_PLUG_STATE,
                        getStringType(cs.status.plugStatusData.plugState.content));
            }
        }
        return updated;
    }
}
