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
import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.PRESSURE_UNIT;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link PressureChannelHelper} handle specific behavior
 * of modules measuring pressure
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PressureChannelHelper extends AbstractChannelHelper {

    public PressureChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_PRESSURE);
    }

    @Override
    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
        switch (channelId) {
            case CHANNEL_VALUE:
                return toQuantityType(dashboard.getPressure(), PRESSURE_UNIT);
            case CHANNEL_TREND:
                return toStringType(dashboard.getPressureTrend());
            case CHANNEL_ABSOLUTE_PRESSURE:
                return toQuantityType(dashboard.getAbsolutePressure(), PRESSURE_UNIT);
        }
        return null;
    }
}
