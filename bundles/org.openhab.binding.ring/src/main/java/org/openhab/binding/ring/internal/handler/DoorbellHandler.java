/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;
import static org.openhab.binding.ring.internal.ApiConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.device.Doorbell;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The handler for a Ring Video Doorbell.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 *
 */

@NonNullByDefault
public class DoorbellHandler extends RingDeviceHandler {
    private int lastBattery = -1;
    private long lastSnapshotTimestamp = -1;
    private TimeZoneProvider timeZoneProvider;
    private boolean motionDetectionSupport = false;

    public DoorbellHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Doorbell handler");
        super.initialize(Doorbell.class);
        String kind = thing.getProperties().get(THING_PROPERTY_KIND);
        if (kind != null && !kind.isEmpty()) {
            if (MOTION_DETECTION_KINDS.contains(kind)) {
                motionDetectionSupport = true;
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_CONTROL_MOTION_DETECTION);
                Channel channel = thing.getChannel(channelUID);
                if (channel == null) {
                    logger.debug("Adding channel for motion detection status, on device {}", getThing().getUID());
                    ThingBuilder thingBuilder = editThing();
                    channel = ChannelBuilder.create(channelUID, CoreItemFactory.SWITCH)
                            .withLabel("Motion Detection Status")
                            .withType(new ChannelTypeUID(BINDING_ID, "motionDetection")).build();
                    thingBuilder.withChannel(channel);
                    updateThing(thingBuilder.build());
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            return;
        }
        if (channelUID.getId().equals(CHANNEL_CONTROL_MOTION_DETECTION)) {
            if (command instanceof OnOffType onOffCommand) {
                motionDetectionCommand(onOffCommand == OnOffType.ON);
            }
        }
    }

    @Override
    protected void refreshState() {
        // Do Nothing
    }

    @Override
    protected void minuteTick() {
        logger.debug("DoorbellHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }
        RingDeviceTO deviceTO = device.getDeviceStatus();
        if (deviceTO.health.batteryPercentage != lastBattery) {
            logger.debug("Battery Level: {}", deviceTO.health.batteryPercentage);
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_BATTERY);
            updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
            lastBattery = deviceTO.health.batteryPercentage;
        } else {
            logger.debug("Battery Level Unchanged for {} - {} vs {}", getThing().getUID().getId(),
                    deviceTO.health.batteryPercentage, lastBattery);
        }
        if (motionDetectionSupport) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_CONTROL_MOTION_DETECTION);
            updateState(channelUID, OnOffType.from(deviceTO.deviceSettings.motionDetectionEnabled));
        }
        long timestamp = getSnapshotTimestamp();
        if (timestamp > lastSnapshotTimestamp) {
            logger.debug("timestamp = {} != lastSnapshotTimestamp {}, update snapshot channel", timestamp,
                    lastSnapshotTimestamp);
            lastSnapshotTimestamp = timestamp;
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_SNAPSHOT);
            updateState(channelUID, new RawType(getSnapshot(), "image/jpeg"));
            channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_SNAPSHOT_TIMESTAMP);
            updateState(channelUID, new DateTimeType(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp),
                    timeZoneProvider.getTimeZone())));
        }
    }

    protected void motionDetectionCommand(boolean b) {
        String command = URL_SETTINGS;
        String payload = "{\"motion_settings\": {\"motion_detection_enabled\": " + b + "}}";
        sendCommand(URL_DEVICE, command, HttpMethod.PATCH, payload);
    }
}
