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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

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
    private int batteryLow;

    private Object module;

    public BatteryHelper(String batteryLevels) {
        List<String> thresholds = Arrays.asList(batteryLevels.split(","));
        batteryLow = Integer.parseInt(thresholds.get(1));
    }

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
                            Method getBatteryPercent = module.getClass().getMethod("getBatteryPercent");
                            Integer batteryPercent = (Integer) getBatteryPercent.invoke(module);
                            return Optional.of(ChannelTypeUtils.toDecimalType(batteryPercent));
                        case CHANNEL_LOW_BATTERY:
                            Method getBatteryVp = module.getClass().getMethod("getBatteryVp");
                            Integer batteryVp = (Integer) getBatteryVp.invoke(module);
                            return Optional.of(batteryVp < batteryLow ? OnOffType.ON : OnOffType.OFF);
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
