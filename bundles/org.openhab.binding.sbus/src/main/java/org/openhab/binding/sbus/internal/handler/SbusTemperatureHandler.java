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
package org.openhab.binding.sbus.internal.handler;

import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.binding.sbus.internal.config.TemperatureChannelConfig;
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

import ro.ciprianpascu.sbus.facade.TemperatureUnit;
import ro.ciprianpascu.sbus.msg.ReadTemperatureRequest;
import ro.ciprianpascu.sbus.msg.ReadTemperatureResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.procimg.InputRegister;

/**
 * The {@link SbusTemperatureHandler} is responsible for handling commands for Sbus temperature sensors.
 * It supports reading temperature values in Celsius.
 *
 * @author Ciprian Pascu - Initial contribution
 */
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
                        "@text/error.channel.invalid-number [\"" + channel.getUID().toString() + "\"]");
                return;
            }
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

            // Read temperatures in Celsius from device
            float[] temperatures = readTemperatures(adapter, config.subnetId, config.id, TemperatureUnit.CELSIUS);

            // Iterate over all channels and update their states with corresponding temperatures
            updateChannelStatesFromTemperatures(temperatures);

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.device.read-state");
        }
    }

    /**
     * Update channel states based on temperature values.
     */
    private void updateChannelStatesFromTemperatures(float[] temperatures) {
        for (Channel channel : getThing().getChannels()) {
            if (!isLinked(channel.getUID())) {
                continue;
            }
            TemperatureChannelConfig channelConfig = channel.getConfiguration().as(TemperatureChannelConfig.class);
            if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= temperatures.length) {
                float temperatureCelsius = temperatures[channelConfig.channelNumber - 1];
                if (channelConfig.isFahrenheit()) {
                    // Convert Celsius to Fahrenheit
                    float temperatureFahrenheit = (temperatureCelsius * 9 / 5) + 32;
                    updateState(channel.getUID(), new QuantityType<>(temperatureFahrenheit, ImperialUnits.FAHRENHEIT));
                } else {
                    updateState(channel.getUID(), new QuantityType<>(temperatureCelsius, SIUnits.CELSIUS));
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Temperature sensors are read-only
        logger.debug("{}", "@text/info.temperature.readonly");
    }

    // SBUS Protocol Adaptation Methods

    /**
     * Reads temperature values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param temperatureUnit the unit of measurement (FAHRENHEIT or CELSIUS)
     * @return array of temperature values in the specified unit
     * @throws Exception if the SBUS transaction fails
     */
    private float[] readTemperatures(SbusService adapter, int subnetId, int deviceId, TemperatureUnit temperatureUnit)
            throws Exception {
        // Construct SBUS request
        ReadTemperatureRequest request = new ReadTemperatureRequest();
        request.setTemperatureUnit(temperatureUnit.getValue());
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadTemperatureResponse)) {
            throw new IllegalStateException("Unexpected response type: " + response.getClass().getSimpleName());
        }

        ReadTemperatureResponse tempResponse = (ReadTemperatureResponse) response;
        InputRegister[] registers = tempResponse.getRegisters();
        float[] temperatures = new float[registers.length];
        for (int i = 0; i < registers.length; i++) {
            byte[] bytes = registers[i].toBytes();
            if (bytes != null && bytes.length > 0) {
                temperatures[i] = bytes[0];
            } else {
                logger.warn("Register {} returned empty or null byte array, using 0 as default temperature", i);
                temperatures[i] = 0;
            }
        }
        return temperatures;
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof ReadTemperatureResponse) {
                // Process temperature response using existing logic
                ReadTemperatureResponse tempResponse = (ReadTemperatureResponse) response;
                float[] temperatures = extractTemperatures(tempResponse);

                // Update channel states based on async message
                updateChannelStatesFromTemperatures(temperatures);
                logger.debug("Processed async temperature message for handler {}", getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("Error processing async message in temperature handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (!(response instanceof ReadTemperatureResponse)) {
            return false;
        }

        // Check if the message is for this device based on subnet and unit ID
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
    }

    /**
     * Extract temperature values from ReadTemperatureResponse.
     * Reuses existing logic from readTemperatures method.
     */
    private float[] extractTemperatures(ReadTemperatureResponse response) {
        InputRegister[] registers = response.getRegisters();
        float[] temperatures = new float[registers.length];

        for (int i = 0; i < registers.length; i++) {
            byte[] bytes = registers[i].toBytes();
            if (bytes != null && bytes.length > 0) {
                temperatures[i] = bytes[0];
            } else {
                logger.warn("Register {} returned empty or null byte array, using 0 as default temperature", i);
                temperatures[i] = 0;
            }
        }
        return temperatures;
    }
}
