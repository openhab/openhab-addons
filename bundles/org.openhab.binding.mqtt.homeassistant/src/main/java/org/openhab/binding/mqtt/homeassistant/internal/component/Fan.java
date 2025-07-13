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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * A MQTT Fan component, following the https://www.home-assistant.io/components/fan.mqtt/ specification.
 *
 * Only ON/OFF is supported so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Fan extends AbstractComponent<Fan.Configuration> implements ChannelStateUpdateListener {
    public static final String SWITCH_CHANNEL_ID = "switch";
    public static final String SPEED_CHANNEL_ID = "speed";
    public static final String PRESET_MODE_CHANNEL_ID = "preset-mode";
    public static final String OSCILLATION_CHANNEL_ID = "oscillation";
    public static final String DIRECTION_CHANNEL_ID = "direction";

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Fan");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        @Nullable
        String getDirectionCommandTopic() {
            return getOptionalString("direction_command_topic");
        }

        @Nullable
        Value getDirectionCommandTemplate() {
            return getOptionalValue("direction_command_template");
        }

        @Nullable
        String getDirectionStateTopic() {
            return getOptionalString("direction_state_topic");
        }

        @Nullable
        Value getDirectionValueTemplate() {
            return getOptionalValue("direction_value_template");
        }

        @Nullable
        String getOscillationCommandTopic() {
            return getOptionalString("oscillation_command_topic");
        }

        @Nullable
        Value getOscillationCommandTemplate() {
            return getOptionalValue("oscillation_command_template");
        }

        @Nullable
        String getOscillationStateTopic() {
            return getOptionalString("oscillation_state_topic");
        }

        @Nullable
        Value getOscillationValueTemplate() {
            return getOptionalValue("oscillation_value_template");
        }

        @Nullable
        String getPercentageCommandTopic() {
            return getOptionalString("percentage_command_topic");
        }

        @Nullable
        Value getPercentageCommandTemplate() {
            return getOptionalValue("percentage_command_template");
        }

        @Nullable
        String getPercentageStateTopic() {
            return getOptionalString("percentage_state_topic");
        }

        @Nullable
        Value getPercentageValueTemplate() {
            return getOptionalValue("percentage_value_template");
        }

        @Nullable
        String getPresetModeCommandTopic() {
            return getOptionalString("preset_mode_command_topic");
        }

        List<String> getPresetModes() {
            return getStringList("preset_modes");
        }

        @Nullable
        Value getPresetModeCommandTemplate() {
            return getOptionalValue("preset_mode_command_template");
        }

        @Nullable
        String getPresetModeStateTopic() {
            return getOptionalString("preset_mode_state_topic");
        }

        @Nullable
        Value getPresetModeValueTemplate() {
            return getOptionalValue("preset_mode_value_template");
        }

        int getSpeedRangeMin() {
            return getInt("speed_range_min");
        }

        int getSpeedRangeMax() {
            return getInt("speed_range_max");
        }

        String getPayloadResetPercentage() {
            return getString("payload_reset_percentage");
        }

        String getPayloadResetPresetMode() {
            return getString("payload_reset_preset_mode");
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        String getPayloadOscillationOff() {
            return getString("payload_oscillation_off");
        }

        String getPayloadOscillationOn() {
            return getString("payload_oscillation_on");
        }

        @Nullable
        Value getStateValueTemplate() {
            return getOptionalValue("state_value_template");
        }
    }

    private final OnOffValue onOffValue;
    private final PercentageValue speedValue;
    private State rawSpeedState;
    private final ComponentChannel onOffChannel;
    private final @Nullable ComponentChannel speedChannel;
    private final ComponentChannel primaryChannel;
    private final ChannelStateUpdateListener channelStateUpdateListener;

    public Fan(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);
        this.channelStateUpdateListener = componentContext.getUpdateListener();

        boolean optimistic = config.isOptimistic();
        String payloadOn = config.getPayloadOn();
        String payloadOff = config.getPayloadOff();
        onOffValue = new OnOffValue(payloadOn, payloadOff);
        String percentageCommandTopic = config.getPercentageCommandTopic();
        ChannelStateUpdateListener onOffListener = percentageCommandTopic == null ? componentContext.getUpdateListener()
                : this;
        onOffChannel = buildChannel(SWITCH_CHANNEL_ID, ComponentChannelType.SWITCH, onOffValue, "On/Off State",
                onOffListener).stateTopic(config.getStateTopic(), config.getStateValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .inferOptimistic(optimistic).build(percentageCommandTopic == null);

        rawSpeedState = UnDefType.NULL;

        speedValue = new PercentageValue(BigDecimal.valueOf(config.getSpeedRangeMin() - 1),
                BigDecimal.valueOf(config.getSpeedRangeMax()), null, payloadOn, payloadOff, FORMAT_INTEGER);

        if (percentageCommandTopic != null) {
            hiddenChannels.add(onOffChannel);
            primaryChannel = speedChannel = buildChannel(SPEED_CHANNEL_ID, ComponentChannelType.DIMMER, speedValue,
                    "Speed", this)
                    .stateTopic(config.getPercentageStateTopic(), config.getPercentageValueTemplate())
                    .commandTopic(percentageCommandTopic, config.isRetain(), config.getQos(),
                            config.getPercentageCommandTemplate())
                    .parseCommandValueAsInteger(true).inferOptimistic(optimistic)
                    .commandFilter(this::handlePercentageCommand).build();
        } else {
            primaryChannel = onOffChannel;
            speedChannel = null;
        }

        String presetModeCommandTopic = config.getPresetModeCommandTopic();
        if (presetModeCommandTopic != null) {
            TextValue presetModeValue = new TextValue(config.getPresetModes().toArray(new String[0]));
            presetModeValue.setNullValue(config.getPayloadResetPresetMode());
            buildChannel(PRESET_MODE_CHANNEL_ID, ComponentChannelType.STRING, presetModeValue, "Preset Mode",
                    componentContext.getUpdateListener())
                    .stateTopic(config.getPresetModeStateTopic(), config.getPresetModeValueTemplate())
                    .commandTopic(presetModeCommandTopic, config.isRetain(), config.getQos(),
                            config.getPresetModeCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        String oscillationCommandTopic = config.getOscillationCommandTopic();
        if (oscillationCommandTopic != null) {
            OnOffValue oscillationValue = new OnOffValue(config.getPayloadOscillationOn(),
                    config.getPayloadOscillationOff());
            buildChannel(OSCILLATION_CHANNEL_ID, ComponentChannelType.SWITCH, oscillationValue, "Oscillation",
                    componentContext.getUpdateListener())
                    .stateTopic(config.getOscillationStateTopic(), config.getOscillationValueTemplate())
                    .commandTopic(oscillationCommandTopic, config.isRetain(), config.getQos(),
                            config.getOscillationCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        String directionCommandTopic = config.getDirectionCommandTopic();
        if (directionCommandTopic != null) {
            TextValue directionValue = new TextValue(new String[] { "forward", "backward" });
            buildChannel(DIRECTION_CHANNEL_ID, ComponentChannelType.STRING, directionValue, "Direction",
                    componentContext.getUpdateListener())
                    .stateTopic(config.getDirectionStateTopic(), config.getDirectionValueTemplate())
                    .commandTopic(directionCommandTopic, config.isRetain(), config.getQos(),
                            config.getDirectionCommandTemplate())
                    .inferOptimistic(optimistic).build();
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
