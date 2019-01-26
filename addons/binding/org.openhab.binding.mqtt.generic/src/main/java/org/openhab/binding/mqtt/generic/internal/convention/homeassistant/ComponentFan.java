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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;

import com.google.gson.Gson;

/**
 * A MQTT Fan component, following the https://www.home-assistant.io/components/fan.mqtt/ specification.
 *
 * Only ON/OFF is supported so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentFan extends AbstractComponent {
    public static final String switchChannelID = "fan"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config {
        protected String name = "MQTT fan";
        protected String icon = "";
        protected int qos = 1;
        protected boolean retain = true;
        protected @Nullable String state_value_template;
        protected @Nullable String unique_id;

        protected @Nullable String state_topic;
        protected String command_topic = "";
        protected String payload_on = "ON";
        protected String payload_off = "OFF";
    };

    protected Config config = new Config();

    public ComponentFan(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson) {
        super(thing, haID, configJSON, gson);
        config = gson.fromJson(configJSON, Config.class);

        OnOffValue value = new OnOffValue(config.payload_on, config.payload_off);
        channels.put(switchChannelID, new CChannel(this, switchChannelID, value, //
                config.state_topic, config.command_topic, config.name, "", updateListener));
    }

    @Override
    public String name() {
        return config.name;
    }
}
