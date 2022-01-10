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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadioHelper} handle specific behavior
 * of WIFI or RF devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RadioHelper {
    private final Logger logger = LoggerFactory.getLogger(RadioHelper.class);
    private final List<Integer> signalThresholds;
    private @Nullable Object module;

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
        Object module = this.module;
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
