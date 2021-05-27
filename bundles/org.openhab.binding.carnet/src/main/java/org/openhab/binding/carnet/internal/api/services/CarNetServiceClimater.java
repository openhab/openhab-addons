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
package org.openhab.binding.carnet.internal.api.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION;

import java.math.BigDecimal;
import java.util.Map;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus.CNClimaterStatus.CarNetClimaterStatusData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus.CNClimaterStatus.CarNetClimaterStatusData.CNClimaterElementState.CarNetClimaterZoneStateList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus.CNClimaterStatus.CarNetClimaterStatusData.CarNetClimaterZoneState;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceClimater} implements climater & preheat service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceClimater extends CarNetBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceClimater.class);

    public CarNetServiceClimater(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws CarNetException {
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_CLIMATER, ITEMT_SWITCH, null, false, false);
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CLIMATER_TARGET_TEMP, ITEMT_TEMP, SIUnits.CELSIUS, false, false);
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CLIMATER_HEAT_SOURCE, ITEMT_STRING, null, true, false);
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_WINHEAT, ITEMT_SWITCH, null, false, false);
        addChannel(ch, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_HEATSOURCE, ITEMT_STRING, null, true, false);

        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_GEN_STATE, ITEMT_STRING, null, false, true);
        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_FL_STATE, ITEMT_SWITCH, null, true, true);
        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_FR_STATE, ITEMT_SWITCH, null, true, true);
        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_RL_STATE, ITEMT_SWITCH, null, true, true);
        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_RR_STATE, ITEMT_SWITCH, null, true, true);
        addChannel(ch, CHANNEL_GROUP_CLIMATER, CHANNEL_CLIMATER_MIRROR_HEAT, ITEMT_SWITCH, null, false, true);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        boolean updated = false;
        try {
            CarNetClimaterStatus cs = api.getClimaterStatus();
            String group = CHANNEL_GROUP_CLIMATER;
            if (cs.settings != null) {
                if (cs.settings.heaterSource != null) {
                    // convert temp from dK to C
                    Double temp = getDouble(cs.settings.targetTemperature.content).doubleValue();
                    BigDecimal bd = new BigDecimal(
                            DKELVIN.getConverterToAny(SIUnits.CELSIUS).convert(temp).doubleValue() + 0.1);
                    updated |= updateChannel(group, CHANNEL_CLIMATER_TARGET_TEMP,
                            toQuantityType(bd.doubleValue(), 1, SIUnits.CELSIUS));
                    if (cs.settings.heaterSource != null) {
                        updated |= updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CLIMATER_HEAT_SOURCE,
                                getStringType(cs.settings.heaterSource.content));
                    }
                }
            }

            if (cs.status != null) {
                if (cs.status.climatisationStatusData != null) {
                    CarNetClimaterStatusData sd = cs.status.climatisationStatusData;
                    if (sd.climatisationState != null) {
                        updated |= updateChannel(group, CHANNEL_CLIMATER_GEN_STATE,
                                getStringType(sd.climatisationState.content));
                        updated |= updateChannel(group, CHANNEL_CONTROL_CLIMATER,
                                getOnOff(sd.climatisationState.content));
                    }
                    if (sd.climatisationElementStates != null) {
                        updateZoneStates(sd.climatisationElementStates.zoneStates);
                        if (sd.climatisationElementStates.isMirrorHeatingActive != null) {
                            updated |= updateChannel(group, CHANNEL_CLIMATER_MIRROR_HEAT,
                                    getOnOff(sd.climatisationElementStates.isMirrorHeatingActive.content));
                        }
                    }
                }
                if (cs.status.vehicleParkingClockStatusData != null) {
                    logger.debug("{}: vehicleParkingClock = {}", thingId,
                            getString(cs.status.vehicleParkingClockStatusData.vehicleParkingClock.content));
                }
            }

            String timer = api.getClimaterTimer();

        } catch (IncommensurableException e) {
            logger.debug("IncommensurableException ignored");
        }
        return updated;
    }

    private boolean updateZoneStates(@Nullable CarNetClimaterZoneStateList zoneList) {
        if (zoneList != null) {
            for (CarNetClimaterZoneState zs : zoneList.zoneState) {
                updateChannel(CHANNEL_GROUP_CLIMATER, getString(zs.value.position), getOnOff(zs.value.isActive));
            }
            return true;
        }
        return false;
    }
}
