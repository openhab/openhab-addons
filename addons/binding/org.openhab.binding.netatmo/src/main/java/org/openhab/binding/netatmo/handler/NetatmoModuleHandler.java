/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoDeviceConfiguration;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.NAModuleAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class NetatmoModuleHandler<X extends NetatmoModuleConfiguration>
        extends AbstractNetatmoThingHandler<X> {
    private static Logger logger = LoggerFactory.getLogger(NetatmoModuleHandler.class);
    private int batteryMin = 0;
    private int batteryLow = 0;
    private int batteryMax = 1;
    protected NAModuleAdapter module;

    protected NetatmoModuleHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    private void initializeBatteryLevels() {
        try {
            this.batteryMax = Integer.parseInt(getProperty(PROPERTY_BATTERY_MAX));
            this.batteryMin = Integer.parseInt(getProperty(PROPERTY_BATTERY_MIN));
            this.batteryLow = Integer.parseInt(getProperty(PROPERTY_BATTERY_LOW));
        } catch (NumberFormatException e) {
            logger.warn("Battery levels were not correctly initialised - reported values will be inconsistent");
        }
    }

    // when batteries are freshly changed, API may return a value superior to batteryMax !
    private int getBatteryPercent() {
        if (batteryMax == 1) {
            initializeBatteryLevels();
        }

        int correctedVp = Math.min(module.getBatteryVp(), batteryMax);
        return (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            switch (channelId) {
                case CHANNEL_BATTERY_LEVEL:
                    return ChannelTypeUtils.toDecimalType(getBatteryPercent());
                case CHANNEL_LOW_BATTERY:
                    return module.getBatteryVp() < batteryLow ? OnOffType.ON : OnOffType.OFF;
                case CHANNEL_LAST_MESSAGE:
                    return ChannelTypeUtils.toDateTimeType(module.getLastMessage());
                case CHANNEL_RF_STATUS:
                    Integer rfStatus = module.getRfStatus();
                    return ChannelTypeUtils.toDecimalType(getSignalStrength(rfStatus));

            }

        }
        return super.getNAThingProperty(channelId);
    }

    public void updateChannels(NAModuleAdapter module) {
        this.module = module;
        super.updateChannels(configuration.getParentId());
    }

    protected void requestParentRefresh() {
        logger.debug("Updating parent modules of {}", configuration.getEquipmentId());
        for (Thing thing : getBridge().getThings()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof NetatmoDeviceHandler) {
                @SuppressWarnings("unchecked")
                NetatmoDeviceHandler<NetatmoDeviceConfiguration> deviceHandler = (NetatmoDeviceHandler<NetatmoDeviceConfiguration>) thingHandler;
                NetatmoDeviceConfiguration deviceConfiguration = deviceHandler.configuration;
                if (deviceConfiguration.getEquipmentId().equalsIgnoreCase(configuration.getParentId())) {
                    // I'm your father Luke
                    thingHandler.handleCommand(null, RefreshType.REFRESH);
                }
            }
        }
    }

}
