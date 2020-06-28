/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.elgatokeylight.internal;

import static org.openhab.binding.elgatokeylight.internal.ElgatoKeyLightBindingConstants.CHANNEL_BRIGHTNESS;
import static org.openhab.binding.elgatokeylight.internal.ElgatoKeyLightBindingConstants.CHANNEL_COLOR_TEMPREATURE;
import static org.openhab.binding.elgatokeylight.internal.ElgatoKeyLightBindingConstants.CHANNEL_SWITCH;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.elgatokeylight.internal.ElgatoKeyLight.LightStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElgatoKeyLightHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Gunnar Wagenknecht - Initial contribution
 */
@NonNullByDefault
public class ElgatoKeyLightHandler extends BaseThingHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ElgatoKeyLightHandler.class);

    private volatile @Nullable ElgatoKeyLightConfiguration config;
    private volatile @Nullable ElgatoKeyLight light;
    private volatile @Nullable ScheduledFuture<?> pollingJob;

    public ElgatoKeyLightHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            this.pollingJob = null;
            pollingJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        ElgatoKeyLight light = this.light;
        if (light == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.OFFLINE.NONE, "Not initialized!");
            return;
        }

        try {
            LightStatus lightStatus = light.readLightStatus();

            if (command instanceof RefreshType) {
                updateState(lightStatus);
                return;
            }

            switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    boolean on = !PercentType.ZERO.equals(command);
                    int brightness = ((PercentType) command).intValue();
                    lightStatus = light.switchTo(on, brightness);
                } else if (command instanceof OnOffType) {
                    boolean on = OnOffType.ON.equals(command);
                    lightStatus = light.switchTo(on, lightStatus.brightness > 0 ? lightStatus.brightness : 100);
                } else if (command instanceof IncreaseDecreaseType) {
                    int brightness = LightStateConverter.toAdjustedBrightness((IncreaseDecreaseType) command,
                            lightStatus.brightness);
                    boolean on = brightness > 0;
                    lightStatus = light.switchTo(on, brightness);
                }
                updateState(lightStatus);
                break;

            case CHANNEL_COLOR_TEMPREATURE:
                if (command instanceof PercentType) {
                    int temperature = LightStateConverter.toColorTemperatureLightState((PercentType) command);
                    lightStatus = light.writeTemperature(temperature);
                } else if (command instanceof OnOffType) {
                    boolean on = OnOffType.ON.equals(command);
                    lightStatus = light.switchTo(on, lightStatus.brightness > 0 ? lightStatus.brightness : 100);
                } else if (command instanceof IncreaseDecreaseType) {
                    int temperature = LightStateConverter.toAdjustedColorTemp((IncreaseDecreaseType) command,
                            lightStatus.brightness);
                    lightStatus = light.writeTemperature(temperature);
                }
                updateState(lightStatus);
                break;
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElgatoKeyLightConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task
        // decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            if ((config.host == null) || config.host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host not set!");
                return;
            }
            if (config.port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port not set!");
                return;
            }
            try {
                light = new ElgatoKeyLight(config.host, config.port);
                LightStatus lightStatus = light.readLightStatus();
                if (lightStatus != null) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "No status returned querying device!");
                }
            } catch (Exception e) {
                LOG.debug("Exception reading light status '{}'", config.host, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Exception reading status: " + e.getMessage());
            }
        });

        Runnable refresRunnable = () -> {
            ElgatoKeyLight light = this.light;
            if (light != null) {
                try {
                    LightStatus lightStatus = light.readLightStatus();
                    if (lightStatus != null) {
                        if (thing.getStatus() != ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.ONLINE);
                        }
                        updateState(lightStatus);
                    }
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                    return;
                }
            }
        };
        pollingJob = scheduler.scheduleWithFixedDelay(refresRunnable, 0,
                config.refreshIntervalSeconds != null ? config.refreshIntervalSeconds : 300, TimeUnit.SECONDS);
    }

    private void updateState(final LightStatus lightStatus) {
        updateState(CHANNEL_SWITCH, OnOffType.from(lightStatus.on));
        updateState(CHANNEL_BRIGHTNESS, LightStateConverter.toBrightnessPercentType(lightStatus));
        updateState(CHANNEL_COLOR_TEMPREATURE, LightStateConverter.toColorTemperaturePercentType(lightStatus));
    }
}
