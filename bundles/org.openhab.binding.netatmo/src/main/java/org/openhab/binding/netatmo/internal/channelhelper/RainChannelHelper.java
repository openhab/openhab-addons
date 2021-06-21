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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.types.State;

/**
 * The {@link RainChannelHelper} handle specific behavior
 * of modules measuring rain
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RainChannelHelper extends AbstractChannelHelper {

    public RainChannelHelper() {
        super(Set.of(GROUP_RAIN));
    }

    @Override
    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
        switch (channelId) {
            case CHANNEL_VALUE:
                return toQuantityType(dashboard.getRain(), MeasureClass.RAIN_INTENSITY);
            case CHANNEL_SUM_RAIN1:
                return toQuantityType(dashboard.getSumRain1(), MeasureClass.RAIN_QTTY);
            case CHANNEL_SUM_RAIN24:
                return toQuantityType(dashboard.getSumRain24(), MeasureClass.RAIN_QTTY);
        }
        return null;
    }
}
