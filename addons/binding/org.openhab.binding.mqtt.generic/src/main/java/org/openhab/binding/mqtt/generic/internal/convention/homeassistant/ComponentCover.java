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
import org.openhab.binding.mqtt.generic.internal.values.RollershutterValue;

import com.google.gson.Gson;

/**
 * A MQTT Cover component, following the https://www.home-assistant.io/components/cover.mqtt/ specification.
 *
 * Only Open/Close/Stop works so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentCover extends AbstractComponent {
    public static final String switchChannelID = "cover"; // Randomly chosen channel "ID"

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
        protected @Nullable String command_topic;
        protected String payload_open = "OPEN";
        protected String payload_close = "CLOSE";
        protected String payload_stop = "STOP";
    };

    protected Config config = new Config();

    public ComponentCover(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson) {
        super(thing, haID, configJSON, gson);
        config = gson.fromJson(configJSON, Config.class);

        RollershutterValue value = new RollershutterValue(config.payload_open, config.payload_close,
                config.payload_stop);
        channels.put(switchChannelID, new CChannel(this, switchChannelID, value, //
                config.state_topic, config.command_topic, config.name, "", updateListener));
    }

    @Override
    public String name() {
        return config.name;
    }
}
