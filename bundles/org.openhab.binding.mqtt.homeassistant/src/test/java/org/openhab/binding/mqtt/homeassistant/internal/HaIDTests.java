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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItem;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;

/**
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
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

        HandlerConfiguration haConfig = new HandlerConfiguration(subject.baseTopic, List.of(subject.toShortTopic()));

        Collection<HaID> restoreList = HaID.fromConfig(haConfig);
        assertThat(restoreList, hasItem(new HaID("homeassistant/switch/name/config")));
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

        HandlerConfiguration haConfig = new HandlerConfiguration(subject.baseTopic, List.of(subject.toShortTopic()));

        Collection<HaID> restoreList = HaID.fromConfig(haConfig);
        assertThat(restoreList, hasItem(new HaID("homeassistant/switch/node/name/config")));
    }
}
