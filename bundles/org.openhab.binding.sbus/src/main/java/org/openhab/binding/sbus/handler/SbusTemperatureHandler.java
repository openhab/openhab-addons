/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.sbus.handler.config.SbusDeviceConfig;
import org.openhab.binding.sbus.handler.config.TemperatureChannelConfig;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusTemperatureHandler} is responsible for handling commands for Sbus temperature sensors.
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
            TemperatureChannelConfig channelConfig = channel.getConfiguration().as(TemperatureChannelConfig.class);
            if (!channelConfig.isValid()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid channel configuration: " + channel.getUID());
                return;
            }
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sbus adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

            // Read temperatures in Celsius from device
            float[] temperatures = adapter.readTemperatures(config.subnetId, config.id, 1);

            // Iterate over all channels and update their states with corresponding temperatures
            for (Channel channel : getThing().getChannels()) {
                TemperatureChannelConfig channelConfig = channel.getConfiguration().as(TemperatureChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= temperatures.length) {
                    float temperatureCelsius = temperatures[channelConfig.channelNumber - 1];
                    if (channelConfig.isFahrenheit()) {
                        // Convert Celsius to Fahrenheit
                        float temperatureFahrenheit = (temperatureCelsius * 9 / 5) + 32;
                        updateState(channel.getUID(),
                                new QuantityType<>(temperatureFahrenheit, ImperialUnits.FAHRENHEIT));
                    } else {
                        updateState(channel.getUID(), new QuantityType<>(temperatureCelsius, SIUnits.CELSIUS));
                    }
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error reading device state");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Temperature sensors are read-only
        logger.debug("Temperature device is read-only, ignoring command");
    }
}
