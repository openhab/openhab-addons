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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelGroupUID;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;

public class HaIDTests {

    @Test(expected = IllegalArgumentException.class)
    public void testTooShort() {
        new HaID("homeassistant/switch/config");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLong() {
        new HaID("homeassistant/switch/a/b/c/config");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoConfig() {
        new HaID("homeassistant/switch/a/b/c");
    }

    @Test
    public void testWithoutNode() {
        HaID subject = new HaID("homeassistant/switch/name/config");

        assertThat(subject.getThingID(), is("name"));
        assertThat(subject.getChannelGroupTypeID(), is("name_switch"));
        assertThat(subject.getChannelTypeID("channel"), is(new ChannelTypeUID("mqtt:name_switch_channel")));
        assertThat(subject.getChannelGroupID(), is("switch"));

        final String thingID = subject.getThingID();
        final ThingUID bridge = new ThingUID("mqtt:broker:brokerId");
        final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, bridge, thingID);
        final ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, subject.getChannelGroupID());
        final ChannelUID channelUID = new ChannelUID(channelGroupUID, "channel");

        final HaID derived = new HaID(subject.baseTopic, channelUID);

        assertThat(derived, isID(subject));
    }

    @Test
    public void testWithNode() {
        HaID subject = new HaID("homeassistant/switch/node/name/config");

        assertThat(subject.getThingID(), is("name"));
        assertThat(subject.getChannelGroupTypeID(), is("name_switchnode"));
        assertThat(subject.getChannelTypeID("channel"), is(new ChannelTypeUID("mqtt:name_switchnode_channel")));
        assertThat(subject.getChannelGroupID(), is("switch_node"));

        final String thingID = subject.getThingID();
        final ThingUID bridge = new ThingUID("mqtt:broker:brokerId");
        final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, bridge, thingID);
        final ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, subject.getChannelGroupID());
        final ChannelUID channelUID = new ChannelUID(channelGroupUID, "channel");

        final HaID derived = new HaID(subject.baseTopic, channelUID);

        assertThat(derived, isID(subject));
    }

    private static Matcher<HaID> isID(HaID other) {
        return new HaIDMatcher(other);
    }

    private static final class HaIDMatcher extends TypeSafeMatcher<HaID> {
        private final HaID other;

        public HaIDMatcher(HaID other) {
            super();
            this.other = other;
        }

        @Override
        protected boolean matchesSafely(HaID item) {
            return StringUtils.equals(other.baseTopic, item.baseTopic)
                    && StringUtils.equals(other.component, item.component)
                    && StringUtils.equals(other.nodeID, item.nodeID)
                    && StringUtils.equals(other.objectID, item.objectID);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(other.toString());
        }

        @Override
        protected void describeMismatchSafely(HaID item, Description mismatchDescription) {
            mismatchDescription.appendText("was").appendValue(item.toString());
        }
    }
}
