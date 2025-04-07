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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusSwitchHandler} is responsible for handling commands for Sbus switch devices.
 * It supports reading the current state and switching the device on/off.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusSwitchHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusSwitchHandler.class);

    public SbusSwitchHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected void initializeChannels() {
        // Get all channel configurations from the thing
        for (Channel channel : getThing().getChannels()) {
            // Channels are already defined in thing-types.xml, just validate their configuration
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber <= 0) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                logger.warn("{}", translationProvider.getText(bundle, "error.channel.invalid-number",
                        channel.getUID().toString(), localeProvider.getLocale()));
            }
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
            int[] statuses = adapter.readStatusChannels(config.subnetId, config.id);

            // Iterate over all channels and update their states
            for (Channel channel : getThing().getChannels()) {
                if (!isLinked(channel.getUID())) {
                    continue;
                }
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= statuses.length) {
                    var channelTypeUID = channel.getChannelTypeUID();
                    if (channelTypeUID == null) {
                        Bundle bundle = FrameworkUtil.getBundle(getClass());
                        logger.warn("{}", translationProvider.getText(bundle, "error.channel.no-type",
                                channel.getUID().toString(), localeProvider.getLocale()));
                        continue;
                    }
                    String channelTypeId = channelTypeUID.getId();
                    // 100 when on, something else when off
                    boolean isActive = statuses[channelConfig.channelNumber - 1] == 0x64;

                    if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                        updateState(channel.getUID(), OnOffType.from(isActive));
                    } else if (BindingConstants.CHANNEL_TYPE_DIMMER.equals(channelTypeId)) {
                        updateState(channel.getUID(), new PercentType(statuses[channelConfig.channelNumber - 1]));
                    } else if (BindingConstants.CHANNEL_TYPE_PAIRED.equals(channelTypeId)) {
                        updateState(channel.getUID(), isActive ? UpDownType.UP : UpDownType.DOWN);
                    }
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
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber <= 0) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.channel.invalid-number",
                            channelUID.toString(), localeProvider.getLocale()));
                    return;
                }

                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

                if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof PercentType percentCommand) {
                    handlePercentCommand(percentCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof UpDownType upDownCommand) {
                    handleUpDownCommand(upDownCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof StopMoveType stopMoveCommand && stopMoveCommand == StopMoveType.STOP) {
                    handleStopMoveCommand(config, channelConfig, channelUID, adapter);
                }
            }
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            logger.warn("{}",
                    translationProvider.getText(bundle, "error.device.send-command", null, localeProvider.getLocale()),
                    e);
        }
    }

    private void handleOnOffCommand(OnOffType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isOn = command == OnOffType.ON;
        adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, isOn ? 100 : 0,
                channelConfig.timer);
        updateState(channelUID, OnOffType.from(isOn));
    }

    private void handlePercentCommand(PercentType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, command.intValue(),
                channelConfig.timer);
        updateState(channelUID, command);
    }

    private void handleUpDownCommand(UpDownType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isUp = command == UpDownType.UP;
        // Set main channel
        if (getChannelToClose(channelConfig, isUp) > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, getChannelToClose(channelConfig, isUp), 0,
                    channelConfig.timer);
        }
        // Set paired channel to opposite state if configured
        if (getChannelToOpen(channelConfig, isUp) > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, getChannelToOpen(channelConfig, isUp), 0x64,
                    channelConfig.timer);
        }
        updateState(channelUID, isUp ? UpDownType.UP : UpDownType.DOWN);
    }

    private int getChannelToOpen(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.channelNumber : channelConfig.pairedChannelNumber;
    }

    private int getChannelToClose(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.pairedChannelNumber : channelConfig.channelNumber;
    }

    private void handleStopMoveCommand(SbusDeviceConfig config, SbusChannelConfig channelConfig, ChannelUID channelUID,
            SbusService adapter) throws Exception {
        // For STOP command, we stop both channels by setting them to 0
        if (channelConfig.channelNumber > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, 0, channelConfig.timer);
        }
        if (channelConfig.pairedChannelNumber > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.pairedChannelNumber, 0,
                    channelConfig.timer);
        }
        // We don't update the state here as the rollershutter is neither UP nor DOWN after stopping
    }
}
