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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * An MQTT button, following the https://www.home-assistant.io/integrations/button.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Button extends AbstractComponent<Button.Configuration> {
    public static final String BUTTON_CHANNEL_ID = "button";

    public static final String PAYLOAD_PRESS = "PRESS";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_PRESS, "@text/command.button.press");

    public static class Configuration extends EntityConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Button");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        String getCommandTopic() {
            return getString("command_topic");
        }

        String getPayloadPress() {
            return getString("payload_press");
        }

        boolean isRetain() {
            return getBoolean("retain");
        }
    }

    public Button(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        TextValue value = new TextValue(Map.of(), Map.of(PAYLOAD_PRESS, config.getPayloadPress()), Map.of(),
                COMMAND_LABELS);

        buildChannel(BUTTON_CHANNEL_ID, ComponentChannelType.STRING, value, "Button",
                componentContext.getUpdateListener())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        finalizeChannels();
    }
}
