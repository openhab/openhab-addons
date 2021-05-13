package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.OnOffValue;

/**
 * Tests for {@link Switch}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
public class SwitchTests extends AbstractComponentTests {

    @Test
    public void testSwitchWithStateAndCommand() {
        var component = discoverComponent("homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config",
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"command_topic\": \"zigbee2mqtt/th1/set/auto_lock\",\n"
                        + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"payload_off\": \"MANUAL\",\n" + "  \"payload_on\": \"AUTO\",\n"
                        + "  \"state_off\": \"MANUAL\",\n" + "  \"state_on\": \"AUTO\",\n"
                        + "  \"state_topic\": \"zigbee2mqtt/th1\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.switchChannelID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/auto_lock", "state",
                OnOffValue.class);
    }

    @Test
    public void testSwitchWithState() {
        var component = discoverComponent("homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config",
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"state_off\": \"MANUAL\",\n" + "  \"state_on\": \"AUTO\",\n"
                        + "  \"state_topic\": \"zigbee2mqtt/th1\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.switchChannelID, "zigbee2mqtt/th1", "", "state", OnOffValue.class);
    }

    @Test
    public void testSwitchWithCommand() {
        var component = discoverComponent("homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config",
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"command_topic\": \"zigbee2mqtt/th1/set/auto_lock\",\n"
                        + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"payload_off\": \"MANUAL\",\n" + "  \"payload_on\": \"AUTO\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.switchChannelID, "", "zigbee2mqtt/th1/set/auto_lock", "state",
                OnOffValue.class);
    }
}
