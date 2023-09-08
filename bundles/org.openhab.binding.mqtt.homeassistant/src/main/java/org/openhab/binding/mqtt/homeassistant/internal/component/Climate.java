/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT climate component, following the https://www.home-assistant.io/components/climate.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 * @author Anton Kharuzhy - Implementation
 */
@NonNullByDefault
public class Climate extends AbstractComponent<Climate.ChannelConfiguration> {
    public static final String ACTION_CH_ID = "action";
    public static final String AUX_CH_ID = "aux";
    public static final String AWAY_MODE_CH_ID = "awayMode";
    public static final String CURRENT_TEMPERATURE_CH_ID = "currentTemperature";
    public static final String FAN_MODE_CH_ID = "fanMode";
    public static final String HOLD_CH_ID = "hold";
    public static final String MODE_CH_ID = "mode";
    public static final String SWING_CH_ID = "swing";
    public static final String TEMPERATURE_CH_ID = "temperature";
    public static final String TEMPERATURE_HIGH_CH_ID = "temperatureHigh";
    public static final String TEMPERATURE_LOW_CH_ID = "temperatureLow";
    public static final String POWER_CH_ID = "power";

    public enum TemperatureUnit {
        @SerializedName("C")
        CELSIUS(SIUnits.CELSIUS, new BigDecimal("0.1")),
        @SerializedName("F")
        FAHRENHEIT(ImperialUnits.FAHRENHEIT, BigDecimal.ONE);

        private final Unit<Temperature> unit;
        private final BigDecimal defaultPrecision;

        TemperatureUnit(Unit<Temperature> unit, BigDecimal defaultPrecision) {
            this.unit = unit;
            this.defaultPrecision = defaultPrecision;
        }

        public Unit<Temperature> getUnit() {
            return unit;
        }

        public BigDecimal getDefaultPrecision() {
            return defaultPrecision;
        }
    }

    private static final String ACTION_OFF = "off";
    private static final State ACTION_OFF_STATE = new StringType(ACTION_OFF);
    private static final List<String> ACTION_MODES = List.of(ACTION_OFF, "heating", "cooling", "drying", "idle", "fan");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT HVAC");
        }

        @SerializedName("action_template")
        protected @Nullable String actionTemplate;
        @SerializedName("action_topic")
        protected @Nullable String actionTopic;

        @SerializedName("aux_command_topic")
        protected @Nullable String auxCommandTopic;
        @SerializedName("aux_state_template")
        protected @Nullable String auxStateTemplate;
        @SerializedName("aux_state_topic")
        protected @Nullable String auxStateTopic;

        @SerializedName("away_mode_command_topic")
        protected @Nullable String awayModeCommandTopic;
        @SerializedName("away_mode_state_template")
        protected @Nullable String awayModeStateTemplate;
        @SerializedName("away_mode_state_topic")
        protected @Nullable String awayModeStateTopic;

        @SerializedName("current_temperature_template")
        protected @Nullable String currentTemperatureTemplate;
        @SerializedName("current_temperature_topic")
        protected @Nullable String currentTemperatureTopic;

        @SerializedName("fan_mode_command_template")
        protected @Nullable String fanModeCommandTemplate;
        @SerializedName("fan_mode_command_topic")
        protected @Nullable String fanModeCommandTopic;
        @SerializedName("fan_mode_state_template")
        protected @Nullable String fanModeStateTemplate;
        @SerializedName("fan_mode_state_topic")
        protected @Nullable String fanModeStateTopic;
        @SerializedName("fan_modes")
        protected List<String> fanModes = Arrays.asList("auto", "low", "medium", "high");

        @SerializedName("hold_command_template")
        protected @Nullable String holdCommandTemplate;
        @SerializedName("hold_command_topic")
        protected @Nullable String holdCommandTopic;
        @SerializedName("hold_state_template")
        protected @Nullable String holdStateTemplate;
        @SerializedName("hold_state_topic")
        protected @Nullable String holdStateTopic;
        @SerializedName("hold_modes")
        protected @Nullable List<String> holdModes; // Are there default modes? Now the channel will be ignored without
                                                    // hold modes.

        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate; // Attributes are not supported yet
        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;

        @SerializedName("mode_command_template")
        protected @Nullable String modeCommandTemplate;
        @SerializedName("mode_command_topic")
        protected @Nullable String modeCommandTopic;
        @SerializedName("mode_state_template")
        protected @Nullable String modeStateTemplate;
        @SerializedName("mode_state_topic")
        protected @Nullable String modeStateTopic;
        protected List<String> modes = Arrays.asList("auto", "off", "cool", "heat", "dry", "fan_only");

        @SerializedName("swing_command_template")
        protected @Nullable String swingCommandTemplate;
        @SerializedName("swing_command_topic")
        protected @Nullable String swingCommandTopic;
        @SerializedName("swing_state_template")
        protected @Nullable String swingStateTemplate;
        @SerializedName("swing_state_topic")
        protected @Nullable String swingStateTopic;
        @SerializedName("swing_modes")
        protected List<String> swingModes = Arrays.asList("on", "off");

        @SerializedName("temperature_command_template")
        protected @Nullable String temperatureCommandTemplate;
        @SerializedName("temperature_command_topic")
        protected @Nullable String temperatureCommandTopic;
        @SerializedName("temperature_state_template")
        protected @Nullable String temperatureStateTemplate;
        @SerializedName("temperature_state_topic")
        protected @Nullable String temperatureStateTopic;

        @SerializedName("temperature_high_command_template")
        protected @Nullable String temperatureHighCommandTemplate;
        @SerializedName("temperature_high_command_topic")
        protected @Nullable String temperatureHighCommandTopic;
        @SerializedName("temperature_high_state_template")
        protected @Nullable String temperatureHighStateTemplate;
        @SerializedName("temperature_high_state_topic")
        protected @Nullable String temperatureHighStateTopic;

        @SerializedName("temperature_low_command_template")
        protected @Nullable String temperatureLowCommandTemplate;
        @SerializedName("temperature_low_command_topic")
        protected @Nullable String temperatureLowCommandTopic;
        @SerializedName("temperature_low_state_template")
        protected @Nullable String temperatureLowStateTemplate;
        @SerializedName("temperature_low_state_topic")
        protected @Nullable String temperatureLowStateTopic;

        @SerializedName("power_command_topic")
        protected @Nullable String powerCommandTopic;

        protected Integer initial = 21;
        @SerializedName("max_temp")
        protected @Nullable BigDecimal maxTemp;
        @SerializedName("min_temp")
        protected @Nullable BigDecimal minTemp;
        @SerializedName("temperature_unit")
        protected TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS; // System unit by default
        @SerializedName("temp_step")
        protected BigDecimal tempStep = BigDecimal.ONE;
        protected @Nullable BigDecimal precision;
        @SerializedName("send_if_off")
        protected Boolean sendIfOff = true;
    }

    public Climate(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        BigDecimal precision = channelConfiguration.precision != null ? channelConfiguration.precision
                : channelConfiguration.temperatureUnit.getDefaultPrecision();
        final ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        ComponentChannel actionChannel = buildOptionalChannel(ACTION_CH_ID,
                new TextValue(ACTION_MODES.toArray(new String[0])), updateListener, null, null,
                channelConfiguration.actionTemplate, channelConfiguration.actionTopic, null);

        final Predicate<Command> commandFilter = channelConfiguration.sendIfOff ? null
                : getCommandFilter(actionChannel);

        buildOptionalChannel(AUX_CH_ID, new OnOffValue(), updateListener, null, channelConfiguration.auxCommandTopic,
                channelConfiguration.auxStateTemplate, channelConfiguration.auxStateTopic, commandFilter);

        buildOptionalChannel(AWAY_MODE_CH_ID, new OnOffValue(), updateListener, null,
                channelConfiguration.awayModeCommandTopic, channelConfiguration.awayModeStateTemplate,
                channelConfiguration.awayModeStateTopic, commandFilter);

        buildOptionalChannel(CURRENT_TEMPERATURE_CH_ID,
                new NumberValue(null, null, precision, channelConfiguration.temperatureUnit.getUnit()), updateListener,
                null, null, channelConfiguration.currentTemperatureTemplate,
                channelConfiguration.currentTemperatureTopic, commandFilter);

        buildOptionalChannel(FAN_MODE_CH_ID, new TextValue(channelConfiguration.fanModes.toArray(new String[0])),
                updateListener, channelConfiguration.fanModeCommandTemplate, channelConfiguration.fanModeCommandTopic,
                channelConfiguration.fanModeStateTemplate, channelConfiguration.fanModeStateTopic, commandFilter);

        List<String> holdModes = channelConfiguration.holdModes;
        if (holdModes != null && !holdModes.isEmpty()) {
            buildOptionalChannel(HOLD_CH_ID, new TextValue(holdModes.toArray(new String[0])), updateListener,
                    channelConfiguration.holdCommandTemplate, channelConfiguration.holdCommandTopic,
                    channelConfiguration.holdStateTemplate, channelConfiguration.holdStateTopic, commandFilter);
        }

        buildOptionalChannel(MODE_CH_ID, new TextValue(channelConfiguration.modes.toArray(new String[0])),
                updateListener, channelConfiguration.modeCommandTemplate, channelConfiguration.modeCommandTopic,
                channelConfiguration.modeStateTemplate, channelConfiguration.modeStateTopic, commandFilter);

        buildOptionalChannel(SWING_CH_ID, new TextValue(channelConfiguration.swingModes.toArray(new String[0])),
                updateListener, channelConfiguration.swingCommandTemplate, channelConfiguration.swingCommandTopic,
                channelConfiguration.swingStateTemplate, channelConfiguration.swingStateTopic, commandFilter);

        buildOptionalChannel(TEMPERATURE_CH_ID,
                new NumberValue(channelConfiguration.minTemp, channelConfiguration.maxTemp,
                        channelConfiguration.tempStep, channelConfiguration.temperatureUnit.getUnit()),
                updateListener, channelConfiguration.temperatureCommandTemplate,
                channelConfiguration.temperatureCommandTopic, channelConfiguration.temperatureStateTemplate,
                channelConfiguration.temperatureStateTopic, commandFilter);

        buildOptionalChannel(TEMPERATURE_HIGH_CH_ID,
                new NumberValue(channelConfiguration.minTemp, channelConfiguration.maxTemp,
                        channelConfiguration.tempStep, channelConfiguration.temperatureUnit.getUnit()),
                updateListener, channelConfiguration.temperatureHighCommandTemplate,
                channelConfiguration.temperatureHighCommandTopic, channelConfiguration.temperatureHighStateTemplate,
                channelConfiguration.temperatureHighStateTopic, commandFilter);

        buildOptionalChannel(TEMPERATURE_LOW_CH_ID,
                new NumberValue(channelConfiguration.minTemp, channelConfiguration.maxTemp,
                        channelConfiguration.tempStep, channelConfiguration.temperatureUnit.getUnit()),
                updateListener, channelConfiguration.temperatureLowCommandTemplate,
                channelConfiguration.temperatureLowCommandTopic, channelConfiguration.temperatureLowStateTemplate,
                channelConfiguration.temperatureLowStateTopic, commandFilter);

        buildOptionalChannel(POWER_CH_ID, new OnOffValue(), updateListener, null,
                channelConfiguration.powerCommandTopic, null, null, null);
    }

    @Nullable
    private ComponentChannel buildOptionalChannel(String channelId, Value valueState,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable String commandTemplate,
            @Nullable String commandTopic, @Nullable String stateTemplate, @Nullable String stateTopic,
            @Nullable Predicate<Command> commandFilter) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, valueState, channelConfiguration.getName(), channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, channelConfiguration.getValueTemplate())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos(),
                            commandTemplate)
                    .commandFilter(commandFilter).build();
        }
        return null;
    }

    private @Nullable Predicate<Command> getCommandFilter(@Nullable ComponentChannel actionChannel) {
        if (actionChannel == null) {
            return null;
        }
        final var val = actionChannel.getState().getCache();
        return command -> !ACTION_OFF_STATE.equals(val.getChannelState());
    }
}
