/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;

import io.swagger.client.model.NAModule;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class NetatmoModuleHandler extends AbstractNetatmoThingHandler {
    private final int batteryMin;
    private final int batteryLow;
    private final int batteryMax;
    private final NetatmoModuleConfiguration configuration;
    protected NAModule module;

    protected NetatmoModuleHandler(Thing thing) {
        super(thing);
        Map<String, String> properties = thing.getProperties();
        this.batteryMax = Integer.parseInt(properties.get(PROPERTY_BATTERY_MAX));
        this.batteryMin = Integer.parseInt(properties.get(PROPERTY_BATTERY_MIN));
        this.batteryLow = Integer.parseInt(properties.get(PROPERTY_BATTERY_LOW));
        this.configuration = this.getConfigAs(NetatmoModuleConfiguration.class);
    }

    public String getParentId() {
        return configuration.getParentId();
    }

    public String getId() {
        return configuration.getEquipmentId();
    }

    private int getBatteryPercent(int batteryVp) {
        // With new battery, API may return a value superior to batteryMax !
        int correctedVp = Math.min(batteryVp, batteryMax);
        return (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
    }

    private boolean isBatteryLow(int batteryVp) {
        return (batteryVp < batteryLow);
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_BATTERY_LEVEL:
                return new DecimalType(getBatteryPercent(module.getBatteryVp()));
            case CHANNEL_LOW_BATTERY:
                return isBatteryLow(module.getBatteryVp()) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_LAST_MESSAGE:
                return new DateTimeType(timestampToCalendar(module.getLastMessage()));
            case CHANNEL_RF_STATUS:
                Integer rfStatus = module.getRfStatus();
                return new DecimalType(getSignalStrength(rfStatus));
            default:
                return super.getNAThingProperty(chanelId);
        }
    }

    @Override
    protected void updateChannels() {
        dashboard = module.getDashboardData();

        super.updateChannels();

    }

}
