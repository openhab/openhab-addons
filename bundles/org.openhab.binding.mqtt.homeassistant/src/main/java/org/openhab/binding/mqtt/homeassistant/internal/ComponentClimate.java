/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ChannelStateUpdateListenerProxy;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * A MQTT climate component, following the https://www.home-assistant.io/components/climate.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentClimate extends AbstractComponent<ComponentClimate.ChannelConfiguration> {
    private static final String ACTION_CH_ID = "action";
    private static final String AUX_CH_ID = "aux";
    private static final String AWAY_MODE_CH_ID = "awayMode";
    private static final String CURRENT_TEMPERATURE_CH_ID = "currentTemperature";
    private static final String FAN_MODE_CH_ID = "fanMode";
    private static final String HOLD_CH_ID = "hold";
    private static final String MODE_CH_ID = "mode";
    private static final String SWING_CH_ID = "swing";
    private static final String TEMPERATURE_CH_ID = "temperature";
    private static final String TEMPERATURE_HIGH_CH_ID = "temperatureHigh";
    private static final String TEMPERATURE_LOW_CH_ID = "temperatureLow";
    private static final String POWER_CH_ID = "power";

    private static final String CELSIUM = "C";
    private static final String FAHRENHEIT = "F";
    private static final float DEFAULT_CELSIUM_PRECISION = 0.1f;
    private static final float DEFAULT_FAHRENHEIT_PRECISION = 1f;

    private static final String ACTION_OFF = "off";
    private static final State ACTION_OFF_STATE = new StringType(ACTION_OFF);
    private static final List<String> ACTION_MODES = List.of(ACTION_OFF, "heating", "cooling", "drying", "idle", "fan");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT HVAC");
        }

        protected @Nullable String action_template;
        protected @Nullable String action_topic;

        protected @Nullable String aux_command_topic;
        protected @Nullable String aux_state_template;
        protected @Nullable String aux_state_topic;

        protected @Nullable String away_mode_command_topic;
        protected @Nullable String away_mode_state_template;
        protected @Nullable String away_mode_state_topic;

        protected @Nullable String current_temperature_template;
        protected @Nullable String current_temperature_topic;

        protected @Nullable String fan_mode_command_template;
        protected @Nullable String fan_mode_command_topic;
        protected @Nullable String fan_mode_state_template;
        protected @Nullable String fan_mode_state_topic;
        protected List<String> fan_modes = Arrays.asList("auto", "low", "medium", "high");

        protected @Nullable String hold_command_template;
        protected @Nullable String hold_command_topic;
        protected @Nullable String hold_state_template;
        protected @Nullable String hold_state_topic;
        protected @Nullable List<String> hold_modes; // Are there default modes? Now the channel will be ignored without
                                                     // hold modes.

        protected @Nullable String json_attributes_template; // Attributes are not supported yet
        protected @Nullable String json_attributes_topic;

        protected @Nullable String mode_command_template;
        protected @Nullable String mode_command_topic;
        protected @Nullable String mode_state_template;
        protected @Nullable String mode_state_topic;
        protected List<String> modes = Arrays.asList("auto", "off", "cool", "heat", "dry", "fan_only");

        protected @Nullable String swing_command_template;
        protected @Nullable String swing_command_topic;
        protected @Nullable String swing_state_template;
        protected @Nullable String swing_state_topic;
        protected List<String> swing_modes = Arrays.asList("on", "off");

        protected @Nullable String temperature_command_template;
        protected @Nullable String temperature_command_topic;
        protected @Nullable String temperature_state_template;
        protected @Nullable String temperature_state_topic;

        protected @Nullable String temperature_high_command_template;
        protected @Nullable String temperature_high_command_topic;
        protected @Nullable String temperature_high_state_template;
        protected @Nullable String temperature_high_state_topic;

        protected @Nullable String temperature_low_command_template;
        protected @Nullable String temperature_low_command_topic;
        protected @Nullable String temperature_low_state_template;
        protected @Nullable String temperature_low_state_topic;

        protected @Nullable String power_command_topic;

        protected Integer initial = 21;
        protected @Nullable Float max_temp;
        protected @Nullable Float min_temp;
        protected String temperature_unit = CELSIUM; // System unit by default
        protected Float temp_step = 1f;
        protected @Nullable Float precision;
        protected Boolean send_if_off = true;
    }

    public ComponentClimate(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        @Nullable
        BigDecimal minTemp = channelConfiguration.min_temp != null ? BigDecimal.valueOf(channelConfiguration.min_temp)
                : null;
        @Nullable
        BigDecimal maxTemp = channelConfiguration.max_temp != null ? BigDecimal.valueOf(channelConfiguration.max_temp)
                : null;
        float precision = channelConfiguration.precision != null ? channelConfiguration.precision
                : (FAHRENHEIT.equals(channelConfiguration.temperature_unit) ? DEFAULT_FAHRENHEIT_PRECISION
                        : DEFAULT_CELSIUM_PRECISION);

        @Nullable
        CChannel actionChannel = buildOptionalChannel(ACTION_CH_ID, new TextValue(ACTION_MODES.toArray(new String[0])),
                componentConfiguration.getUpdateListener(), null, null, channelConfiguration.action_template,
                channelConfiguration.action_topic);

        ChannelStateUpdateListener updateListener = getListener(channelConfiguration.send_if_off, actionChannel,
                componentConfiguration.getUpdateListener());

        buildOptionalChannel(AUX_CH_ID, new OnOffValue(), updateListener, null, channelConfiguration.aux_command_topic,
                channelConfiguration.aux_state_template, channelConfiguration.aux_state_topic);

        buildOptionalChannel(AWAY_MODE_CH_ID, new OnOffValue(), updateListener, null,
                channelConfiguration.away_mode_command_topic, channelConfiguration.away_mode_state_template,
                channelConfiguration.away_mode_state_topic);

        buildOptionalChannel(CURRENT_TEMPERATURE_CH_ID,
                new NumberValue(minTemp, maxTemp, BigDecimal.valueOf(precision), channelConfiguration.temperature_unit),
                updateListener, null, null, channelConfiguration.current_temperature_template,
                channelConfiguration.current_temperature_topic);

        buildOptionalChannel(FAN_MODE_CH_ID, new TextValue(channelConfiguration.fan_modes.toArray(new String[0])),
                updateListener, channelConfiguration.fan_mode_command_template,
                channelConfiguration.fan_mode_command_topic, channelConfiguration.fan_mode_state_template,
                channelConfiguration.fan_mode_state_topic);

        if (channelConfiguration.hold_modes != null && !channelConfiguration.hold_modes.isEmpty()) {
            buildOptionalChannel(HOLD_CH_ID, new TextValue(channelConfiguration.hold_modes.toArray(new String[0])),
                    updateListener, channelConfiguration.hold_command_template, channelConfiguration.hold_command_topic,
                    channelConfiguration.hold_state_template, channelConfiguration.hold_state_topic);
        }

        buildOptionalChannel(MODE_CH_ID, new TextValue(channelConfiguration.modes.toArray(new String[0])),
                updateListener, channelConfiguration.mode_command_template, channelConfiguration.mode_command_topic,
                channelConfiguration.mode_state_template, channelConfiguration.mode_state_topic);

        buildOptionalChannel(SWING_CH_ID, new TextValue(channelConfiguration.swing_modes.toArray(new String[0])),
                updateListener, channelConfiguration.swing_command_template, channelConfiguration.swing_command_topic,
                channelConfiguration.swing_state_template, channelConfiguration.swing_state_topic);

        buildOptionalChannel(TEMPERATURE_CH_ID,
                new NumberValue(minTemp, maxTemp, BigDecimal.valueOf(channelConfiguration.temp_step),
                        channelConfiguration.temperature_unit),
                updateListener, channelConfiguration.temperature_command_template,
                channelConfiguration.temperature_command_topic, channelConfiguration.temperature_state_template,
                channelConfiguration.temperature_state_topic);

        buildOptionalChannel(TEMPERATURE_HIGH_CH_ID,
                new NumberValue(minTemp, maxTemp, BigDecimal.valueOf(channelConfiguration.temp_step),
                        channelConfiguration.temperature_unit),
                updateListener, channelConfiguration.temperature_high_command_template,
                channelConfiguration.temperature_high_command_topic,
                channelConfiguration.temperature_high_state_template,
                channelConfiguration.temperature_high_state_topic);

        buildOptionalChannel(TEMPERATURE_LOW_CH_ID,
                new NumberValue(minTemp, maxTemp, BigDecimal.valueOf(channelConfiguration.temp_step),
                        channelConfiguration.temperature_unit),
                updateListener, channelConfiguration.temperature_low_command_template,
                channelConfiguration.temperature_low_command_topic, channelConfiguration.temperature_low_state_template,
                channelConfiguration.temperature_low_state_topic);

        buildOptionalChannel(POWER_CH_ID, new OnOffValue(), updateListener, null,
                channelConfiguration.power_command_topic, null, null);
    }

    @Nullable
    private CChannel buildOptionalChannel(String channelId, Value valueState,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable String commandTemplate,
            @Nullable String commandTopic, @Nullable String stateTemplate, @Nullable String stateTopic) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, valueState, channelConfiguration.name, channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, channelConfiguration.value_template)
                    .commandTopic(commandTopic, channelConfiguration.retain, channelConfiguration.qos, commandTemplate)
                    .build();
        }
        return null;
    }

    private ChannelStateUpdateListener getListener(boolean sendIfOff, @Nullable CChannel actionChannel,
            ChannelStateUpdateListener original) {
        if (!sendIfOff && actionChannel != null) {
            return new ChannelStateUpdateListenerProxy(original) {
                @Override
                public void postChannelCommand(@NonNull ChannelUID channelUID, @NonNull Command value) {
                    if (isOff()) {
                        return; // Do not send command if action is off
                    }
                    super.postChannelCommand(channelUID, value);
                }

                @Override
                public void triggerChannel(@NonNull ChannelUID channelUID, @NonNull String eventPayload) {
                    if (isOff()) {
                        return; // Do not trigger if action is off
                    }
                    super.triggerChannel(channelUID, eventPayload);
                }

                private boolean isOff() {
                    Value val = actionChannel.getState().getCache();
                    return ACTION_OFF_STATE.equals(val.getChannelState());
                }
            };
        }
        return original;
    }
}
