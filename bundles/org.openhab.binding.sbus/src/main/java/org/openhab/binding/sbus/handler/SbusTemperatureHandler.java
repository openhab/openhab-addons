/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusTemperatureHandler} is responsible for handling commands for SBUS temperature sensors.
 * It supports reading temperature values in Celsius.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusTemperatureHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusTemperatureHandler.class);

    public SbusTemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // Get all channel configurations from the thing
        for (Channel channel : getThing().getChannels()) {
            // Channels are already defined in thing-types.xml, just validate their configuration
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber <= 0) {
                logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
            }
        }
    }

    @Override
    protected void pollDevice() {
        handleReadTemperature();
    }

    private void handleReadTemperature() {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("SBUS adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SBUS adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            float[] temperatures = adapter.readTemperatures(config.subnetId, config.id);
            if (temperatures == null) {
                logger.warn("Received null temperatures from SBUS device");
                return;
            }

            // Iterate over all channels and update their states with corresponding temperatures
            for (Channel channel : getThing().getChannels()) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= temperatures.length) {
                    float temperature = temperatures[channelConfig.channelNumber - 1];
                    updateState(channel.getUID(), new QuantityType<>(temperature, SIUnits.CELSIUS));
                }
            }
        } catch (Exception e) {
            logger.error("Error reading temperature", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Temperature sensors are read-only
        logger.debug("Temperature device is read-only, ignoring command");
    }
}
