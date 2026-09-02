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
package org.openhab.binding.homeassistant.internal.component;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.binding.mqtt.generic.values.OnOffValue;

/**
 * A MQTT switch, following the https://www.home-assistant.io/components/switch.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Switch extends AbstractComponent<Switch.Configuration> {
    public static final String SWITCH_CHANNEL_ID = "switch";

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Switch");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        @Nullable
        String getStateOff() {
            return getOptionalString("state_off");
        }

        @Nullable
        String getStateOn() {
            return getOptionalString("state_on");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public Switch(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        String payloadOff = config.getPayloadOff();
        String payloadOn = config.getPayloadOn();
        String stateOff = config.getStateOff();
        String stateOn = config.getStateOn();
        if (stateOff == null) {
            stateOff = payloadOff;
        }
        OnOffValue value = new OnOffValue(stateOn, stateOff, payloadOn, payloadOff);

        buildChannel(SWITCH_CHANNEL_ID, ComponentChannelType.SWITCH, value, "Switch",
                componentContext.getUpdateListener()).stateTopic(config.getStateTopic(), config.getValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .inferOptimistic(config.isOptimistic()).build();

        finalizeChannels();
    }
}
