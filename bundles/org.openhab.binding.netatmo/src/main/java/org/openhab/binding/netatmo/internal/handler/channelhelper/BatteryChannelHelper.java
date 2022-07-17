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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toStringType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.BatteryState;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.Module;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link BatteryChannelHelper} handles specific channels of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class BatteryChannelHelper extends ChannelHelper {

    public BatteryChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        int percent = -1;
        BatteryState batteryState = BatteryState.UNKNOWN;
        if (naThing instanceof Module) {
            percent = ((Module) naThing).getBatteryPercent();
            batteryState = ((Module) naThing).getBatteryState();
        } else if (naThing instanceof HomeStatusModule) {
            percent = ((HomeStatusModule) naThing).getBatteryState().level;
            batteryState = ((HomeStatusModule) naThing).getBatteryState();
        } else {
            return null;
        }
        switch (channelId) {
            case CHANNEL_VALUE:
                if (percent >= 0) {
                    return new DecimalType(percent);
                }
            case CHANNEL_LOW_BATTERY:
                if (percent >= 0) {
                    return OnOffType.from(percent < 20);
                }
            case CHANNEL_BATTERY_STATUS:
                return toStringType(batteryState);
        }
        return null;
    }
}
