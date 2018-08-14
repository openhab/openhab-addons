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
import io.rudolph.netatmo.api.energy.EnergyConnector;
import io.rudolph.netatmo.api.energy.model.module.ValveModule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;

/**
 * {@link ValveHandler} is the class used to handle the energy
 * module of a energy set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 */
public class ValveHandler extends NetatmoModuleHandler<ValveModule> {
    private final Logger logger = LoggerFactory.getLogger(ValveHandler.class);


    public ValveHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(ValveModule moduleData) {
        ValveModule module = getBridgeHandler().api.getEnergyApi().getModuleStatus(getParentId(), moduleData);


        updateProperties(moduleData.getFirmware(), moduleData.getType().getValue());
    }

    @Override
    protected void updateChannels(Object module) {
        logger.debug("object null: " + module );
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_REACHABLE:
                return toOnOffType(module.isReachable());
        }

        return super.getNAThingProperty(channelId);
    }

}
