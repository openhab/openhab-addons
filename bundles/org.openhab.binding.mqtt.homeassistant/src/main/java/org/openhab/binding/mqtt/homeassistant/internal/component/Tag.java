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
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractComponentConfiguration;

/**
 * A MQTT Tag scanner, following the https://www.home-assistant.io/integrations/tag.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Tag extends AbstractComponent<Tag.Configuration> {
    public static final String TAG_CHANNEL_ID = "tag";

    public static class Configuration extends AbstractComponentConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "Tag Scanner"); // Technically tag scanners don't have a name
        }

        String getTopic() {
            return getString("topic");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public Tag(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        buildChannel(TAG_CHANNEL_ID, ComponentChannelType.TRIGGER, new TextValue(), "Tag",
                componentContext.getUpdateListener()).stateTopic(config.getTopic(), config.getValueTemplate())
                .trigger(true).build();
        finalizeChannels();
    }
}
