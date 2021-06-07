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
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link SignalHelper} handle specific behavior
 * of WIFI or RF devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SignalHelper extends AbstractChannelHelper {
    private final int[] levels;

    public SignalHelper(Thing thing, TimeZoneProvider timeZoneProvider, int[] signalLevels) {
        super(thing, timeZoneProvider, Set.of(GROUP_SIGNAL));
        this.levels = signalLevels;
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

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        int status = naThing.getRadioStatus();
        return CHANNEL_SIGNAL_STRENGTH.equals(channelId) ? new DecimalType(getSignalStrength(status))
                : CHANNEL_VALUE.equals(channelId) ? new QuantityType<>(status, Units.DECIBEL_MILLIWATTS) : null;
    }
}
