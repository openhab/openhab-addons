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
package org.openhab.binding.heos.internal.handler;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.payload.BrowseResult;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.YesNoEnum;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;

/**
 * Dynamically create the users list of favorites and playlists.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HeosDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class HeosDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    String getValueByLabel(ChannelUID channelUID, String input) {
        Optional<String> optionalValueByLabel = channelOptionsMap.get(channelUID).stream()
                .filter(o -> input.equals(o.getLabel())).map(StateOption::getValue).findFirst();

        // if no match was found we assume that it already was a value and not a label
        return optionalValueByLabel.orElse(input);
    }

    public void setFavorites(ChannelUID channelUID, List<BrowseResult> favorites) {
        setBrowseResultList(channelUID, favorites, d -> d.mediaId);
    }

    public void setPlaylists(ChannelUID channelUID, List<BrowseResult> playLists) {
        setBrowseResultList(channelUID, playLists, d -> d.containerId);
    }

    private void setBrowseResultList(ChannelUID channelUID, List<BrowseResult> playlists,
            Function<BrowseResult, @Nullable String> function) {
        setStateOptions(channelUID,
                playlists.stream().filter(browseResult -> browseResult.playable == YesNoEnum.YES)
                        .map(browseResult -> getStateOption(function, browseResult)).filter(Optional::isPresent)
                        .map(Optional::get).collect(Collectors.toList()));
    }

    private Optional<StateOption> getStateOption(Function<BrowseResult, @Nullable String> function,
            BrowseResult browseResult) {
        @Nullable
        String identifier = function.apply(browseResult);
        if (identifier != null) {
            return Optional.of(new StateOption(identifier, browseResult.name));
        } else {
            return Optional.empty();
        }
    }

    public void setQueue(ChannelUID channelUID, List<Media> queue) {
        setStateOptions(channelUID,
                queue.stream().map(m -> new StateOption(String.valueOf(m.queueId), m.combinedSongArtist()))
                        .collect(Collectors.toList()));
    }
}
