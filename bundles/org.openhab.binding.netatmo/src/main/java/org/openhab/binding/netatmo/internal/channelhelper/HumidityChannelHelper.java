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
import static org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;
import static org.openhab.binding.netatmo.internal.utils.WeatherUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link HumidityChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HumidityChannelHelper extends AbstractChannelHelper {

    public HumidityChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_HUMIDITY);
    }

    @Override
    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
        return CHANNEL_VALUE.equals(channelId) ? toQuantityType(dashboard.getHumidity(), HUMIDITY_UNIT)
                : getDerived(dashboard.getTemperature(), dashboard.getHumidity(), channelId);
    }

    protected @Nullable State getDerived(float temperature, float humidity, String channelId) {
        double humidex = getHumidex(temperature, humidity);
        switch (channelId) {
            case CHANNEL_HUMIDEX:
                return new DecimalType(humidex);
            case CHANNEL_HUMIDEX_SCALE:
                return new DecimalType(humidexScale(humidex));
            case CHANNEL_HEAT_INDEX:
                return toQuantityType(getHeatIndex(temperature, humidity), TEMPERATURE_UNIT);
            case CHANNEL_DEWPOINT:
                return toQuantityType(getDewPoint(temperature, humidity), TEMPERATURE_UNIT);
            case CHANNEL_DEWPOINT_DEP:
                double dewPoint = getDewPoint(temperature, humidity);
                return toQuantityType(getDewPointDep(temperature, dewPoint), TEMPERATURE_UNIT);
        }
        return null;
    }
}
