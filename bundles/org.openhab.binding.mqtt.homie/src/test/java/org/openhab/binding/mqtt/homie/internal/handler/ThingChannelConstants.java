/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homie.internal.handler;

import static org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Static test definitions, like thing, bridge and channel definitions
 *
 * @author David Graeff - Initial contribution
 */
public class ThingChannelConstants {
    // Common ThingUID and ChannelUIDs
    public static final ThingUID TEST_HOMIE_THING = new ThingUID(HOMIE300_MQTT_THING, "device123");

    public static final ChannelTypeUID UNKNOWN_CHANNEL = new ChannelTypeUID(BINDING_ID, "unknown");

    public static final String JSON_PATH_JSON = "{ \"device\": { \"status\": { \"temperature\": 23.2 }}}";
    public static final String JSON_PATH_PATTERN = "$.device.status.temperature";

    public static final List<Channel> THING_CHANNEL_LIST = new ArrayList<>();
    public static final List<Channel> THING_CHANNEL_LIST_WITH_JSON = new ArrayList<>();

    static Configuration textConfiguration() {
        Map<String, Object> data = new HashMap<>();
        data.put("stateTopic", "test/state");
        data.put("commandTopic", "test/command");
        return new Configuration(data);
    }

    static Configuration textConfigurationWithJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("stateTopic", "test/state");
        data.put("commandTopic", "test/command");
        data.put("transformationPattern", "JSONPATH:" + JSON_PATH_PATTERN);
        return new Configuration(data);
    }

    private static Configuration numberConfiguration() {
        Map<String, Object> data = new HashMap<>();
        data.put("stateTopic", "test/state");
        data.put("commandTopic", "test/command");
        data.put("min", BigDecimal.valueOf(1));
        data.put("max", BigDecimal.valueOf(99));
        data.put("step", BigDecimal.valueOf(2));
        data.put("isDecimal", true);
        return new Configuration(data);
    }

    private static Configuration percentageConfiguration() {
        Map<String, Object> data = new HashMap<>();
        data.put("stateTopic", "test/state");
        data.put("commandTopic", "test/command");
        data.put("on", "ON");
        data.put("off", "OFF");
        return new Configuration(data);
    }

    private static Configuration onoffConfiguration() {
        Map<String, Object> data = new HashMap<>();
        data.put("stateTopic", "test/state");
        data.put("commandTopic", "test/command");
        data.put("on", "ON");
        data.put("off", "OFF");
        data.put("inverse", true);
        return new Configuration(data);
    }
}
