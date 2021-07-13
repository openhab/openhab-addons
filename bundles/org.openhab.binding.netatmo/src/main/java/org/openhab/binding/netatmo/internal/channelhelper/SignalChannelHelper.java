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
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link SignalChannelHelper} handle specific behavior
 * of WIFI or RF devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SignalChannelHelper extends AbstractChannelHelper {
    private final int[] levels;

    public SignalChannelHelper(int[] signalLevels) {
        super(Set.of(GROUP_SIGNAL));
        this.levels = signalLevels;
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        int status = naThing.getRadioStatus();
        switch (channelId) {
            case CHANNEL_SIGNAL_STRENGTH:
                return new DecimalType(getSignalStrength(status));
            case CHANNEL_VALUE:
                return toQuantityType(status, Units.DECIBEL_MILLIWATTS);
        }
        return null;
    }

    private int getSignalStrength(int signalLevel) {
        int level;
        for (level = 0; level < levels.length; level++) {
            if (signalLevel > levels[level]) {
                break;
            }
        }
        return level;
    }
}
