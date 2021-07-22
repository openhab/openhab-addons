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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_REMOTE_BATTERY_CHARGE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus.CNChargerStatus.CarNetChargerStatusData;
import org.openhab.binding.connectedcar.internal.handler.VehicleCarNetHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link CarNetServiceCharger} implements the charger service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceCharger extends ApiBaseService {
    public CarNetServiceCharger(VehicleCarNetHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_BATTERY_CHARGE, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) {
        // Control channels
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CHARGER, ITEMT_SWITCH, null, false, false);
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MAXCURRENT, ITEMT_AMP, null, false, true);

        // Status channels
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
            String group = CHANNEL_GROUP_CHARGER;
            State current = cs.settings != null ? getDecimal(cs.settings.maxChargeCurrent.content) : UnDefType.UNDEF;
            updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MAXCURRENT, current, Units.AMPERE);
            if (sd.chargingState != null) {
                updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CHARGER,
                        getOnOffType(sd.chargingState.content));
                updated |= updateChannel(group, CHANNEL_CHARGER_CHG_STATE, getStringType(sd.chargingState.content));
                updated |= updateChannel(group, CHANNEL_CHARGER_STATUS, getStringType(sd.chargingState.content));
            }
            if (sd.chargingStateErrorCode != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_ERROR, getDecimal(sd.chargingStateErrorCode.content));
            }
            if (sd.externalPowerSupplyState != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_PWR_STATE,
                        getStringType(sd.externalPowerSupplyState.content));
            }
            if (sd.energyFlow != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_FLOW, getStringType(sd.energyFlow.content));
            }
            if (cs.status.batteryStatusData != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_BAT_STATE,
                        new QuantityType<>(getInteger(cs.status.batteryStatusData.stateOfCharge.content), PERCENT));
                if (cs.status.batteryStatusData.remainingChargingTime != null) {
                    int remaining = getDecimal(cs.status.batteryStatusData.remainingChargingTime.content).intValue();
                    updated |= updateChannel(group, CHANNEL_CHARGER_REMAINING,
                            remaining == 65535 ? UnDefType.UNDEF
                                    : new QuantityType<>(
                                            getDecimal(cs.status.batteryStatusData.remainingChargingTime.content),
                                            QMINUTES));
                }
            }
            if (cs.status.plugStatusData != null) {
                updated |= updateChannel(group, CHANNEL_CHARGER_PLUG_STATE,
                        getStringType(cs.status.plugStatusData.plugState.content));
                updated |= updateChannel(group, CHANNEL_CHARGER_LOCK_STATE,
                        getStringType(cs.status.plugStatusData.lockState.content));
            }
        }
        return updated;
    }
}
