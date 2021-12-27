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
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.types.State;

/**
 * The {@link TimestampChannelHelper} handles specific behavior
 * of modules reporting last seen
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class TimestampChannelHelper extends AbstractChannelHelper {

    public TimestampChannelHelper() {
        this(GROUP_TIMESTAMP);
    }

    protected TimestampChannelHelper(String groupName) {
        super(groupName);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        return CHANNEL_LAST_SEEN.equals(channelId) ? toDateTimeType(naThing.getLastSeen()) : null;
    }
}
