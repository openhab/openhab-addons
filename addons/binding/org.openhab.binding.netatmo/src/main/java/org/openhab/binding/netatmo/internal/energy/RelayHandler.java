/**
  Copyright (c) 2010-2018 by the respective copyright holders.
  <p>
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.energy;

import io.rudolph.netatmo.api.common.model.Module;
import io.rudolph.netatmo.api.energy.model.HomeStatusBody;
import io.rudolph.netatmo.api.energy.model.module.RelayModule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;

import java.time.ZoneOffset;
import java.util.List;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.CHANNEL_CONNECTED_BOILER;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;

/**
 * {@link RelayHandler} is the class used to handle the plug
 * device of a energy set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 */
public class RelayHandler extends NetatmoDeviceHandler<RelayModule> {

    public RelayHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected RelayModule updateReadings() {
        final HomeStatusBody thermostatDataBody = getBridgeHandler().api
                .getEnergyApi()
                .getHomeStatus(getId(), null)
                .executeSync();

        if (thermostatDataBody == null
                || thermostatDataBody.getHome() == null
                || thermostatDataBody.getHome().get(0) == null) {
            return null;
        }

        List<Module> modules = thermostatDataBody.getHome()
                .get(0)
                .getModules();

        if (modules == null) {
            return null;
        }

        RelayModule result = null;

        for (Module module : modules) {

            if (getId().equalsIgnoreCase(getId()) && module instanceof RelayModule) {
                result = (RelayModule) module;
            } else {
                childs.put(module.getId(), module);
            }
        }
        return result;
    }

    @Override
    protected void updateProperties(RelayModule relayModule) {
        updateProperties(null, relayModule.getType().getValue());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_CONNECTED_BOILER:
                return device != null ? toOnOffType(device.getConnectedToBoiler()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }


    @Override
    protected @Nullable Long getDataTimestamp() {
        if (device != null && device.getSetupDate()!= null) {
            return device.getSetupDate().toEpochSecond(ZoneOffset.UTC);
        }
        return null;
    }

}
