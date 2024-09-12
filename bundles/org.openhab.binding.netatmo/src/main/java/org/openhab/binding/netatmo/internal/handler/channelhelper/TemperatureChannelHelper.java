/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.utils.WeatherUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.core.types.State;

/**
 * The {@link TemperatureChannelHelper} handles channels of modules measuring temperature
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class TemperatureChannelHelper extends ChannelHelper {

    public TemperatureChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        switch (channelId) {
            case CHANNEL_VALUE:
                return toQuantityType(dashboard.getTemperature(), MeasureClass.OUTSIDE_TEMPERATURE);
            case CHANNEL_MIN_VALUE:
                return toQuantityType(dashboard.getMinTemp(), MeasureClass.OUTSIDE_TEMPERATURE);
            case CHANNEL_MAX_VALUE:
                return toQuantityType(dashboard.getMaxTemp(), MeasureClass.OUTSIDE_TEMPERATURE);
            case CHANNEL_MIN_TIME:
                return toDateTimeType(dashboard.getDateMinTemp());
            case CHANNEL_MAX_TIME:
                return toDateTimeType(dashboard.getDateMaxTemp());
            case CHANNEL_HEAT_INDEX:
                return toQuantityType(heatIndex(dashboard.getTemperature(), dashboard.getHumidity()),
                        MeasureClass.HEAT_INDEX);
            case CHANNEL_DEWPOINT:
                return toQuantityType(dewPoint(dashboard.getTemperature(), dashboard.getHumidity()),
                        MeasureClass.OUTSIDE_TEMPERATURE);
            case CHANNEL_DEWPOINT_DEP:
                double dewPoint = dewPoint(dashboard.getTemperature(), dashboard.getHumidity());
                return toQuantityType(dewPointDep(dashboard.getTemperature(), dewPoint),
                        MeasureClass.OUTSIDE_TEMPERATURE);
            case CHANNEL_TREND:
                return toStringType(dashboard.getTempTrend());
        }
        return null;
    }
}
