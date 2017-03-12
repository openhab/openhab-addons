/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NADashboardData;

/**
 * {@link NAModule3Handler} is the class used to handle the Rain Gauge
 * capable of measuring precipitation
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule3Handler extends NetatmoModuleHandler<NetatmoModuleConfiguration> {

    public NAModule3Handler(Thing thing) {
        super(thing, NetatmoModuleConfiguration.class);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            NADashboardData dashboardData = module.getDashboardData();
            switch (channelId) {
                case CHANNEL_RAIN:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getRain());
                case CHANNEL_SUM_RAIN1:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getSumRain24());
                case CHANNEL_SUM_RAIN24:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getSumRain24());

            }
        }
        return super.getNAThingProperty(channelId);
    }

}
