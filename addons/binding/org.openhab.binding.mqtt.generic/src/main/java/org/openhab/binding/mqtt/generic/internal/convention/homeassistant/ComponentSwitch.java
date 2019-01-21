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
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;

import com.google.gson.Gson;

/**
 * A MQTT switch, following the https://www.home-assistant.io/components/switch.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentSwitch extends AbstractComponent {
    public static final String switchChannelID = "switch"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config {
        protected String name = "MQTT Switch";
        protected String icon = "";
        protected int qos = 1;
        protected boolean retain = true;
        protected @Nullable String value_template;
        protected @Nullable String unique_id;

        protected boolean optimistic = false;

        protected String state_topic = "";
        protected String state_on = "true";
        protected String state_off = "false";
        protected @Nullable String command_topic;
        protected String payload_on = "true";
        protected String payload_off = "false";

        protected @Nullable String availability_topic;
        protected String payload_available = "online";
        protected String payload_not_available = "offline";
    };

    protected Config config = new Config();

    public ComponentSwitch(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson) {
        super(thing, haID, configJSON, gson);
        config = gson.fromJson(configJSON, Config.class);

        // We do not support all HomeAssistant quirks
        if (config.optimistic && StringUtils.isNotBlank(config.state_topic)) {
            throw new UnsupportedOperationException("Component:Switch does not support forced optimistic mode");
        }

        channels.put(switchChannelID,
                new CChannel(this, switchChannelID, new OnOffValue(config.state_on, config.state_off),
                        config.state_topic, config.command_topic, config.name, "", channelStateUpdateListener));
    }

    @Override
    public String name() {
        return config.name;
    }
}
