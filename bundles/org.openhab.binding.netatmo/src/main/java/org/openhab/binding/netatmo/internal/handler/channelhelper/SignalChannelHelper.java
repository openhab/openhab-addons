/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link SignalChannelHelper} handles specific behavior of WIFI or RF devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SignalChannelHelper extends ChannelHelper {

    public SignalChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        int status = naThing.getRadioStatus();
        if (status != -1) {
            switch (channelId) {
                case CHANNEL_SIGNAL_STRENGTH:
                    return new DecimalType(getSignalStrength(status, naThing.getType().getSignalLevels()));
                case CHANNEL_VALUE:
                    return toQuantityType(status, Units.DECIBEL_MILLIWATTS);
            }
        }
        return null;
    }

    private int getSignalStrength(int signalLevel, int[] levels) {
        int level;
        for (level = 0; level < levels.length; level++) {
            if (signalLevel > levels[level]) {
                break;
            }
        }
        return level;
    }
}
