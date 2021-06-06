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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link DeviceChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceChannelHelper extends AbstractChannelHelper {

    public DeviceChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, Set.of(GROUP_DEVICE));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        return CHANNEL_LAST_SEEN.equals(channelId) ? ChannelTypeUtils.toDateTimeType(naThing.getLastSeen()) : null;
    }
}
