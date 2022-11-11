/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.builders.SceneGroupChannelBuilder;
import org.openhab.binding.hdpowerview.internal.dto.SceneCollection;
import org.openhab.binding.hdpowerview.internal.providers.MockedLocaleProvider;
import org.openhab.binding.hdpowerview.internal.providers.MockedTranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.osgi.framework.Bundle;

/**
 * Unit tests for {@link SceneGroupChannelBuilder}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneGroupChannelBuilderTest {

    private static final ChannelGroupUID CHANNEL_GROUP_UID = new ChannelGroupUID(
            new ThingUID(HDPowerViewBindingConstants.BINDING_ID, SceneGroupChannelBuilderTest.class.getSimpleName()),
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_GROUP_ACTIVATE);

    private static final HDPowerViewTranslationProvider TRANSLATION_PROVIDER = new HDPowerViewTranslationProvider(
            mock(Bundle.class), new MockedTranslationProvider(), new MockedLocaleProvider());

    private SceneGroupChannelBuilder builder = SceneGroupChannelBuilder.create(TRANSLATION_PROVIDER, CHANNEL_GROUP_UID);

    @BeforeEach
    private void setUp() {
        builder = SceneGroupChannelBuilder.create(TRANSLATION_PROVIDER, CHANNEL_GROUP_UID);
    }

    @Test
    public void labelIsCorrect() {
        List<SceneCollection> sceneCollections = createSceneCollections();
        List<Channel> channels = builder.withSceneCollections(sceneCollections).build();

        assertEquals(1, channels.size());
        assertEquals("TestSceneCollection", channels.get(0).getLabel());
    }

    @Test
    public void descriptionIsCorrect() {
        List<SceneCollection> sceneCollections = createSceneCollections();
        List<Channel> channels = builder.withSceneCollections(sceneCollections).build();

        assertEquals(1, channels.size());
        assertEquals("Activates the scene group 'TestSceneCollection'", channels.get(0).getDescription());
    }

    @Test
    public void groupAndIdAreCorrect() {
        List<SceneCollection> sceneCollections = createSceneCollections();
        List<Channel> channels = builder.withSceneCollections(sceneCollections).build();

        assertEquals(1, channels.size());
        assertEquals(CHANNEL_GROUP_UID.getId(), channels.get(0).getUID().getGroupId());
        assertEquals(Integer.toString(sceneCollections.get(0).id), channels.get(0).getUID().getIdWithoutGroup());
    }

    @Test
    public void autoUpdatePolicyIsCorrect() {
        List<SceneCollection> sceneCollections = createSceneCollections();
        List<Channel> channels = builder.withSceneCollections(sceneCollections).build();

        assertEquals(1, channels.size());
        assertEquals(AutoUpdatePolicy.VETO, channels.get(0).getAutoUpdatePolicy());
    }

    @Test
    public void suppliedListIsUsed() {
        List<SceneCollection> sceneCollections = createSceneCollections();
        List<Channel> existingChannels = new ArrayList<>(0);
        List<Channel> channels = builder.withSceneCollections(sceneCollections).withChannels(existingChannels).build();

        assertEquals(existingChannels, channels);
    }

    @Test
    public void emptyListWhenNoSceneCollections() {
        List<Channel> channels = builder.build();

        assertEquals(0, channels.size());
    }

    private List<SceneCollection> createSceneCollections() {
        SceneCollection sceneCollection = new SceneCollection();
        sceneCollection.id = 1;
        sceneCollection.name = Base64.getEncoder().encodeToString(("TestSceneCollection").getBytes());
        return new ArrayList<>(List.of(sceneCollection));
    }
}
