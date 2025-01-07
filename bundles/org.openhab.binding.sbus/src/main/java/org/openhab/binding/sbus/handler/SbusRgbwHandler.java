/**
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
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusRgbwHandler} is responsible for handling commands for Sbus RGBW devices.
 * It supports reading and controlling red, green, blue, and white color channels.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusRgbwHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusRgbwHandler.class);

    public SbusRgbwHandler(Thing thing) {
        super(thing);
    }

    /**
     * Converts an openHAB HSBType into an RGBW array ([R, G, B, W]),
     * with each channel in [0..255].
     *
     * We extract 'white' by taking the minimum of R, G, B and
     * subtracting it from each color channel.
     *
     * @param hsbType the openHAB HSBType (hue [0..360], sat [0..100], bri [0..100])
     * @return an int array [R, G, B, W] each in [0..255]
     */
    private int[] hsbToRgbw(HSBType hsbType) {
        // Convert HSBType to standard RGB [0..255]
        PercentType[] rgb = ColorUtil.hsbToRgbPercent(hsbType);
        // Convert each channel from 0..100 to 0..255
        int r = (int) Math.round(rgb[0].floatValue() * 2.55);
        int g = (int) Math.round(rgb[1].floatValue() * 2.55);
        int b = (int) Math.round(rgb[2].floatValue() * 2.55);

        // Determine the white component as the min of R, G, B
        int w = Math.min(r, Math.min(g, b));

        // Subtract W from each
        r -= w;
        g -= w;
        b -= w;

        return new int[] { r, g, b, w };
    }

    /**
     * Converts an RGBW array ([R, G, B, W]) back to an openHAB HSBType.
     *
     * We add the W channel back into R, G, and B, then clamp to [0..255].
     * Finally, we create an HSBType via fromRGB().
     *
     * @param rgbw an int array [R, G, B, W] each in [0..255]
     * @return an HSBType (hue [0..360], saturation/brightness [0..100])
     */
    private HSBType rgbwToHsb(int[] rgbw) {
        if (rgbw.length < 4) {
            throw new IllegalArgumentException("rgbw must have 4 elements: [R, G, B, W].");
        }

        int r = rgbw[0];
        int g = rgbw[1];
        int b = rgbw[2];
        int w = rgbw[3];

        // Restore the combined R, G, B
        int rTotal = r + w;
        int gTotal = g + w;
        int bTotal = b + w;

        // Clamp to [0..255]
        rTotal = Math.min(255, Math.max(0, rTotal));
        gTotal = Math.min(255, Math.max(0, gTotal));
        bTotal = Math.min(255, Math.max(0, bTotal));

        // Convert back to an HSBType via fromRGB
        HSBType hsbType = HSBType.fromRGB(rTotal, gTotal, bTotal);

        return hsbType;
    }

    /**
     * Checks if any RGBW value is greater than 0.
     *
     * @param rgbw an int array [R, G, B, W] each in [0..255]
     * @return true if any value is greater than 0, false otherwise
     */
    private boolean isAnyRgbwValueActive(int[] rgbw) {
        if (rgbw.length < 4) {
            return false;
        }
        for (int value : rgbw) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initializeChannels() {
        int switchChannelCount = 0;

        // Validate all channel configurations
        for (Channel channel : getThing().getChannels()) {
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                logger.warn("Channel {} has no channel type", channel.getUID());
                continue;
            }
            String channelTypeId = channelTypeUID.getId();
            if ("color-channel".equals(channelTypeId)) {
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
                }
            }
            if ("switch-channel".equals(channelTypeId)) {
                switchChannelCount++;
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
                }
            }
        }
        if (switchChannelCount > 1) {
            logger.error("Only one switch channel is allowed for RGBW thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Only one switch channel is allowed");
            return;
        }
    }

    @Override
    protected void pollDevice() {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sbus adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

            // Update all channels
            for (Channel channel : getThing().getChannels()) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("Channel {} has no channel type", channel.getUID());
                    continue;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if ("color-channel".equals(channelTypeId)) {
                    // Read RGBW values for this channel
                    int[] rgbwValues = adapter.readRgbw(config.subnetId, config.id, channelConfig.channelNumber);
                    if (rgbwValues != null && rgbwValues.length >= 4) {
                        // Convert RGBW to HSB using our custom conversion
                        HSBType hsbType = rgbwToHsb(rgbwValues);
                        updateState(channel.getUID(), hsbType);
                    }
                } else if ("switch-channel".equals(channelTypeId)) {
                    // Read status channels for switch states
                    int[] statuses = adapter.readStatusChannels(config.subnetId, config.id);

                    // Update switch state
                    boolean isActive = isAnyRgbwValueActive(statuses);
                    updateState(channel.getUID(), isActive ? OnOffType.ON : OnOffType.OFF);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error reading device state");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("Sbus adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sbus adapter not initialized");
            return;
        }

        try {
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel != null) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("Channel {} has no channel type", channel.getUID());
                    return;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if ("color-channel".equals(channelTypeId) && command instanceof HSBType hsbCommand) {
                    // Handle color command
                    int[] rgbw = hsbToRgbw(hsbCommand);
                    adapter.writeRgbw(config.subnetId, config.id, channelConfig.channelNumber, rgbw[0], rgbw[1],
                            rgbw[2], rgbw[3]);
                    updateState(channelUID, hsbCommand);
                } else if ("switch-channel".equals(channelTypeId) && command instanceof OnOffType onOffCommand) {
                    // Handle switch command
                    boolean isOn = onOffCommand == OnOffType.ON;
                    adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, isOn ? 100 : 0,
                            -1);
                    updateState(channelUID, isOn ? OnOffType.ON : OnOffType.OFF);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling command", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error sending command to device");
        }
    }
}
