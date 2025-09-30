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
package org.openhab.binding.sbus.internal.helper;

import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.internal.handler.Sbus9in1SensorsHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

import ro.ciprianpascu.sbus.msg.MotionSensorStatusReport;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link SbusLuxHelper} is a helper class for processing lux sensor channels
 * from 9-in-1 sensor devices. It processes lux values from ReadNineInOneStatusResponse
 * and MotionSensorStatusReport messages. This is a lightweight helper that does not manage
 * its own lifecycle, polling, or message listening - it is coordinated by Sbus9in1SensorsHandler.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusLuxHelper extends AbstractSbusHelper {

    public SbusLuxHelper(Thing thing, Sbus9in1SensorsHandler coordinator) {
        super(thing, coordinator);
    }

    @Override
    public void initialize() {
        // Initialize lux channel processing
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_LUX.equals(channel.getChannelTypeUID().getId())) {
                // Lux channels are already defined in the thing configuration
                logger.debug("Initialized lux channel: {}", channelUID.getId());
            }
        }
    }

    @Override
    public void processMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                processMotionSensorReport(report);
                logger.debug("Processed async motion sensor status report for lux helper {}", thing.getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                process9in1Response(statusResponse);
                logger.debug("Processed async 9-in-1 status response for lux helper {}", thing.getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in lux helper {}: {}", thing.getUID(), e.getMessage());
        }
    }

    @Override
    public boolean hasRelevantChannels() {
        for (Channel channel : thing.getChannels()) {
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_LUX.equals(channel.getChannelTypeUID().getId())) {
                return true;
            }
        }
        return false;
    }

    public void process9in1Response(ReadNineInOneStatusResponse response) {
        // Update lux channels from 9-in-1 response
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_LUX.equals(channel.getChannelTypeUID().getId())) {

                int luxValue = response.getLuxValue();
                QuantityType<?> state = new QuantityType<>(luxValue, Units.LUX);
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 lux channel {} state: {} lux", channelUID.getId(), luxValue);
            }
        }
    }

    public void processMotionSensorReport(MotionSensorStatusReport report) {
        // Update lux channels from motion sensor status report
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_LUX.equals(channel.getChannelTypeUID().getId())) {

                int luxValue = report.getLuxValue();
                QuantityType<?> state = new QuantityType<>(luxValue, Units.LUX);
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 lux channel {} state from report: {} lux", channelUID.getId(), luxValue);
            }
        }
    }
}
