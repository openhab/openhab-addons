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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.DeviceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.ModuleChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.SignalHelper;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NetatmoHandlerBuilder {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandlerBuilder.class);
    private final Bridge bridge;
    private final TimeZoneProvider timeZoneProvider;
    private final NetatmoDescriptionProvider stateDescriptionProvider;
    private final List<AbstractChannelHelper> channelHelpers = new ArrayList<>();
    private final Class<?> handlerClass;
    private final ApiBridge apiBridge;

    public NetatmoHandlerBuilder(Bridge bridge, TimeZoneProvider timeZoneProvider,
            NetatmoDescriptionProvider stateDescriptionProvider, ApiBridge apiBridge, ModuleType moduleType) {
        this.bridge = bridge;
        this.timeZoneProvider = timeZoneProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.handlerClass = moduleType.handlerClass;
        this.apiBridge = apiBridge;
        if (moduleType.hasBattery()) {
            channelHelpers.add(new BatteryHelper(bridge, timeZoneProvider));
        }
        if (moduleType.groups.contains(GROUP_MODULE)) {
            channelHelpers.add(new ModuleChannelHelper(bridge, timeZoneProvider));
        }
        if (moduleType.groups.contains(GROUP_DEVICE)) {
            channelHelpers.add(new DeviceChannelHelper(bridge, timeZoneProvider));
        }
        if (moduleType.getSignalLevels() != NetatmoConstants.NO_RADIO) {
            channelHelpers.add(new SignalHelper(bridge, timeZoneProvider, moduleType.getSignalLevels()));
        }
        if (moduleType.extensions != null) {
            channelHelpers.add(new MeasuresChannelHelper(bridge, timeZoneProvider));
        }
        moduleType.channelHelpers.forEach(helper -> addChannelHelper(helper));
    }

    public @Nullable BaseThingHandler build() {
        try {
            Constructor<?> constructor = handlerClass.getConstructor(Bridge.class, List.class, ApiBridge.class,
                    TimeZoneProvider.class, NetatmoDescriptionProvider.class);
            return (BaseThingHandler) constructor.newInstance(
                    new Object[] { bridge, channelHelpers, apiBridge, timeZoneProvider, stateDescriptionProvider });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.warn("Error creating handler = {}", e.getMessage());
        }
        return null;
    }

    private void addChannelHelper(Class<?> channelHelpClass) {
        try {
            Constructor<?> constructor = channelHelpClass.getConstructor(Thing.class, TimeZoneProvider.class);
            channelHelpers
                    .add((AbstractChannelHelper) constructor.newInstance(new Object[] { bridge, timeZoneProvider }));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.warn("Error creating ChannelHelper instance : {}", e.getMessage());
        }
    }
}
