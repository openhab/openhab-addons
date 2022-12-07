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
package org.openhab.binding.mqtt.homeassistant.internal.handler;

import static org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants.*;

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
    final public static ThingUID testHomeAssistantThing = new ThingUID(HOMEASSISTANT_MQTT_THING, "device234");

    final public static ChannelTypeUID unknownChannel = new ChannelTypeUID(BINDING_ID, "unknown");

    final public static String jsonPathJSON = "{ \"device\": { \"status\": { \"temperature\": 23.2 }}}";
    final public static String jsonPathPattern = "$.device.status.temperature";

    final public static List<Channel> thingChannelList = new ArrayList<>();
    final public static List<Channel> thingChannelListWithJson = new ArrayList<>();

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

}
