/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link WindChannelHelper} handle specific behavior
 * of modules measuring wind
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class WindChannelHelper extends AbstractChannelHelper {

    public WindChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_WIND);
    }

    @Override
    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
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
                return toDateTimeType(dashboard.getDateMaxWindStr(), zoneId);
        }
        return null;
    }
}
