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
import org.openhab.binding.sbus.handler.config.SbusChannelConfig;
import org.openhab.binding.sbus.handler.config.SbusDeviceConfig;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OpenClosedType;
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
 * The {@link SbusContactHandler} is responsible for handling commands for Sbus contact devices.
 * It supports reading the current contact state (open/closed).
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusContactHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusContactHandler.class);

    public SbusContactHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
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
            boolean[] contactStates = adapter.readContactStatusChannels(config.subnetId, config.id);

            // Iterate over all channels and update their states
            for (Channel channel : getThing().getChannels()) {
                if (!isLinked(channel.getUID())) {
                    continue;
                }
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= contactStates.length) {
                    boolean isOpen = contactStates[channelConfig.channelNumber - 1];
                    updateState(channel.getUID(), isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
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
        // Contact sensors are read-only
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        logger.debug("{}",
                translationProvider.getText(bundle, "info.contact.readonly", null, localeProvider.getLocale()));
    }
}
