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
package org.openhab.binding.hdpowerview.internal.builders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link SceneChannelBuilder} class creates scene channels
 * from structured scene data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneChannelBuilder {

    private final HDPowerViewTranslationProvider translationProvider;
    private final ChannelGroupUID channelGroupUid;
    private final ChannelTypeUID channelTypeUid = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    private List<Channel> channels;
    private List<Scene> scenes;

    public SceneChannelBuilder(HDPowerViewTranslationProvider translationProvider, ChannelGroupUID channelGroupUid) {
        this.translationProvider = translationProvider;
        this.channelGroupUid = channelGroupUid;
        this.channels = new ArrayList<>(0);
        this.scenes = new ArrayList<>(0);
    }

    /**
     * Creates an {@link SceneChannelBuilder} for the given {@link HDPowerViewTranslationProvider} and
     * {@link ChannelGroupUID}.
     * 
     * @param translationProvider the {@link HDPowerViewTranslationProvider}
     * @param channelGroupUid parent {@link ChannelGroupUID} for created channels
     * @return channel builder
     */
    public static SceneChannelBuilder create(HDPowerViewTranslationProvider translationProvider,
            ChannelGroupUID channelGroupUid) {
        return new SceneChannelBuilder(translationProvider, channelGroupUid);
    }

    /**
     * Adds created channels to existing list.
     * 
     * @param channels list that channels will be added to
     * @return channel builder
     */
    public SceneChannelBuilder withChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }

    /**
     * Sets the scenes.
     * 
     * @param scenes the scenes
     * @return channel builder
     */
    public SceneChannelBuilder withScenes(List<Scene> scenes) {
        this.scenes = scenes;
        return this;
    }

    /**
     * Builds and returns the channels.
     *
     * @return the {@link Channel} list
     */
    public List<Channel> build() {
        scenes.stream().sorted().forEach(scene -> channels.add(createChannel(scene)));
        return channels;
    }

    private Channel createChannel(Scene scene) {
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(scene.id));
        String description = translationProvider.getText("dynamic-channel.scene-activate.description", scene.getName());
        return ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(channelTypeUid)
                .withLabel(scene.getName()).withDescription(description).build();
    }
}
