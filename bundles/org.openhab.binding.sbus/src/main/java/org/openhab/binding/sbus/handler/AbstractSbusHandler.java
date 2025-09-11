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

import static org.openhab.binding.sbus.BindingConstants.BINDING_ID;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sbus.handler.config.SbusDeviceConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractSbusHandler} is the base class for all Sbus device handlers.
 * It provides common functionality for device initialization, channel management, and polling.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSbusHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected @Nullable SbusService sbusAdapter;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected final TranslationProvider translationProvider;
    protected final LocaleProvider localeProvider;

    public AbstractSbusHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing);
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public final void initialize() {
        logger.debug("Initializing Sbus handler for thing {}", getThing().getUID());

        initializeChannels();

        Bridge bridge = getBridge();
        if (bridge == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    translationProvider.getText(bundle, "error.device.no-bridge", null, localeProvider.getLocale()));
            return;
        }

        SbusBridgeHandler bridgeHandler = (SbusBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null || bridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        sbusAdapter = bridgeHandler.getSbusConnection();
        if (sbusAdapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, translationProvider
                    .getText(bundle, "error.device.bridge-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        startPolling();
    }

    /**
     * Initialize channels for this device based on its configuration.
     * This method should be implemented by concrete handlers to set up their specific channels.
     */
    protected abstract void initializeChannels();

    /**
     * Create or update a channel with the specified ID and type.
     *
     * @param channelId The ID of the channel to create/update
     * @param channelTypeId The type ID of the channel
     */
    protected void createChannel(String channelId, String channelTypeId) {
        ThingBuilder thingBuilder = ThingBuilder.create(getThing().getThingTypeUID(), getThing().getUID())
                .withConfiguration(getThing().getConfiguration()).withBridge(getThing().getBridgeUID());

        // Add all existing channels except the one we're creating/updating
        ChannelUID newChannelUID = new ChannelUID(getThing().getUID(), channelId);
        for (Channel existingChannel : getThing().getChannels()) {
            if (!existingChannel.getUID().equals(newChannelUID)) {
                thingBuilder.withChannel(existingChannel);
            }
        }

        // Add the new channel
        Channel channel = ChannelBuilder.create(newChannelUID).withType(new ChannelTypeUID(BINDING_ID, channelTypeId))
                .withConfiguration(new Configuration()).build();
        thingBuilder.withChannel(channel);

        // Update the thing with the new channel configuration
        updateThing(thingBuilder.build());
    }

    /**
     * Start polling the device for updates based on the configured refresh interval.
     */
    protected void startPolling() {
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        if (config.refresh > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    pollDevice();
                } catch (Exception e) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.device.polling", null,
                            localeProvider.getLocale()), e);
                }
            }, 0, config.refresh, TimeUnit.SECONDS);
        }
    }

    /**
     * Poll the device for updates. This method should be implemented by concrete handlers
     * to update their specific channel states.
     */
    protected abstract void pollDevice();

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        final SbusService adapter = sbusAdapter;
        if (adapter != null) {
            adapter.close();
        }
        pollingJob = null;
        sbusAdapter = null;
        super.dispose();
    }
}
