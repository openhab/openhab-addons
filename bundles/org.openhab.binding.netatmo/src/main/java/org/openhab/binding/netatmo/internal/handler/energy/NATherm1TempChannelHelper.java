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
package org.openhab.binding.netatmo.internal.handler.energy;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.TEMPERATURE_UNIT;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.energy.NAThermMeasure;
import org.openhab.binding.netatmo.internal.api.energy.NAThermostat;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link NATherm1TempChannelHelper} handle specific behavior
 * of the thermostat module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NATherm1TempChannelHelper extends AbstractChannelHelper {

    public NATherm1TempChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_TH_TEMPERATURE);
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NAThermostat thermostat = (NAThermostat) naThing;
        NAThermMeasure measured = thermostat.getMeasured();
        if (measured != null && CHANNEL_VALUE.equals(channelId)) {
            return toQuantityType(measured.getTemperature(), TEMPERATURE_UNIT);
        } else if (measured != null && CHANNEL_TIMEUTC.equals(channelId)) {
            return toDateTimeType(measured.getTime(), zoneId);
        }
        return null;
    }
}
