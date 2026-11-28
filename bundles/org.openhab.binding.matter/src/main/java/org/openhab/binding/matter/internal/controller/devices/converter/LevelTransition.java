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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonNull;

/**
 * Transition times for the level and color commands of an endpoint.
 * Matter uses tenths of a second. The configuration uses seconds.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class LevelTransition {
    private static final int MAX_DEVICE_DEFAULT = 65535;
    // Level and color commands accept at most 65534. Level commands use 65535 for null.
    private static final int MAX_COMMAND_TRANSITION = 65534;

    private final Logger logger = LoggerFactory.getLogger(LevelTransition.class);
    private final MatterBaseThingHandler handler;
    private final int endpointNumber;
    // The channel that holds the device default
    private final String channelId;
    private volatile @Nullable Integer deviceDefault;

    LevelTransition(MatterBaseThingHandler handler, int endpointNumber, String channelId) {
        this.handler = handler;
        this.endpointNumber = endpointNumber;
        this.channelId = channelId;
    }

    void initialize(@Nullable LevelControlCluster cluster) {
        Integer deviceDefault = cluster != null ? cluster.onOffTransitionTime : null;
        this.deviceDefault = deviceDefault;
        if (deviceDefault == null) {
            return;
        }
        handler.addChannelConfigDescription(channelUID(), List.of(defaultParameter()));
        applyConfiguredDefault();
    }

    /**
     * Writes the configured default to the device when they differ. The configured value wins.
     */
    void applyConfiguredDefault() {
        Integer deviceDefault = this.deviceDefault;
        if (deviceDefault == null) {
            return;
        }
        Object value = channelConfig().get(CONFIG_CHANNEL_DEFAULT_TRANSITION_TIME);
        if (value == null) {
            updateDefaultConfig(deviceDefault);
            return;
        }
        Integer configured = toTenths(value, MAX_DEVICE_DEFAULT);
        if (configured == null) {
            logger.debug("Ignoring invalid default transition time {} for endpoint {}", value, endpointNumber);
            restoreDefaultConfig();
        } else if (!configured.equals(deviceDefault)) {
            // Endpoints are rebuilt when the device comes back online, which applies the value then
            if (handler.getThing().getStatus() != ThingStatus.ONLINE) {
                return;
            }
            logger.debug("Updating default transition time of endpoint {} from {} to {}", endpointNumber, deviceDefault,
                    configured);
            // deviceDefault updates when the device reports the new value
            handler.writeAttribute(endpointNumber, LevelControlCluster.CLUSTER_NAME,
                    LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME, String.valueOf(configured))
                    .exceptionally(e -> {
                        logger.debug("Failed to update default transition time: {}", e.getMessage());
                        if (isRejectedByDevice(e)) {
                            restoreDefaultConfig();
                        }
                        return Void.TYPE.cast(null);
                    });
        }
    }

    /**
     * True when the controller answered with an error while the device is online.
     * Other failures keep the configured value so it is written again when the endpoints are rebuilt.
     */
    private boolean isRejectedByDevice(Throwable e) {
        Throwable cause = e instanceof CompletionException ? e.getCause() : e;
        return cause instanceof MatterRequestException request && request.isFromServer()
                && handler.getThing().getStatus() == ThingStatus.ONLINE;
    }

    /**
     * @return tenths of a second, or null if not set
     */
    @Nullable
    Integer channelTransitionTime(ChannelUID channelUID) {
        Channel channel = handler.getThing().getChannel(channelUID);
        Object value = channel != null ? channel.getConfiguration().get(CONFIG_CHANNEL_TRANSITION_TIME) : null;
        if (value == null) {
            return null;
        }
        Integer tenths = toTenths(value, MAX_COMMAND_TRANSITION);
        if (tenths == null) {
            logger.debug("Ignoring invalid transition time {} on channel {}", value, channelUID);
        }
        return tenths;
    }

    // Color has no device default, so the level default is sent instead
    int colorTransitionTime(@Nullable Integer channelTransition) {
        if (channelTransition != null) {
            return channelTransition;
        }
        Integer deviceDefault = this.deviceDefault;
        return deviceDefault != null ? Math.min(deviceDefault, MAX_COMMAND_TRANSITION) : 0;
    }

    static ClusterCommand moveToLevelWithOnOff(int level, @Nullable Integer transitionTime,
            LevelControlCluster.@Nullable OptionsBitmap options) {
        ClusterCommand command = LevelControlCluster.moveToLevelWithOnOff(level, transitionTime, options, options);
        if (transitionTime == null) {
            // Null means use the device default. The builder drops nulls, but matter.js requires the field.
            command.args.put("transitionTime", JsonNull.INSTANCE);
        }
        return command;
    }

    void onEvent(AttributeChangedMessage message) {
        if (LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME.equals(message.path.attributeName)
                && message.value instanceof Number number) {
            deviceDefault = number.intValue();
            updateDefaultConfig(number.intValue());
        }
    }

    private ChannelUID channelUID() {
        return new ChannelUID(new ChannelGroupUID(handler.getThing().getUID(), String.valueOf(endpointNumber)),
                channelId);
    }

    private Configuration channelConfig() {
        Channel channel = handler.getThing().getChannel(channelUID());
        return channel != null ? channel.getConfiguration() : new Configuration();
    }

    private void restoreDefaultConfig() {
        Integer deviceDefault = this.deviceDefault;
        if (deviceDefault != null) {
            handler.updateChannelConfiguration(channelUID(),
                    Map.of(CONFIG_CHANNEL_DEFAULT_TRANSITION_TIME, toSeconds(deviceDefault)));
        }
    }

    private void updateDefaultConfig(int tenths) {
        BigDecimal seconds = toSeconds(tenths);
        Object stored = channelConfig().get(CONFIG_CHANNEL_DEFAULT_TRANSITION_TIME);
        if (!(stored instanceof BigDecimal decimal && decimal.compareTo(seconds) == 0)) {
            handler.updateChannelConfiguration(channelUID(), Map.of(CONFIG_CHANNEL_DEFAULT_TRANSITION_TIME, seconds));
        }
    }

    private ConfigDescriptionParameter defaultParameter() {
        return ConfigDescriptionParameterBuilder.create(CONFIG_CHANNEL_DEFAULT_TRANSITION_TIME, Type.DECIMAL)
                .withLabel(handler.getTranslation(CONFIG_LABEL_DEFAULT_TRANSITION_TIME))
                .withDescription(handler.getTranslation(CONFIG_DESC_DEFAULT_TRANSITION_TIME))
                .withMinimum(BigDecimal.ZERO).withMaximum(toSeconds(MAX_DEVICE_DEFAULT))
                .withStepSize(BigDecimal.valueOf(1, 1)).withUnit("s").build();
    }

    /**
     * @return tenths of a second, or null if invalid
     */
    private static @Nullable Integer toTenths(Object seconds, int max) {
        BigDecimal tenths;
        try {
            tenths = new BigDecimal(seconds.toString().trim()).movePointRight(1).setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
        if (tenths.signum() < 0 || tenths.compareTo(BigDecimal.valueOf(max)) > 0) {
            return null;
        }
        return tenths.intValue();
    }

    private static BigDecimal toSeconds(int tenths) {
        return BigDecimal.valueOf(tenths, 1);
    }
}
