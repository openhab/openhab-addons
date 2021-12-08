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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toDateTimeType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.types.State;

/**
 * The {@link TimestampExtendedChannelHelper} handle specific behavior
 * of modules reporting measurement timestamp in dashboard
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class TimestampExtendedChannelHelper extends TimestampChannelHelper {

    public TimestampExtendedChannelHelper() {
        super(GROUP_EXTENDED_TIMESTAMP);
    }

    @Override
    protected @Nullable State internalGetDashboard(String channelId, NADashboard dashboard) {
        return CHANNEL_MEASURE_TIMESTAMP.equals(channelId) ? toDateTimeType(dashboard.getTimeUtc()) : null;
    }
}
