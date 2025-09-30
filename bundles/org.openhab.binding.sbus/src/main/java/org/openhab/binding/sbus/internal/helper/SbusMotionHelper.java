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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

import ro.ciprianpascu.sbus.msg.MotionSensorStatusReport;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link SbusMotionHelper} is a helper class for processing motion sensor channels
 * from 9-in-1 sensor devices. It processes motion states from ReadNineInOneStatusResponse
 * and MotionSensorStatusReport messages. This is a lightweight helper that does not manage
 * its own lifecycle, polling, or message listening - it is coordinated by Sbus9in1SensorsHandler.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusMotionHelper extends AbstractSbusHelper {

    public SbusMotionHelper(Thing thing, Sbus9in1SensorsHandler coordinator) {
        super(thing, coordinator);
    }

    @Override
    public void initialize() {
        // Initialize motion channel processing
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_MOTION.equals(channel.getChannelTypeUID().getId())) {
                // Motion channels are already defined in the thing configuration
                logger.debug("Initialized motion channel: {}", channelUID.getId());
            }
        }
    }

    @Override
    public void processMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                processMotionSensorReport(report);
                logger.debug("Processed async motion sensor status report for motion helper {}", thing.getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                process9in1Response(statusResponse);
                logger.debug("Processed async 9-in-1 status response for motion helper {}", thing.getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in motion helper {}: {}", thing.getUID(), e.getMessage());
        }
    }

    @Override
    public boolean hasRelevantChannels() {
        for (Channel channel : thing.getChannels()) {
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_MOTION.equals(channel.getChannelTypeUID().getId())) {
                return true;
            }
        }
        return false;
    }

    public void process9in1Response(ReadNineInOneStatusResponse response) {
        // Update motion channels from 9-in-1 response
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_MOTION.equals(channel.getChannelTypeUID().getId())) {

                boolean motionDetected = response.getMotionStatus() > 0;
                OnOffType state = motionDetected ? OnOffType.ON : OnOffType.OFF;
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 motion channel {} state: {}", channelUID.getId(), state);
            }
        }
    }

    public void processMotionSensorReport(MotionSensorStatusReport report) {
        // Update motion channels from motion sensor status report
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_MOTION.equals(channel.getChannelTypeUID().getId())) {

                boolean motionDetected = report.getMotionStatus() > 0;
                OnOffType state = motionDetected ? OnOffType.ON : OnOffType.OFF;
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 motion channel {} state from report: {}", channelUID.getId(), state);
            }
        }
    }
}
