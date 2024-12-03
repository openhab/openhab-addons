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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Fan component, following the https://www.home-assistant.io/components/fan.mqtt/ specification.
 *
 * Only ON/OFF is supported so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Fan extends AbstractComponent<Fan.ChannelConfiguration> implements ChannelStateUpdateListener {
    public static final String SWITCH_CHANNEL_ID = "switch";
    public static final String SWITCH_CHANNEL_ID_DEPRECATED = "fan";
    public static final String SPEED_CHANNEL_ID = "speed";
    public static final String PRESET_MODE_CHANNEL_ID = "preset-mode";
    public static final String OSCILLATION_CHANNEL_ID = "oscillation";
    public static final String DIRECTION_CHANNEL_ID = "direction";

    private static final BigDecimal BIG_DECIMAL_HUNDRED = new BigDecimal(100);
    private static final String FORMAT_INTEGER = "%.0f";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Fan");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("state_value_template")
        protected @Nullable String stateValueTemplate;
        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("command_topic")
        protected String commandTopic = "";
        @SerializedName("direction_command_template")
        protected @Nullable String directionCommandTemplate;
        @SerializedName("direction_command_topic")
        protected @Nullable String directionCommandTopic;
        @SerializedName("direction_state_topic")
        protected @Nullable String directionStateTopic;
        @SerializedName("direction_value_template")
        protected @Nullable String directionValueTemplate;
        @SerializedName("oscillation_command_template")
        protected @Nullable String oscillationCommandTemplate;
        @SerializedName("oscillation_command_topic")
        protected @Nullable String oscillationCommandTopic;
        @SerializedName("oscillation_state_topic")
        protected @Nullable String oscillationStateTopic;
        @SerializedName("oscillation_value_template")
        protected @Nullable String oscillationValueTemplate;
        @SerializedName("payload_oscillation_off")
        protected String payloadOscillationOff = "oscillate_off";
        @SerializedName("payload_oscillation_on")
        protected String payloadOscillationOn = "oscillate_on";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";
        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_reset_percentage")
        protected String payloadResetPercentage = "None";
        @SerializedName("payload_reset_preset_mode")
        protected String payloadResetPresetMode = "None";
        @SerializedName("percentage_command_template")
        protected @Nullable String percentageCommandTemplate;
        @SerializedName("percentage_command_topic")
        protected @Nullable String percentageCommandTopic;
        @SerializedName("percentage_state_topic")
        protected @Nullable String percentageStateTopic;
        @SerializedName("percentage_value_template")
        protected @Nullable String percentageValueTemplate;
        @SerializedName("preset_mode_command_template")
        protected @Nullable String presetModeCommandTemplate;
        @SerializedName("preset_mode_command_topic")
        protected @Nullable String presetModeCommandTopic;
        @SerializedName("preset_mode_state_topic")
        protected @Nullable String presetModeStateTopic;
        @SerializedName("preset_mode_value_template")
        protected @Nullable String presetModeValueTemplate;
        @SerializedName("preset_modes")
        protected @Nullable List<String> presetModes;
        @SerializedName("speed_range_max")
        protected int speedRangeMax = 100;
        @SerializedName("speed_range_min")
        protected int speedRangeMin = 1;
    }

    private final OnOffValue onOffValue;
    private final PercentageValue speedValue;
    private State rawSpeedState;
    private final ComponentChannel onOffChannel;
    private final @Nullable ComponentChannel speedChannel;
    private final ComponentChannel primaryChannel;
    private final ChannelStateUpdateListener channelStateUpdateListener;

    public Fan(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);
        this.channelStateUpdateListener = componentConfiguration.getUpdateListener();

        onOffValue = new OnOffValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff);
        ChannelStateUpdateListener onOffListener = channelConfiguration.percentageCommandTopic == null
                ? componentConfiguration.getUpdateListener()
                : this;
        onOffChannel = buildChannel(newStyleChannels ? SWITCH_CHANNEL_ID : SWITCH_CHANNEL_ID_DEPRECATED,
                ComponentChannelType.SWITCH, onOffValue, "On/Off State", onOffListener)
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.stateValueTemplate)
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .inferOptimistic(channelConfiguration.optimistic)
                .build(channelConfiguration.percentageCommandTopic == null);

        rawSpeedState = UnDefType.NULL;

        speedValue = new PercentageValue(BigDecimal.valueOf(channelConfiguration.speedRangeMin - 1),
                BigDecimal.valueOf(channelConfiguration.speedRangeMax), null, channelConfiguration.payloadOn,
                channelConfiguration.payloadOff, FORMAT_INTEGER);

        if (channelConfiguration.percentageCommandTopic != null) {
            hiddenChannels.add(onOffChannel);
            primaryChannel = speedChannel = buildChannel(SPEED_CHANNEL_ID, ComponentChannelType.DIMMER, speedValue,
                    "Speed", this)
                    .stateTopic(channelConfiguration.percentageStateTopic, channelConfiguration.percentageValueTemplate)
                    .commandTopic(channelConfiguration.percentageCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.percentageCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).commandFilter(this::handlePercentageCommand)
                    .build();
        } else {
            primaryChannel = onOffChannel;
            speedChannel = null;
        }

        List<String> presetModes = channelConfiguration.presetModes;
        if (presetModes != null) {
            TextValue presetModeValue = new TextValue(presetModes.toArray(new String[0]));
            presetModeValue.setNullValue(channelConfiguration.payloadResetPresetMode);
            buildChannel(PRESET_MODE_CHANNEL_ID, ComponentChannelType.STRING, presetModeValue, "Preset Mode",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.presetModeStateTopic, channelConfiguration.presetModeValueTemplate)
                    .commandTopic(channelConfiguration.presetModeCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.presetModeCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.oscillationCommandTopic != null) {
            OnOffValue oscillationValue = new OnOffValue(channelConfiguration.payloadOscillationOn,
                    channelConfiguration.payloadOscillationOff);
            buildChannel(OSCILLATION_CHANNEL_ID, ComponentChannelType.SWITCH, oscillationValue, "Oscillation",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.oscillationStateTopic,
                            channelConfiguration.oscillationValueTemplate)
                    .commandTopic(channelConfiguration.oscillationCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.oscillationCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.directionCommandTopic != null) {
            TextValue directionValue = new TextValue(new String[] { "forward", "backward" });
            buildChannel(DIRECTION_CHANNEL_ID, ComponentChannelType.STRING, directionValue, "Direction",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.directionStateTopic, channelConfiguration.directionValueTemplate)
                    .commandTopic(channelConfiguration.directionCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.directionCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        finalizeChannels();
    }

    private boolean handlePercentageCommand(Command command) {
        // ON/OFF go to the regular command topic, not the percentage topic
        if (command.equals(OnOffType.ON) || command.equals(OnOffType.OFF)) {
            onOffChannel.getState().publishValue(command);
            return false;
        }
        return true;
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        if (onOffChannel.getChannel().getUID().equals(channel)) {
            if (rawSpeedState instanceof UnDefType && state.equals(OnOffType.ON)) {
                // Assume full on if we don't yet know the actual speed
                state = PercentType.HUNDRED;
            } else if (state.equals(OnOffType.OFF)) {
                state = PercentType.ZERO;
            } else {
                state = rawSpeedState;
            }
        } else if (Objects.requireNonNull(speedChannel).getChannel().getUID().equals(channel)) {
            rawSpeedState = state;
            if (onOffValue.getChannelState().equals(OnOffType.OFF)) {
                // Don't pass on percentage values while the fan is off
                state = PercentType.ZERO;
            }
        }
        speedValue.update(state);
        channelStateUpdateListener.updateChannelState(primaryChannel.getChannel().getUID(), state);
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }
}
