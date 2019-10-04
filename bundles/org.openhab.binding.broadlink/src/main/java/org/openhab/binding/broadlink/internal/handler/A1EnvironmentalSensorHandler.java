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
package org.openhab.binding.broadlink.internal.handler;

import com.github.mob41.blapi.A1Device;
import com.github.mob41.blapi.EnvData;
import com.github.mob41.blapi.FloureonDevice;
import com.github.mob41.blapi.mac.Mac;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link A1EnvironmentalSensorHandler} is responsible for handling A1 Environmental Sensors.
 *
 * @author Florian Mueller - Initial contribution
 */
public class A1EnvironmentalSensorHandler extends BroadlinkHandler{

    private final Logger logger = LoggerFactory.getLogger(A1EnvironmentalSensorHandler.class);
    private A1Device a1Device;

    public A1EnvironmentalSensorHandler(Thing thing) {
        super(thing);
        try {
            blDevice = new FloureonDevice(host, new Mac(mac));
            this.a1Device = (A1Device) blDevice;
        } catch (IOException e) {
            logger.error("Could not find broadlink device at Host {} with MAC {} ", host, mac, e);
            updateStatus(ThingStatus.OFFLINE);
        }

        authenticate();

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Channel {} cannot handle command {}",channelUID, command);
    }

    private void refreshData() {
        try {
            EnvData envData = a1Device.getSensorsData();
            logger.debug("Retrieved data from device {}: {}", thing.getUID(), envData);

            updateState(AIR_QUALITY, new StringType(envData.getAirQualityDescription()));
            updateState(HUMIDITY, new DecimalType(envData.getHumidity()));
            updateState(LIGHT, new StringType(envData.getLightDescription()));
            updateState(NOISE, new StringType(envData.getNoiseDescription()));
            updateState(TEMPERATURE, new DecimalType(envData.getTemp()));
        } catch (Exception e) {
            logger.error("Error while retrieving data for {}", thing.getUID(), e);
        }
    }
}
