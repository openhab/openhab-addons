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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
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
    // Color Control commands accept at most 65534
    private static final int MAX_COLOR_TRANSITION = 65534;

    private final Logger logger = LoggerFactory.getLogger(LevelTransition.class);
    private final MatterBaseThingHandler handler;
    private final int endpointNumber;
    private final String defaultKey;
    private volatile @Nullable Integer deviceDefault;

    LevelTransition(MatterBaseThingHandler handler, int endpointNumber) {
        this.handler = handler;
        this.endpointNumber = endpointNumber;
        this.defaultKey = CONFIG_LEVEL_DEFAULT_TRANSITION_TIME + "-" + endpointNumber;
    }

    void initialize(@Nullable LevelControlCluster cluster) {
        Integer deviceDefault = cluster != null ? cluster.onOffTransitionTime : null;
        this.deviceDefault = deviceDefault;
        if (deviceDefault != null) {
            updateConfigDescription();
            updateDefaultConfig(deviceDefault);
        }
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
        Integer tenths = toTenths(value, MAX_COLOR_TRANSITION);
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
        return deviceDefault != null ? Math.min(deviceDefault, MAX_COLOR_TRANSITION) : 0;
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

    void handleConfigurationUpdate(Configuration config) {
        Integer current = deviceDefault;
        Object value = config.get(defaultKey);
        if (current == null || value == null) {
            return;
        }
        Integer configured = toTenths(value, MAX_DEVICE_DEFAULT);
        if (configured == null) {
            logger.debug("Ignoring invalid default transition time {} for endpoint {}", value, endpointNumber);
            restoreDefaultConfig();
        } else if (!configured.equals(current)) {
            logger.debug("Updating default transition time of endpoint {} from {} to {}", endpointNumber, current,
                    configured);
            // deviceDefault updates when the device reports the new value
            handler.writeAttribute(endpointNumber, LevelControlCluster.CLUSTER_NAME,
                    LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME, String.valueOf(configured))
                    .exceptionally(e -> {
                        logger.debug("Failed to update default transition time: {}", e.getMessage());
                        restoreDefaultConfig();
                        return Void.TYPE.cast(null);
                    });
        }
    }

    private void restoreDefaultConfig() {
        Integer deviceDefault = this.deviceDefault;
        if (deviceDefault != null) {
            handler.updateConfiguration(Map.of(defaultKey, toSeconds(deviceDefault)));
        }
    }

    private void updateDefaultConfig(int tenths) {
        BigDecimal seconds = toSeconds(tenths);
        Object stored = handler.getThing().getConfiguration().get(defaultKey);
        if (!(stored instanceof BigDecimal decimal && decimal.compareTo(seconds) == 0)) {
            handler.updateConfiguration(Map.of(defaultKey, seconds));
        }
    }

    private void updateConfigDescription() {
        String groupName = CONFIG_GROUP_LEVEL_TRANSITION + "-" + endpointNumber;
        ConfigDescriptionParameter param = ConfigDescriptionParameterBuilder.create(defaultKey, Type.DECIMAL)
                .withLabel(handler.getTranslation(CONFIG_LABEL_LEVEL_DEFAULT_TRANSITION_TIME))
                .withDescription(handler.getTranslation(CONFIG_DESC_LEVEL_DEFAULT_TRANSITION_TIME))
                .withGroupName(groupName).withMinimum(BigDecimal.ZERO).withMaximum(toSeconds(MAX_DEVICE_DEFAULT))
                .withStepSize(BigDecimal.valueOf(1, 1)).withUnit("s").build();

        handler.addConfigDescription(groupName, ConfigDescriptionBuilder.create(handler.getConfigDescriptionURI())
                .withParameter(param)
                .withParameterGroup(ConfigDescriptionParameterGroupBuilder.create(groupName)
                        .withLabel(handler.getTranslation(CONFIG_LABEL_LEVEL_TRANSITION, endpointNumber)).build())
                .build());
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
