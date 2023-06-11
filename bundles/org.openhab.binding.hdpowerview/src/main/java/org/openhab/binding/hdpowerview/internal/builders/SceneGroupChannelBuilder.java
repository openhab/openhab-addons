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
import org.openhab.binding.hdpowerview.internal.dto.SceneCollection;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * The {@link SceneGroupChannelBuilder} class creates scene group channels
 * from structured scene collection data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneGroupChannelBuilder extends BaseChannelBuilder {

    @Nullable
    private List<SceneCollection> sceneCollections;

    private SceneGroupChannelBuilder(HDPowerViewTranslationProvider translationProvider,
            ChannelGroupUID channelGroupUid) {
        super(translationProvider, channelGroupUid, HDPowerViewBindingConstants.CHANNELTYPE_SCENE_GROUP_ACTIVATE);
    }

    /**
     * Creates a {@link SceneGroupChannelBuilder} for the given {@link HDPowerViewTranslationProvider} and
     * {@link ChannelGroupUID}.
     * 
     * @param translationProvider the {@link HDPowerViewTranslationProvider}
     * @param channelGroupUid parent {@link ChannelGroupUID} for created channels
     * @return channel builder
     */
    public static SceneGroupChannelBuilder create(HDPowerViewTranslationProvider translationProvider,
            ChannelGroupUID channelGroupUid) {
        return new SceneGroupChannelBuilder(translationProvider, channelGroupUid);
    }

    /**
     * Adds created channels to existing list.
     * 
     * @param channels list that channels will be added to
     * @return channel builder
     */
    public SceneGroupChannelBuilder withChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }

    /**
     * Sets the scene collections.
     * 
     * @param sceneCollections the scene collections
     * @return channel builder
     */
    public SceneGroupChannelBuilder withSceneCollections(List<SceneCollection> sceneCollections) {
        this.sceneCollections = sceneCollections;
        return this;
    }

    /**
     * Builds and returns the channels.
     *
     * @return the {@link Channel} list
     */
    public List<Channel> build() {
        List<SceneCollection> sceneCollections = this.sceneCollections;
        if (sceneCollections == null) {
            return getChannelList(0);
        }
        List<Channel> channels = getChannelList(sceneCollections.size());
        sceneCollections.stream().sorted().forEach(sceneCollection -> channels.add(createChannel(sceneCollection)));
        return channels;
    }

    private Channel createChannel(SceneCollection sceneCollection) {
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(sceneCollection.id));
        String description = translationProvider.getText("dynamic-channel.scene-group-activate.description",
                sceneCollection.getName());
        return ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(channelTypeUid)
                .withLabel(sceneCollection.getName()).withDescription(description)
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
    }
}
