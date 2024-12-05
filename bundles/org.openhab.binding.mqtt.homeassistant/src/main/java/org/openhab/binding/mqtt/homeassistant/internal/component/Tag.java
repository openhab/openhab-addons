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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

/**
 * A MQTT Tag scanner, following the https://www.home-assistant.io/integrations/tag.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Tag extends AbstractComponent<Tag.ChannelConfiguration> {
    public static final String TAG_CHANNEL_ID = "tag";

    /**
     * Configuration class for MQTT component
     */
    public static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Tag Scanner");
        }

        protected String topic = "";
    }

    public Tag(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        buildChannel(TAG_CHANNEL_ID, ComponentChannelType.TRIGGER, new TextValue(), getName(),
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.topic, channelConfiguration.getValueTemplate()).trigger(true).build();
        finalizeChannels();
    }

    @Override
    protected void addJsonAttributesChannel() {
        // json_attributes are not supported
    }
}
