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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;

/**
 * A MQTT alarm control panel, following the https://www.home-assistant.io/components/alarm_control_panel.mqtt/
 * specification.
 *
 * The implemented provides three state-less switches (For disarming, arming@home, arming@away) and one alarm state
 * text.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class AlarmControlPanel extends AbstractComponent<AlarmControlPanel.Configuration> {
    public static final String STATE_CHANNEL_ID = "state";
    public static final String SWITCH_DISARM_CHANNEL_ID = "disarm";
    public static final String SWITCH_ARM_HOME_CHANNEL_ID = "armhome";
    public static final String SWITCH_ARM_AWAY_CHANNEL_ID = "armaway";

    public static final String FEATURE_ARM_HOME = "arm_home";
    public static final String FEATURE_ARM_AWAY = "arm_away";
    public static final String FEATURE_ARM_NIGHT = "arm_night";
    public static final String FEATURE_ARM_VACATION = "arm_vacation";
    public static final String FEATURE_ARM_CUSTOM_BYPASS = "arm_custom_bypass";
    public static final String FEATURE_TRIGGER = "trigger";

    public static final String PAYLOAD_ARM_HOME = "ARM_HOME";
    public static final String PAYLOAD_ARM_AWAY = "ARM_AWAY";
    public static final String PAYLOAD_ARM_NIGHT = "ARM_NIGHT";
    public static final String PAYLOAD_ARM_VACATION = "ARM_VACATION";
    public static final String PAYLOAD_ARM_CUSTOM_BYPASS = "ARM_CUSTOM_BYPASS";
    public static final String PAYLOAD_DISARM = "DISARM";
    public static final String PAYLOAD_TRIGGER = "TRIGGER";

    public static final String STATE_ARMED_AWAY = "armed_away";
    public static final String STATE_ARMED_CUSTOM_BYPASS = "armed_custom_bypass";
    public static final String STATE_ARMED_HOME = "armed_home";
    public static final String STATE_ARMED_NIGHT = "armed_night";
    public static final String STATE_ARMED_VACATION = "armed_vacation";
    public static final String STATE_ARMING = "arming";
    public static final String STATE_DISARMED = "disarmed";
    public static final String STATE_DISARMING = "disarming";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_TRIGGERED = "triggered";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_ARM_AWAY,
            "@text/command.alarm-control-panel.arm-away", PAYLOAD_ARM_HOME,
            "@text/command.alarm-control-panel.arm-home", PAYLOAD_ARM_NIGHT,
            "@text/command.alarm-control-panel.arm-night", PAYLOAD_ARM_VACATION,
            "@text/command.alarm-control-panel.arm-vacation", PAYLOAD_ARM_CUSTOM_BYPASS,
            "@text/command.alarm-control-panel.arm-custom-bypass", PAYLOAD_DISARM,
            "@text/command.alarm-control-panel.disarm", PAYLOAD_TRIGGER, "@text/command.alarm-control-panel.trigger");
    private static final Map<String, String> STATE_LABELS = Map.of(STATE_ARMED_AWAY,
            "@text/state.alarm-control-panel.armed-away", STATE_ARMED_CUSTOM_BYPASS,
            "@text/state.alarm-control-panel.armed-custom-bypass", STATE_ARMED_HOME,
            "@text/state.alarm-control-panel.armed-home", STATE_ARMED_NIGHT,
            "@text/state.alarm-control-panel.armed-night", STATE_ARMED_VACATION,
            "@text/state.alarm-control-panel.armed-vacation", STATE_ARMING, "@text/state.alarm-control-panel.arming",
            STATE_DISARMED, "@text/state.alarm-control-panel.disarmed", STATE_DISARMING,
            "@text/state.alarm-control-panel.disarming", STATE_PENDING, "@text/state.alarm-control-panel.pending",
            STATE_TRIGGERED, "@text/state.alarm-control-panel.triggered");

    /**
     * Configuration class for MQTT component
     */
    public static class Configuration extends EntityConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Alarm");
        }

        List<String> getSupportedFeatures() {
            return getStringList("supported_features");
        }

        Value getCommandTemplate() {
            return getValue("value_template");
        }

        String getCommandTopic() {
            return getString("command_topic");
        }

        String getPayloadArmAway() {
            return getString("payload_arm_away");
        }

        String getPayloadArmHome() {
            return getString("payload_arm_home");
        }

        String getPayloadArmNight() {
            return getString("payload_arm_night");
        }

        String getPayloadArmVacation() {
            return getString("payload_arm_vacation");
        }

        String getPayloadArmCustomBypass() {
            return getString("payload_arm_custom_bypass");
        }

        String getPayloadDisarm() {
            return getString("payload_disarm");
        }

        String getPayloadTrigger() {
            return getString("payload_trigger");
        }

        boolean isRetain() {
            return getBoolean("retain");
        }

        String getStateTopic() {
            return getString("state_topic");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public AlarmControlPanel(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        Map<String, String> stateEnum = new LinkedHashMap<>();
        stateEnum.put(STATE_DISARMED, STATE_DISARMED);
        stateEnum.put(STATE_TRIGGERED, STATE_TRIGGERED);
        stateEnum.put(STATE_ARMING, STATE_ARMING);
        stateEnum.put(STATE_DISARMING, STATE_DISARMING);
        stateEnum.put(STATE_PENDING, STATE_PENDING);

        Map<String, String> commandEnum = new LinkedHashMap<>();
        commandEnum.put(PAYLOAD_DISARM, config.getPayloadDisarm());
        List<String> supportedFeatures = config.getSupportedFeatures();
        if (supportedFeatures.contains(FEATURE_ARM_HOME)) {
            stateEnum.put(STATE_ARMED_HOME, STATE_ARMED_HOME);
            commandEnum.put(PAYLOAD_ARM_HOME, config.getPayloadArmHome());
        }
        if (supportedFeatures.contains(FEATURE_ARM_AWAY)) {
            stateEnum.put(STATE_ARMED_AWAY, STATE_ARMED_AWAY);
            commandEnum.put(PAYLOAD_ARM_AWAY, config.getPayloadArmAway());
        }
        if (supportedFeatures.contains(FEATURE_ARM_NIGHT)) {
            stateEnum.put(STATE_ARMED_NIGHT, STATE_ARMED_NIGHT);
            commandEnum.put(PAYLOAD_ARM_NIGHT, config.getPayloadArmNight());
        }
        if (supportedFeatures.contains(FEATURE_ARM_VACATION)) {
            stateEnum.put(STATE_ARMED_VACATION, STATE_ARMED_VACATION);
            commandEnum.put(PAYLOAD_ARM_VACATION, config.getPayloadArmVacation());
        }
        if (supportedFeatures.contains(FEATURE_ARM_CUSTOM_BYPASS)) {
            stateEnum.put(STATE_ARMED_CUSTOM_BYPASS, STATE_ARMED_CUSTOM_BYPASS);
            commandEnum.put(PAYLOAD_ARM_CUSTOM_BYPASS, config.getPayloadArmCustomBypass());
        }
        if (supportedFeatures.contains(FEATURE_TRIGGER)) {
            commandEnum.put(PAYLOAD_TRIGGER, config.getPayloadTrigger());
        }

        TextValue value = new TextValue(stateEnum, commandEnum, STATE_LABELS, COMMAND_LABELS);
        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, value, "State",
                componentContext.getUpdateListener())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos()). // TODO: use
                                                                                             // getCommandTemplate(),
                                                                                             // but we need to set the
                                                                                             // `action` and `code`
                                                                                             // variables in order to
                                                                                             // use it
                stateTopic(config.getStateTopic(), config.getValueTemplate()).build();

        finalizeChannels();
    }
}
