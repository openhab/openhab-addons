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
package org.openhab.binding.spotify.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Playlist;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;

/**
 * Dynamically create the users list of devices and playlists.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, SpotifyDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class SpotifyDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    private final Map<ChannelUID, List<Device>> devicesByChannel = new HashMap<>();
    private final Map<ChannelUID, List<Playlist>> playlistsByChannel = new HashMap<>();

    public void setDevices(ChannelUID channelUID, List<Device> spotifyDevices) {
        final List<Device> devices = devicesByChannel.get(channelUID);

        if (devices == null || (spotifyDevices.size() != devices.size()
                || !spotifyDevices.stream().allMatch(sd -> devices.stream().anyMatch(
                        d -> sd.getId() == d.getId() && d.getName() != null && d.getName().equals(sd.getName()))))) {
            devicesByChannel.put(channelUID, spotifyDevices);
            setStateOptions(channelUID, spotifyDevices.stream()
                    .map(device -> new StateOption(device.getId(), device.getName())).collect(Collectors.toList()));
        }
    }

    public void setPlayLists(ChannelUID channelUID, List<Playlist> spotifyPlaylists) {
        final List<Playlist> playlists = playlistsByChannel.get(channelUID);

        if (playlists == null || (spotifyPlaylists.size() != playlists.size() || !spotifyPlaylists.stream()
                .allMatch(sp -> playlists.stream().anyMatch(p -> p.getUri() != null && p.getUri().equals(sp.getUri())
                        && p.getName() != null && p.getName().equals(sp.getName()))))) {
            playlistsByChannel.put(channelUID, spotifyPlaylists);
            setStateOptions(channelUID,
                    spotifyPlaylists.stream().map(playlist -> new StateOption(playlist.getUri(), playlist.getName()))
                            .collect(Collectors.toList()));
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        devicesByChannel.clear();
        playlistsByChannel.clear();
    }
}
