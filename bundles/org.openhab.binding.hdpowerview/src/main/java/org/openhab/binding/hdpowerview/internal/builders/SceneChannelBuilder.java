/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.dto.Scene;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * The {@link SceneChannelBuilder} class creates scene channels
 * from structured scene data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneChannelBuilder extends BaseChannelBuilder {

    @Nullable
    private List<Scene> scenes;

    private SceneChannelBuilder(HDPowerViewTranslationProvider translationProvider, ChannelGroupUID channelGroupUid) {
        super(translationProvider, channelGroupUid, HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);
    }

    /**
     * Creates a {@link SceneChannelBuilder} for the given {@link HDPowerViewTranslationProvider} and
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
        List<Scene> scenes = this.scenes;
        if (scenes == null) {
            return getChannelList(0);
        }
        List<Channel> channels = getChannelList(scenes.size());
        scenes.stream().sorted().forEach(scene -> channels.add(createChannel(scene)));
        return channels;
    }

    private Channel createChannel(Scene scene) {
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(scene.id));
        String description = translationProvider.getText("dynamic-channel.scene-activate.description", scene.getName());
        return ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(channelTypeUid)
                .withLabel(scene.getName()).withDescription(description).withAutoUpdatePolicy(AutoUpdatePolicy.VETO)
                .build();
    }
}
