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
package org.openhab.binding.mqtt.generic.internal.handler;

import static org.openhab.binding.mqtt.generic.internal.MqttBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;

/**
 * Static test definitions, like thing, bridge and channel definitions
 *
 * @author David Graeff - Initial contribution
 */
public class ThingChannelConstants {
    // Common ThingUID and ChannelUIDs
    final public static ThingUID testGenericThing = new ThingUID(GENERIC_MQTT_THING, "genericthing");

    final public static ChannelTypeUID textChannel = new ChannelTypeUID(BINDING_ID, MqttBindingConstants.STRING);
    final public static ChannelTypeUID textWithJsonChannel = new ChannelTypeUID(BINDING_ID,
            MqttBindingConstants.STRING);
    final public static ChannelTypeUID onoffChannel = new ChannelTypeUID(BINDING_ID, MqttBindingConstants.SWITCH);
    final public static ChannelTypeUID numberChannel = new ChannelTypeUID(BINDING_ID, MqttBindingConstants.NUMBER);
    final public static ChannelTypeUID percentageChannel = new ChannelTypeUID(BINDING_ID, MqttBindingConstants.DIMMER);
    final public static ChannelTypeUID unknownChannel = new ChannelTypeUID(BINDING_ID, "unknown");

    final public static ChannelUID textChannelUID = new ChannelUID(testGenericThing, "mytext");

    final public static String jsonPathJSON = "{ \"device\": { \"status\": { \"temperature\": 23.2 }}}";
    final public static String jsonPathPattern = "$.device.status.temperature";

    final public static List<Channel> thingChannelList = new ArrayList<>();
    final public static List<Channel> thingChannelListWithJson = new ArrayList<>();

    /**
     * Create a channel with exact the parameters we need for the tests
     *
     * @param id Channel ID
     * @param acceptedType Accept type
     * @param config The configuration
     * @param channelTypeUID ChannelTypeUID provided by the static definitions
     * @return
     */
    public static Channel cb(String id, String acceptedType, Configuration config, ChannelTypeUID channelTypeUID) {
        return ChannelBuilder.create(new ChannelUID(testGenericThing, id), acceptedType).withConfiguration(config)
                .withType(channelTypeUID).build();
    }

    static {
        thingChannelList.add(cb("mytext", "TextItemType", textConfiguration(), textChannel));
        thingChannelList.add(cb("onoff", "OnOffType", onoffConfiguration(), onoffChannel));
        thingChannelList.add(cb("num", "NumberType", numberConfiguration(), numberChannel));
        thingChannelList.add(cb("percent", "NumberType", percentageConfiguration(), percentageChannel));

        thingChannelListWithJson.add(cb("mytext", "TextItemType", textConfigurationWithJson(), textWithJsonChannel));
        thingChannelListWithJson.add(cb("onoff", "OnOffType", onoffConfiguration(), onoffChannel));
        thingChannelListWithJson.add(cb("num", "NumberType", numberConfiguration(), numberChannel));
        thingChannelListWithJson.add(cb("percent", "NumberType", percentageConfiguration(), percentageChannel));
    }

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
        data.put("transformationPattern", "JSONPATH:" + jsonPathPattern);
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
