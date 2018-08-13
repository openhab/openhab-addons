/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.energy;

import io.rudolph.netatmo.api.common.model.MeasureRequestResponse;
import io.rudolph.netatmo.api.common.model.Scale;
import io.rudolph.netatmo.api.common.model.ScaleType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        updateProperties(moduleData.getFirmwareRevision(), moduleData.getType().getValue());
    }

    @Override
    public void updateChannels(Object moduleObject) {
        if (isRefreshRequired()) {

            EnergyConnector energyApi = getBridgeHandler().api.getEnergyApi();
            List<MeasureRequestResponse> measures = energyApi.getMeasure(getId(),
                    getParentId(),
                    Scale.MAX,
                    ScaleType.TEMPERATURE,
                    null,
                    null,
                    null,
                    true,
                    true).executeSync();

            measurableChannels.setMeasures(measures);
        }
        setRefreshRequired(false);

        super.updateChannels(moduleObject);

        if (module != null) {
            updateStateDescription(module);
        }
    }


    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        //TODO: Add Valve status updates
        super.updateStatus(status, statusDetail, description);
    }

    private void updateStateDescription(ValveModule valve) {
        updateProperties(valve);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        return super.getNAThingProperty(channelId);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }
}
