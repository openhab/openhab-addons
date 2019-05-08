/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.OnOffValue;

/**
 * A MQTT lock, following the https://www.home-assistant.io/components/lock.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentLock extends AbstractComponent<ComponentLock.ChannelConfiguration> {
    public static final String switchChannelID = "lock"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Lock");
        }

        protected boolean optimistic = false;

        protected String state_topic = "";
        protected String payload_lock = "LOCK";
        protected String payload_unlock = "UNLOCK";
        protected @Nullable String command_topic;
    };

    public ComponentLock(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        // We do not support all HomeAssistant quirks
        if (channelConfiguration.optimistic && StringUtils.isNotBlank(channelConfiguration.state_topic)) {
            throw new UnsupportedOperationException("Component:Lock does not support forced optimistic mode");
        }

        buildChannel(switchChannelID,
                new OnOffValue(channelConfiguration.payload_lock, channelConfiguration.payload_unlock),
                channelConfiguration.name).listener(componentConfiguration.getUpdateListener())//
                        .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                        .commandTopic(channelConfiguration.command_topic, channelConfiguration.retain)//
                        .build();
    }
}
