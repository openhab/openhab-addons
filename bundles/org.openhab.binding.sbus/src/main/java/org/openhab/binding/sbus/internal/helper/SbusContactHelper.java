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
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.handler.Sbus9in1SensorsHandler;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

import ro.ciprianpascu.sbus.msg.MotionSensorStatusReport;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link SbusContactHelper} is a helper class for processing contact sensor channels
 * from 9-in-1 sensor devices. It processes dry contact states from ReadNineInOneStatusResponse
 * and MotionSensorStatusReport messages. This is a lightweight helper that does not manage
 * its own lifecycle, polling, or message listening - it is coordinated by Sbus9in1SensorsHandler.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusContactHelper extends AbstractSbusHelper {

    public SbusContactHelper(Thing thing, Sbus9in1SensorsHandler coordinator) {
        super(thing, coordinator);
    }

    @Override
    public void initialize() {
        // Initialize contact channel processing
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
                // Contact channels are already defined in the thing configuration
                logger.debug("Initialized contact channel: {}", channelUID.getId());
            }
        }
    }

    @Override
    public void processMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                processMotionSensorReport(report);
                logger.debug("Processed async motion sensor status report for contact helper {}", thing.getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                process9in1Response(statusResponse);
                logger.debug("Processed async 9-in-1 status response for contact helper {}", thing.getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in contact helper {}: {}", thing.getUID(), e.getMessage());
        }
    }

    @Override
    public boolean hasRelevantChannels() {
        for (Channel channel : thing.getChannels()) {
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
                return true;
            }
        }
        return false;
    }

    private void process9in1Response(ReadNineInOneStatusResponse response) {
        // Update contact channels from 9-in-1 response
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                // Use channelNumber to determine which dry contact (1 or 2, default to 1)
                int channelNumber = channelConfig.channelNumber > 0 ? channelConfig.channelNumber : 1;
                boolean contactState = false;

                if (channelNumber == 1) {
                    contactState = response.getDryContact1Status() > 0;
                } else if (channelNumber == 2) {
                    contactState = response.getDryContact2Status() > 0;
                }

                OpenClosedType state = contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 contact channel {} (number {}) state: {}", channelUID.getId(),
                        channelNumber, state);
            }
        }
    }

    private void processMotionSensorReport(MotionSensorStatusReport report) {
        // Update contact channels from motion sensor status report
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                // Use channelNumber to determine which dry contact (1 or 2, default to 1)
                int channelNumber = channelConfig.channelNumber > 0 ? channelConfig.channelNumber : 1;
                boolean contactState = false;

                if (channelNumber == 1) {
                    contactState = report.getDryContactStatus(0) > 0; // First dry contact (index 0)
                } else if (channelNumber == 2) {
                    contactState = report.getDryContactStatus(1) > 0; // Second dry contact (index 1)
                }

                OpenClosedType state = contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                coordinator.updateChannelState(channelUID, state);

                logger.debug("Updated 9-in-1 contact channel {} (number {}) state from report: {}", channelUID.getId(),
                        channelNumber, state);
            }
        }
    }
}
