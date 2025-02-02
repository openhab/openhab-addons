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
import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.handler.config.SbusChannelConfig;
import org.openhab.binding.sbus.handler.config.SbusDeviceConfig;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusRgbwHandler} is responsible for handling commands for Sbus RGBW devices.
 * It supports reading and controlling red, green, blue, and white color channels.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusRgbwHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusRgbwHandler.class);

    public SbusRgbwHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
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
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                logger.warn("{}", translationProvider.getText(bundle, "error.channel.no-type",
                        channel.getUID().toString(), localeProvider.getLocale()));
                continue;
            }
            String channelTypeId = channelTypeUID.getId();
            if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                if (channelConfig.channelNumber <= 0) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.channel.invalid-number",
                            channel.getUID().toString(), localeProvider.getLocale()));
                }
            }
            if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                switchChannelCount++;
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
                }
            }
        }
        if (switchChannelCount > 1) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.rgbw.too-many-switches", getThing().getUID().toString(), localeProvider.getLocale()));
            return;
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.device.adapter-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

            // Update all channels
            for (Channel channel : getThing().getChannels()) {
                if (!isLinked(channel.getUID())) {
                    continue;
                }
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("Channel {} has no channel type", channel.getUID().toString());
                    continue;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                    // Read RGBW values for this channel
                    int[] rgbwValues = adapter.readRgbw(config.subnetId, config.id, channelConfig.channelNumber);
                    if (rgbwValues.length >= 4) {
                        // Convert RGBW to HSB using our custom conversion
                        HSBType hsbType = ColorUtil.rgbToHsb(rgbwValues);
                        updateState(channel.getUID(), hsbType);
                    }
                } else if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                    // Read status channels for switch states
                    int[] statuses = adapter.readStatusChannels(config.subnetId, config.id);

                    // Update switch state
                    boolean isActive = isAnyRgbwValueActive(statuses);
                    updateState(channel.getUID(), isActive ? OnOffType.ON : OnOffType.OFF);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.read-state", null, localeProvider.getLocale()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.device.adapter-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        try {
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel != null) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.channel.no-type",
                            channel.getUID().toString(), localeProvider.getLocale()));
                    return;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)
                        && command instanceof HSBType hsbCommand) {
                    // Handle color command
                    int[] rgbw = ColorUtil.hsbToRgbw(hsbCommand);
                    adapter.writeRgbw(config.subnetId, config.id, channelConfig.channelNumber, rgbw);
                    updateState(channelUID, hsbCommand);
                } else if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)
                        && command instanceof OnOffType onOffCommand) {
                    // Handle switch command
                    boolean isOn = onOffCommand == OnOffType.ON;
                    adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, isOn ? 100 : 0,
                            -1);
                    updateState(channelUID, OnOffType.from(isOn));
                }
            }
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.send-command", null, localeProvider.getLocale()));
        }
    }
}
