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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link Co2ChannelHelper} handle specific channels
 * of modules handler ppm measurement
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Co2ChannelHelper extends AbstractChannelHelper {

    public Co2ChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_CO2);
    }

    @Override
    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
        return CHANNEL_VALUE.equals(channelId) ? toQuantityType(dashboard.getCo2(), NetatmoConstants.CO2_UNIT) : null;
    }
}
