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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;
import static org.openhab.binding.netatmo.internal.utils.WeatherUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link HumidityChannelHelper} handles specific channels of modules returning humidity measures.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HumidityChannelHelper extends ChannelHelper {

    public HumidityChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        return switch (channelId) {
            case CHANNEL_HUMIDEX -> new DecimalType(humidex(dashboard.getTemperature(), dashboard.getHumidity()));
            case CHANNEL_HUMIDEX_SCALE ->
                new DecimalType(humidexScale(humidex(dashboard.getTemperature(), dashboard.getHumidity())));
            case CHANNEL_VALUE -> toQuantityType(dashboard.getHumidity(), MeasureClass.HUMIDITY);
            default -> null;
        };
    }
}
