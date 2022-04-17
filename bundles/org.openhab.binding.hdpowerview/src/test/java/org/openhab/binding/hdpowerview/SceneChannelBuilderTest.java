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
package org.openhab.binding.hdpowerview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.builders.SceneChannelBuilder;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.osgi.framework.Bundle;

/**
 * Unit tests for {@link SceneChannelBuilder}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneChannelBuilderTest {

    private static final ChannelGroupUID CHANNEL_GROUP_UID = new ChannelGroupUID(
            new ThingUID(HDPowerViewBindingConstants.BINDING_ID, SceneChannelBuilderTest.class.getSimpleName()),
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    private static final HDPowerViewTranslationProvider translationProvider = new HDPowerViewTranslationProvider(
            mock(Bundle.class), new TranslationProviderForTests(), new LocaleProviderForTests());
    private SceneChannelBuilder builder = SceneChannelBuilder.create(translationProvider, CHANNEL_GROUP_UID);

    @BeforeEach
    private void setUp() {
        builder = SceneChannelBuilder.create(translationProvider, CHANNEL_GROUP_UID);
    }

    @Test
    public void labelIsCorrect() {
        List<Scene> scenes = createScenes();
        List<Channel> channels = builder.withScenes(scenes).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene", channels.get(0).getLabel());
    }

    @Test
    public void descriptionIsCorrect() {
        List<Scene> scenes = createScenes();
        List<Channel> channels = builder.withScenes(scenes).build();

        assertEquals(1, channels.size());
        assertEquals("Activates the scene 'TestScene'", channels.get(0).getDescription());
    }

    @Test
    public void groupAndIdAreCorrect() {
        List<Scene> scenes = createScenes();
        List<Channel> channels = builder.withScenes(scenes).build();

        assertEquals(1, channels.size());
        assertEquals(CHANNEL_GROUP_UID.getId(), channels.get(0).getUID().getGroupId());
        assertEquals(Integer.toString(scenes.get(0).id), channels.get(0).getUID().getIdWithoutGroup());
    }

    @Test
    public void autoUpdatePolicyIsCorrect() {
        List<Scene> scenes = createScenes();
        List<Channel> channels = builder.withScenes(scenes).build();

        assertEquals(1, channels.size());
        assertEquals(AutoUpdatePolicy.VETO, channels.get(0).getAutoUpdatePolicy());
    }

    @Test
    public void suppliedListIsUsed() {
        List<Scene> scenes = createScenes();
        List<Channel> existingChannels = new ArrayList<>(0);
        List<Channel> channels = builder.withScenes(scenes).withChannels(existingChannels).build();

        assertEquals(existingChannels, channels);
    }

    @Test
    public void emptyListWhenNoScenes() {
        List<Channel> channels = builder.build();

        assertEquals(0, channels.size());
    }

    private List<Scene> createScenes() {
        Scene scene = new Scene();
        scene.id = 1;
        scene.name = Base64.getEncoder().encodeToString(("TestScene").getBytes());
        return new ArrayList<>(List.of(scene));
    }
}
