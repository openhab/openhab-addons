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
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.math.BigDecimal;
import java.util.Map;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus.CNClimaterStatus.CarNetClimaterStatusData;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceClimater} implements climater & preheat service.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceClimater extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceClimater.class);

    public CarNetServiceClimater(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws ApiException {
        addChannels(ch, CHANNEL_GROUP_CLIMATER, true, CHANNEL_CONTROL_CLIMATER, CHANNEL_CONTROL_TARGET_TEMP,
                CHANNEL_CONTROL_WINHEAT, CHANNEL_CLIMATER_HEATSOURCE, CHANNEL_CLIMATER_GEN_STATE,
                CHANNEL_CLIMATER_MIRROR_HEAT);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = false;
        try {
            CarNetClimaterStatus cs = ((CarNetApi) api).getClimaterStatus();
            if (cs.settings != null) {
                if (cs.settings.heaterSource != null) {
                    // convert temp from dK to C
                    Double temp = getDouble(cs.settings.targetTemperature.content).doubleValue();
                    BigDecimal bd = new BigDecimal(
                            DKELVIN.getConverterToAny(SIUnits.CELSIUS).convert(temp).doubleValue() + 0.1);
                    updated |= updateChannel(CHANNEL_CONTROL_TARGET_TEMP,
                            toQuantityType(bd.doubleValue(), 1, SIUnits.CELSIUS));
                    if (cs.settings.heaterSource != null) {
                        updated |= updateChannel(CHANNEL_CLIMATER_HEATSOURCE,
                                getStringType(cs.settings.heaterSource.content));
                    }
                }
            }

            if (cs.status != null) {
                if (cs.status.climatisationStatusData != null) {
                    CarNetClimaterStatusData sd = cs.status.climatisationStatusData;
                    if (sd.climatisationState != null) {
                        updated |= updateChannel(CHANNEL_CONTROL_CLIMATER, getOnOffType(sd.climatisationState.content));
                        updated |= updateChannel(CHANNEL_CLIMATER_GEN_STATE,
                                getStringType(sd.climatisationState.content));
                    }
                    if (sd.climatisationElementStates != null) {
                        if (sd.climatisationElementStates.isMirrorHeatingActive != null) {
                            updated |= updateChannel(CHANNEL_CLIMATER_MIRROR_HEAT,
                                    getOnOff(sd.climatisationElementStates.isMirrorHeatingActive.content));
                        }
                    }
                }
                if (cs.status.vehicleParkingClockStatusData != null) {
                    logger.debug("{}: vehicleParkingClock = {}", thingId,
                            getString(cs.status.vehicleParkingClockStatusData.vehicleParkingClock.content));
                }
            }
        } catch (IncommensurableException e) {
            logger.debug("IncommensurableException ignored");
        }
        return updated;
    }
}
