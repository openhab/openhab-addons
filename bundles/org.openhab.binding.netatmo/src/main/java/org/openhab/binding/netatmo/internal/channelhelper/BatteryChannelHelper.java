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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toStringType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link BatteryChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class BatteryChannelHelper extends AbstractChannelHelper {

    public BatteryChannelHelper() {
        super(Set.of(GROUP_BATTERY, GROUP_ENERGY_BATTERY));
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (naThing instanceof NAModule) {
            NAModule module = (NAModule) naThing;
            int percent = module.getBatteryPercent();
            switch (channelId) {
                case CHANNEL_VALUE:
                    return percent >= 0 ? new DecimalType(percent) : UnDefType.NULL;
                case CHANNEL_LOW_BATTERY:
                    return percent >= 0 ? OnOffType.from(percent < 20) : UnDefType.NULL;
                case CHANNEL_BATTERY_STATUS:
                    return toStringType(module.getBatteryState());
            }
        }
        return null;
    }
}
