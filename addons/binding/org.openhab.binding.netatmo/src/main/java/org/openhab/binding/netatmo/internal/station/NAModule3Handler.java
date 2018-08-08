/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.station;

import io.rudolph.netatmo.api.aircare.model.DashboardData;
import io.rudolph.netatmo.api.common.model.ClimateModule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toDateTimeType;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toQuantityType;

/**
 * {@link NAModule3Handler} is the class used to handle the Rain Gauge
 * capable of measuring precipitation
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule3Handler extends NetatmoModuleHandler<ClimateModule> {

    public NAModule3Handler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(ClimateModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType().getValue());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            DashboardData dashboardData = module.getDashboardData();
            switch (channelId) {
                case CHANNEL_RAIN:
                    return toQuantityType(dashboardData.getRain(), API_RAIN_UNIT);
                case CHANNEL_SUM_RAIN1:
                    return toQuantityType(dashboardData.getSumRain1(), API_RAIN_UNIT);
                case CHANNEL_SUM_RAIN24:
                    return toQuantityType(dashboardData.getSumRain24(), API_RAIN_UNIT);
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(dashboardData.getTimeUtc());
            }
        }
        return super.getNAThingProperty(channelId);
    }

}
