/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.AUDIO_INFO;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.TUNER;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.VIDEO_INFO;

import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup;

/**
 * Unit tests for {@link EmotivaSubscriptionTagGroupHandler}.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaSubscriptionTagGroupHandlerTest {

    @Test
    void sourceTuner() {
        EmotivaConfiguration config = new EmotivaConfiguration();
        EmotivaProcessorState state = new EmotivaProcessorState();
        var subscriptionHandler = new EmotivaSubscriptionTagGroupHandler(config, state);

        BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups = (subscribeSet,
                unsubscribeSet) -> {
            assertThat(subscribeSet, hasItems(TUNER));
            assertThat(subscribeSet, not(hasItems(VIDEO_INFO)));
            assertThat(unsubscribeSet, hasItems(VIDEO_INFO));
            assertThat(unsubscribeSet, not(hasItems(TUNER)));
        };
        subscriptionHandler.tagGroupsFromSource(EmotivaControlCommands.tuner, tagGroups);
    }

    @Test
    void sourceAudioOnly() {
        EmotivaConfiguration config = new EmotivaConfiguration();
        EmotivaProcessorState state = new EmotivaProcessorState();
        var subscriptionHandler = new EmotivaSubscriptionTagGroupHandler(config, state);

        BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups = (subscribeSet,
                unsubscribeSet) -> {
            assertThat(subscribeSet, hasItems(AUDIO_INFO));
            assertThat(subscribeSet, not(hasItems(TUNER)));
            assertThat(unsubscribeSet, hasItems(TUNER));
            assertThat(unsubscribeSet, not(hasItems(AUDIO_INFO)));
        };
        subscriptionHandler.tagGroupsFromSource(EmotivaControlCommands.analog1, tagGroups);
    }

    @Test
    void sourceAudioAndVideo() {
        EmotivaConfiguration config = new EmotivaConfiguration();
        EmotivaProcessorState state = new EmotivaProcessorState();
        var subscriptionHandler = new EmotivaSubscriptionTagGroupHandler(config, state);

        BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups = (subscribeSet,
                unsubscribeSet) -> {
            assertThat(subscribeSet, hasItems(AUDIO_INFO));
            assertThat(subscribeSet, hasItems(VIDEO_INFO));
            assertThat(subscribeSet, not(hasItems(TUNER)));
            assertThat(unsubscribeSet, hasItems(TUNER));
            assertThat(unsubscribeSet, not(hasItems(AUDIO_INFO)));
            assertThat(unsubscribeSet, not(hasItems(VIDEO_INFO)));
        };
        subscriptionHandler.tagGroupsFromSource(EmotivaControlCommands.hdmi1, tagGroups);
    }
}
