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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT lock, following the https://www.home-assistant.io/components/lock.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentLock extends AbstractComponent<ComponentLock.Config> {
    public static final String switchChannelID = "lock"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Lock");
        }

        @SerializedName(value = "optimistic", alternate = "opt")
        protected boolean optimistic = false;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected String state_topic = "";
        @SerializedName(value = "payload_lock", alternate = "pl_lock")
        protected String payload_lock = "LOCK";
        @SerializedName(value = "payload_unlock", alternate = "pl_unlk")
        protected String payload_unlock = "UNLOCK";

        @SerializedName(value = "command_topic", alternate = "cmd_t")
        protected String command_topic = "";
    };

    public ComponentLock(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        // We do not support all HomeAssistant quirks
        if (config.optimistic && StringUtils.isNotBlank(config.state_topic)) {
            throw new UnsupportedOperationException("Component:Lock does not support forced optimistic mode");
        }

        CChannel switchChannel = new CChannel(this, switchChannelID,
                new OnOffValue(config.payload_lock, config.payload_unlock), config.expand(config.state_topic),
                config.expand(config.command_topic), config.retain, "Lock", "", channelStateUpdateListener);
        switchChannel.addTemplateIn(provider, config.value_template);
        addChannel(switchChannel);
    }
}
