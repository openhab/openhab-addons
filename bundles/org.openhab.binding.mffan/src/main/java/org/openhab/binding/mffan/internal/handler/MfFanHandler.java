/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mffan.internal.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mffan.internal.MfFanBindingConstants;
import org.openhab.binding.mffan.internal.MfFanConfiguration;
import org.openhab.binding.mffan.internal.api.FanRestApi;
import org.openhab.binding.mffan.internal.api.ShadowBufferDto;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MfFanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class MfFanHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MfFanHandler.class);

    @NonNullByDefault({} /* non-null if initialized */)
    private MfFanConfiguration config;

    @NonNullByDefault({} /* non-null if initialized */)
    private FanRestApi api;

    @NonNullByDefault({} /* non-null if initialized */)
    private ScheduledFuture<?> pollingJob;

    private final ExecutorService executor;

    private final HttpClientFactory httpClientFactory;

    public MfFanHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize() {
        this.logger.debug("Initializing MfFan handler '{}'", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        this.config = getConfigAs(MfFanConfiguration.class);
        if (validateConfig(this.config)) {
            try {
                this.api = new FanRestApi(this.config.getIpAddress().trim(), this.httpClientFactory);
                // handleCommand(RefreshType.REFRESH);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }

            this.pollingJob = this.scheduler.scheduleWithFixedDelay(() -> handleCommand(RefreshType.REFRESH), 0,
                    this.config.getPollingPeriod(), TimeUnit.SECONDS);

            this.logger.debug("Polling job scheduled to run every {} sec. for '{}'", this.config.getPollingPeriod(),
                    getThing().getUID());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration detected.");
        }
    }

    @Override
    public void dispose() {
        this.logger.debug("Disposing MF fan handler '{}'", getThing().getUID());

        ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }

        ExecutorService exec = this.executor;
        exec.shutdown();
        try {
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } catch (@SuppressWarnings("unused") InterruptedException e) {
            exec.shutdownNow();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            this.executor.execute(new CommandHandlerRunnable(channelUID, command));
        } catch (@SuppressWarnings("unused") Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Could not control device at IP address %s", this.config.getIpAddress()));
        }
    }

    private void handleCommand(Command command) {
        handleCommand(new ChannelUID("Null:Null:Null:Null"), command);
    }

    private final class CommandHandlerRunnable implements Runnable {
        private final ChannelUID channel;
        private final Command cmd;

        public CommandHandlerRunnable(ChannelUID channelUID, Command command) {
            this.channel = channelUID;
            this.cmd = command;
        }

        @Override
        public void run() {
            handleCommand(this.channel, this.cmd);
        }

        @SuppressWarnings({ "synthetic-access" })
        private void handleCommand(ChannelUID channelUID, Command command) {
            try {
                if (command instanceof RefreshType) {
                    update(MfFanHandler.this.api.getShadowBuffer());
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_FAN_ON)) {
                    if (command instanceof OnOffType onOffCommand) {
                        update(MfFanHandler.this.api.setFanPower(onOffCommand == OnOffType.ON));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_FAN_SPEED)) {
                    if (command instanceof StringType stringCommand) {
                        update(MfFanHandler.this.api.setFanSpeed(Integer.valueOf(stringCommand.toString())));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_FAN_DIRECTION)) {
                    if (command instanceof StringType stringCommand) {
                        update(MfFanHandler.this.api
                                .setFanDirection(ShadowBufferDto.FanDirection.valueOf(stringCommand.toString())));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_LIGHT_ON)) {
                    if (command instanceof OnOffType onOffCommand) {
                        update(MfFanHandler.this.api.setLightPower(onOffCommand == OnOffType.ON));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_LIGHT_INTENSITY)) {
                    if (command instanceof PercentType percentCommand) {
                        update(MfFanHandler.this.api.setLightIntensity(percentCommand.intValue()));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_WIND_ON)) {
                    if (command instanceof OnOffType onOffCommand) {
                        update(MfFanHandler.this.api.setWindPower(onOffCommand == OnOffType.ON));
                    }
                } else if (channelUID.getId().equals(MfFanBindingConstants.CHANNEL_WIND_LEVEL)) {
                    if (command instanceof StringType stringCommand) {
                        update(MfFanHandler.this.api.setWindSpeed(Integer.valueOf(stringCommand.toString())));
                    }
                } else {
                    MfFanHandler.this.logger.warn("Skipping command. Unidentified channel id '{}'", channelUID.getId());

                }
            } catch (@SuppressWarnings("unused") Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                        .format("Could not control device at IP address %s", MfFanHandler.this.config.getIpAddress()));
            }
        }

        @SuppressWarnings("synthetic-access")
        private void update(@Nullable ShadowBufferDto dto) {
            MfFanHandler.this.logger.debug("Updating data '{}'", getThing().getUID());

            try {
                if (dto != null) {
                    updateState(MfFanBindingConstants.CHANNEL_FAN_ON, OnOffType.from(dto.getFanOn().booleanValue()));
                    updateState(MfFanBindingConstants.CHANNEL_FAN_SPEED,
                            StringType.valueOf(String.valueOf(dto.getFanSpeed())));
                    updateState(MfFanBindingConstants.CHANNEL_FAN_DIRECTION,
                            StringType.valueOf(dto.getFanDirection().name()));
                    updateState(MfFanBindingConstants.CHANNEL_WIND_ON, OnOffType.from(dto.getWind().booleanValue()));
                    updateState(MfFanBindingConstants.CHANNEL_WIND_LEVEL,
                            StringType.valueOf(String.valueOf(dto.getWindSpeed())));
                    updateState(MfFanBindingConstants.CHANNEL_LIGHT_ON,
                            OnOffType.from(dto.getLightOn().booleanValue()));
                    updateState(MfFanBindingConstants.CHANNEL_LIGHT_INTENSITY,
                            new PercentType(dto.getLightBrightness()));

                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Null shadow buffer returned.");
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private static boolean validateConfig(@Nullable MfFanConfiguration config) {
        if ((config == null) || (config.getIpAddress().trim().length() == 0)) {
            return false;
        }
        if (config.getPollingPeriod() < 10) {
            return false;
        }
        return true;
    }
}
