/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.energy;

import io.rudolph.netatmo.api.common.model.*;
import io.rudolph.netatmo.api.energy.model.module.ValveModule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_REACHABLE;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toQuantityType;

/**
 * {@link ValveHandler} is the class used to handle the energy
 * module of a energy set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 */
public class ValveHandler extends NetatmoModuleHandler<ValveModule> {
    private final Logger logger = LoggerFactory.getLogger(ValveHandler.class);

    private float temperature = Float.MIN_VALUE;

    public ValveHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(ValveModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType().getValue());
    }

    @Override
    protected void updateChannels(Object module) {
        if (!isRefreshRequired()
                || !(module instanceof ValveModule)
                || ((ValveModule) module).getType() != DeviceType.VALVE) {
            return;
        }

        String homeId = getBridgeHandler().findNAThing(getParentId()).get().getParentId();

        Module tModule = getBridgeHandler().api
                .getEnergyApi()
                .getModuleDataById(homeId, getId());

        if (tModule.getType() != DeviceType.VALVE || !(tModule instanceof ValveModule)) {
            return;
        }
        super.updateChannels(tModule);

        List<MeasureRequestResponse> response = getBridgeHandler().api
                .getEnergyApi()
                .getMeasure(getId(),
                        this.module.getBridgeId(),
                        Scale.HALFHOUR,
                        ScaleType.TEMPERATURE,
                        null,
                        null,
                        null,
                        null,
                        null)
                .executeSync();

        if (response == null || response.size() == 0) {
            return;
        }


        temperature = response.get(0).getValue().get(0).get(0);

        setRefreshRequired(false);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_REACHABLE:
                return toOnOffType(module.isReachable());

            case CHANNEL_TEMPERATURE:
                return toQuantityType(temperature, API_TEMPERATURE_UNIT);
        }

        return super.getNAThingProperty(channelId);
    }

}
