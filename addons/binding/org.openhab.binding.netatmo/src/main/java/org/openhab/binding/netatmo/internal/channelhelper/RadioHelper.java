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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadioHelper} handle specific behavior
 * of WIFI or RF devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class RadioHelper {
    private Logger logger = LoggerFactory.getLogger(RadioHelper.class);
    private final List<Integer> signalThresholds;
    private Object module;

    public RadioHelper(String signalLevels) {
        signalThresholds = Stream.of(signalLevels.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    private int getSignalStrength(int signalLevel) {
        int level;
        for (level = 0; level < signalThresholds.size(); level++) {
            if (signalLevel > signalThresholds.get(level)) {
                break;
            }
        }
        return level;
    }

    public void setModule(Object module) {
        this.module = module;
    }

    public Optional<State> getNAThingProperty(String channelId) {
        if (module != null) {
            try {
                switch (channelId) {
                    case CHANNEL_RF_STATUS:
                        Method getRfStatus = module.getClass().getMethod("getRfStatus");
                        Integer rfStatus = (Integer) getRfStatus.invoke(module);
                        return Optional.of(new DecimalType(getSignalStrength(rfStatus)));
                    case CHANNEL_WIFI_STATUS:
                        Method getWifiStatus = module.getClass().getMethod("getWifiStatus");
                        Integer wifiStatus = (Integer) getWifiStatus.invoke(module);
                        return Optional.of(new DecimalType(getSignalStrength(wifiStatus)));
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
