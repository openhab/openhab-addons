/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private int batteryMin = 0;
    private int batteryLow = 0;
    private int batteryMax = 1;

    Object module;

    public BatteryHelper(String batteryLevels) {
        List<String> thresholds = Arrays.asList(batteryLevels.split(","));
        batteryMin = Integer.parseInt(thresholds.get(0));
        batteryLow = Integer.parseInt(thresholds.get(1));
        batteryMax = Integer.parseInt(thresholds.get(2));
    }

    public void setModule(Object module) {
        this.module = module;
    }

    public Optional<State> getNAThingProperty(String channelId) {
        if (module != null) {
            try {
                if (CHANNEL_BATTERY_LEVEL.equalsIgnoreCase(channelId)
                        || CHANNEL_LOW_BATTERY.equalsIgnoreCase(channelId)) {
                    Method getBatteryVp = module.getClass().getMethod("getBatteryVp");
                    Integer value = (Integer) getBatteryVp.invoke(module);
                    switch (channelId) {
                        case CHANNEL_BATTERY_LEVEL:
                            // when batteries are freshly changed, API may return a value superior to batteryMax !
                            int correctedVp = Math.min(value.intValue(), batteryMax);
                            int batteryPercent = (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
                            return Optional.of(ChannelTypeUtils.toDecimalType(batteryPercent));
                        case CHANNEL_LOW_BATTERY:
                            return Optional.of(value.intValue() < batteryLow ? OnOffType.ON : OnOffType.OFF);
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
