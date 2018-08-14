/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import io.rudolph.netatmo.api.common.model.BatteryState;
import io.rudolph.netatmo.api.common.model.ClimateModule;
import io.rudolph.netatmo.api.energy.model.module.ValveModule;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BatteryHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class BatteryHelper {
    private Logger logger = LoggerFactory.getLogger(BatteryHelper.class);

    private Object module;

    public void setModule(Object module) {
        this.module = module;
    }

    public Optional<State> getNAThingProperty(String channelId) {
        if (module != null) {
            try {
                if (CHANNEL_BATTERY_LEVEL.equalsIgnoreCase(channelId)
                        || CHANNEL_LOW_BATTERY.equalsIgnoreCase(channelId)) {
                    switch (channelId) {
                        case CHANNEL_BATTERY_LEVEL:
                            Integer batteryLevel;
                            if (module instanceof ValveModule) {
                                batteryLevel = ((ValveModule) module).getBatteryLevel();
                            } else if (module instanceof ClimateModule) {
                                batteryLevel = ((ClimateModule) module).getBatteryVP();
                            } else {
                                break;
                            }
                            return Optional.of(ChannelTypeUtils.toDecimalType(batteryLevel));
                        case CHANNEL_LOW_BATTERY:
                            Method getBatteryVp = module.getClass().getMethod("getGetBatteryState");
                            BatteryState batteryVp = (BatteryState) getBatteryVp.invoke(module);
                            return Optional.of(batteryVp == BatteryState.NO_DATA ? OnOffType.OFF : OnOffType.ON);
                    }
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                logger.warn("The module has no method to access {} property : {}", channelId, e.getMessage());
                return Optional.of(UnDefType.NULL);
            }
        }
        return Optional.empty();
    }
}
