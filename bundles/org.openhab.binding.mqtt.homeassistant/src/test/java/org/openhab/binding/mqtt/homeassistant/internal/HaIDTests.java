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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.config.core.Configuration;
import org.junit.Test;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;

public class HaIDTests {

    @Test
    public void testWithoutNode() {
        HaID subject = new HaID("homeassistant/switch/name/config");

        assertThat(subject.objectID, is("name"));

        assertThat(subject.component, is("switch"));
        assertThat(subject.getTopic("suffix"), is("homeassistant/switch/name/suffix"));

        Configuration config = new Configuration();
        subject.toConfig(config);

        HaID restore = HaID.fromConfig("homeassistant", config);

        assertThat(restore, is(subject));

        HandlerConfiguration haConfig = subject.toHandlerConfiguration();

        restore = HaID.fromConfig(haConfig);
        assertThat(restore, is(new HaID("homeassistant/+/name/config")));
    }

    @Test
    public void testWithNode() {
        HaID subject = new HaID("homeassistant/switch/node/name/config");

        assertThat(subject.objectID, is("name"));

        assertThat(subject.component, is("switch"));
        assertThat(subject.getTopic("suffix"), is("homeassistant/switch/node/name/suffix"));

        Configuration config = new Configuration();
        subject.toConfig(config);

        HaID restore = HaID.fromConfig("homeassistant", config);

        assertThat(restore, is(subject));

        HandlerConfiguration haConfig = subject.toHandlerConfiguration();

        restore = HaID.fromConfig(haConfig);
        assertThat(restore, is(new HaID("homeassistant/+/node/name/config")));
    }

}
