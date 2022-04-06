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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link AirQualityExtChannelHelper} handles specific channels of NHC thing.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AirQualityExtChannelHelper extends AirQualityChannelHelper {

    public AirQualityExtChannelHelper() {
        super(GROUP_TYPE_AIR_QUALITY_EXTENDED);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        return CHANNEL_HEALTH_INDEX.equals(channelId) ? new DecimalType(dashboard.getHealthIdx())
                : super.internalGetDashboard(channelId, dashboard);
    }
}
