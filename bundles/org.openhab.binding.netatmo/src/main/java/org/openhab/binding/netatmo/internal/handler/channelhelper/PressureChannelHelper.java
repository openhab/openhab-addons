/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link PressureChannelHelper} handles specific channels of modules measuring pressure
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PressureChannelHelper extends ChannelHelper {

    public PressureChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        switch (channelId) {
            case CHANNEL_VALUE:
                return toQuantityType(dashboard.getPressure(), MeasureClass.PRESSURE);
            case CHANNEL_ABSOLUTE_PRESSURE:
                return toQuantityType(dashboard.getAbsolutePressure(), MeasureClass.PRESSURE);
            case CHANNEL_TREND:
                return toStringType(dashboard.getPressureTrend());
        }
        return null;
    }
}
