/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.core.types.State;

/**
 * The {@link RainChannelHelper} handles specific channels of modules measuring rain
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RainChannelHelper extends ChannelHelper {

    public RainChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        switch (channelId) {
            case CHANNEL_VALUE:
                return toQuantityType(dashboard.getRain(), MeasureClass.RAIN_INTENSITY);
            case CHANNEL_SUM_RAIN1:
                return toQuantityType(dashboard.getSumRain1(), MeasureClass.RAIN_QUANTITY);
            case CHANNEL_SUM_RAIN24:
                return toQuantityType(dashboard.getSumRain24(), MeasureClass.RAIN_QUANTITY);
        }
        return null;
    }
}
