/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import io.rudolph.netatmo.api.common.model.BatteryState;
import io.rudolph.netatmo.api.common.model.ClimateModule;
import io.rudolph.netatmo.api.energy.model.module.ValveBaseModule;
import io.rudolph.netatmo.api.energy.model.module.ValveModule;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_BATTERY_LEVEL;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_LOW_BATTERY;

/**
 * The {@link BatteryHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class BatteryHelper {
    private Logger logger = LoggerFactory.getLogger(BatteryHelper.class);

    private Object module;

    public void setModule(Object module) {
        this.module = module;
    }

    public Optional<State> getNAThingProperty(String channelId) {
        if (module != null) {
            switch (channelId) {
                case CHANNEL_BATTERY_LEVEL:
                    final Integer batteryLevel;
                    if (module instanceof ValveModule) {
                        batteryLevel = ((ValveModule) module).getBatteryLevel();
                    } else if (module instanceof ClimateModule) {
                        batteryLevel = ((ClimateModule) module).getBatteryVP();
                    } else {
                        break;
                    }
                    return Optional.of(ChannelTypeUtils.toDecimalType(batteryLevel));
                case CHANNEL_LOW_BATTERY:
                    final BatteryState batteryVp;
                    if (module instanceof ValveBaseModule) {
                        batteryVp = ((ValveBaseModule) module).getBatteryState();
                    } else {
                        break;
                    }
                    return Optional.of(batteryVp == BatteryState.NO_DATA ? OnOffType.OFF : OnOffType.ON);
                default:
                    logger.warn("The module has no property: {}", channelId);
                    return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
