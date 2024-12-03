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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.LocationValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link DeviceTracker}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTrackerTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "device_tracker/112233445566-tracker";

    @Test
    public void testIPhone() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "stat_t": "home/TheengsGateway/BTtoMQTT/112233445566",
                  "name": "APPLEDEVICE-tracker",
                  "uniq_id": "112233445566-tracker",
                  "val_tpl": "{% if value_json.get('rssi') -%}home{%- else -%}not_home{%- endif %}",
                  "source_type": "bluetooth_le",
                  "device": {
                    "ids": ["112233445566"],
                    "cns": [["mac", "112233445566"]],
                    "mf": "Apple",
                    "mdl": "APPLEDEVICE",
                    "name": "Apple iPhone/iPad-123456",
                    "via_device": "TheengsGateway"
                  }
                }
                """);

        assertThat(component.channels.size(), is(3));
        assertThat(component.getName(), is("APPLEDEVICE-tracker"));

        assertChannel(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, "home/TheengsGateway/BTtoMQTT/112233445566",
                "", "Location Name", TextValue.class);
        assertChannel(component, DeviceTracker.HOME_CHANNEL_ID, "", "", "At Home", OnOffValue.class);
        assertChannel(component, DeviceTracker.SOURCE_TYPE_CHANNEL_ID, "", "", "Source Type", TextValue.class);
        assertState(component, DeviceTracker.SOURCE_TYPE_CHANNEL_ID, new StringType("bluetooth_le"));

        publishMessage("home/TheengsGateway/BTtoMQTT/112233445566", """
                {
                  "id": "11:22:33:44:55:66",
                  "rssi": -55,
                  "brand": "Apple",
                  "model": "Apple iPhone/iPad",
                  "model_id": "APPLEDEVICE",
                  "type": "TRACK",
                  "unlocked": false
                }
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.ON);
        publishMessage("home/TheengsGateway/BTtoMQTT/112233445566", """
                 {"id": "11:22:33:44:55:66", "presence": "absent"}
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("not_home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.OFF);
    }

    @Test
    public void testGeneric() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "stat_t": "devices/112233445566",
                  "name": "tracker"
                }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("tracker"));

        assertChannel(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, "devices/112233445566", "", "Location Name",
                TextValue.class);
        assertChannel(component, DeviceTracker.HOME_CHANNEL_ID, "", "", "At Home", OnOffValue.class);

        publishMessage("devices/112233445566", "home");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.ON);
        publishMessage("devices/112233445566", "not_home");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("not_home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.OFF);
        publishMessage("devices/112233445566", "work");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        publishMessage("devices/112233445566", "None");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.NULL);
    }

    @Test
    public void testGPS() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "stat_t": "devices/112233445566",
                  "name": "tracker",
                  "json_attributes_topic": "devices/112233445566/json"
                }
                """);

        assertThat(component.channels.size(), is(5));
        assertThat(component.getName(), is("tracker"));

        assertChannel(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, "devices/112233445566", "", "Location Name",
                TextValue.class);
        assertChannel(component, DeviceTracker.HOME_CHANNEL_ID, "", "", "At Home", OnOffValue.class);
        assertChannel(component, DeviceTracker.LOCATION_CHANNEL_ID, "", "", "Location", LocationValue.class);
        assertChannel(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, "", "", "GPS Accuracy", NumberValue.class);
        assertChannel(component, DeviceTracker.JSON_ATTRIBUTES_CHANNEL_ID, "devices/112233445566/json", "",
                "JSON Attributes", TextValue.class);

        publishMessage("devices/112233445566", "home");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.ON);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566", "not_home");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("not_home"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, OnOffType.OFF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566", "work");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", "not JSON");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "nothing": 1
                }
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.5,
                   "longitude": 91.1
                }
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.5), new DecimalType(91.1)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.6,
                   "longitude": 91.2,
                   "gps_accuracy": 5.5
                }
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.6), new DecimalType(91.2)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, new QuantityType<>(5.5, SIUnits.METRE));
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.7,
                   "longitude": 91.3
                }
                """);
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, new StringType("work"));
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.7), new DecimalType(91.3)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566", "None");
        assertState(component, DeviceTracker.LOCATION_NAME_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.HOME_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
    }

    @Test
    public void testGPSOnly() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "name": "tracker",
                  "json_attributes_topic": "devices/112233445566/json"
                }
                """);

        assertThat(component.channels.size(), is(3));
        assertThat(component.getName(), is("tracker"));

        assertChannel(component, DeviceTracker.LOCATION_CHANNEL_ID, "", "", "Location", LocationValue.class);
        assertChannel(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, "", "", "GPS Accuracy", NumberValue.class);
        assertChannel(component, DeviceTracker.JSON_ATTRIBUTES_CHANNEL_ID, "devices/112233445566/json", "",
                "JSON Attributes", TextValue.class);

        publishMessage("devices/112233445566/json", "not JSON");
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "nothing": 1
                }
                """);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID, UnDefType.NULL);
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.5,
                   "longitude": 91.1
                }
                """);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.5), new DecimalType(91.1)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.6,
                   "longitude": 91.2,
                   "gps_accuracy": 5.5
                }
                """);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.6), new DecimalType(91.2)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, new QuantityType<>(5.5, SIUnits.METRE));
        publishMessage("devices/112233445566/json", """
                {
                   "latitude": 45.7,
                   "longitude": 91.3
                }
                """);
        assertState(component, DeviceTracker.LOCATION_CHANNEL_ID,
                new PointType(new DecimalType(45.7), new DecimalType(91.3)));
        assertState(component, DeviceTracker.GPS_ACCURACY_CHANNEL_ID, UnDefType.NULL);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
