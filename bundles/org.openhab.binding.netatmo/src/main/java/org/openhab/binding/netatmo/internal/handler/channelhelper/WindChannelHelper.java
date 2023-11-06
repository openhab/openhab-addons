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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.core.types.State;

/**
 * The {@link WindChannelHelper} handle specifics channels of modules measuring wind
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class WindChannelHelper extends ChannelHelper {

    public WindChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        switch (channelId) {
            case CHANNEL_WIND_ANGLE:
                return toQuantityType(dashboard.getWindAngle(), MeasureClass.WIND_ANGLE);
            case CHANNEL_WIND_STRENGTH:
                return toQuantityType(dashboard.getWindStrength(), MeasureClass.WIND_SPEED);
            case CHANNEL_GUST_ANGLE:
                return toQuantityType(dashboard.getGustAngle(), MeasureClass.WIND_ANGLE);
            case CHANNEL_GUST_STRENGTH:
                return toQuantityType(dashboard.getGustStrength(), MeasureClass.WIND_SPEED);
            case CHANNEL_MAX_WIND_STRENGTH:
                return toQuantityType(dashboard.getMaxWindStr(), MeasureClass.WIND_SPEED);
            case CHANNEL_DATE_MAX_WIND_STRENGTH:
                return toDateTimeType(dashboard.getDateMaxWindStr());
        }
        return null;
    }
}
