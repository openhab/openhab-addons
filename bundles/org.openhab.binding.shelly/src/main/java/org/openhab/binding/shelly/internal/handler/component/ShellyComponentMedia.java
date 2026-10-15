/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler.component;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia.Shelly2DeviceStatusMediaPlayback;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia.Shelly2DeviceStatusMediaPlayback.Shelly2DeviceStatusMediaMeta;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyComponentMedia} implements the Media component (e.g. Wall Display Media Player): status mapping,
 * channel creation/removal, channel updates and command handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponentMedia {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyComponentMedia.class);

    private static final int VOLUME_DEVICE_MAX = 10;
    // A smaller step would map back to the same device volume, making INCREASE/DECREASE a no-op
    static final int VOLUME_STEPSIZE = 100 / VOLUME_DEVICE_MAX;

    private static final String[] CHANNELS = { CHANNEL_MEDIA_CONTROL, CHANNEL_MEDIA_VOLUME, CHANNEL_MEDIA_TITLE,
            CHANNEL_MEDIA_ARTIST, CHANNEL_MEDIA_ALBUM, CHANNEL_MEDIA_TYPE, CHANNEL_MEDIA_PLAY_MEDIA_ID,
            CHANNEL_MEDIA_PLAY_RADIO_FAV_ID };
    private static final Set<String> CHANNEL_IDS = Arrays.stream(CHANNELS)
            .map(channel -> CHANNEL_GROUP_MEDIA + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * A NotifyStatus only carries the components it reports, so only a full GetStatus may clear the media
     * component, which in turn removes its channels.
     */
    public static void fillStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult result, boolean fullStatus) {
        if (fullStatus || result.media != null) {
            status.media = result.media;
        }
    }

    /**
     * Create the channels once the device reports a playback object and remove them when it's gone. A bare
     * {@code media:{"rev":0}} doesn't count, since nothing would ever be published to those channels.
     * As of fw 2.7.4 the app's "Media Player" toggle isn't observable via the API, so this only reacts to the
     * component (dis)appearing.
     */
    public static void updateChannels(ShellyThingInterface thingHandler, ShellySettingsStatus status) {
        Shelly2DeviceStatusMedia media = status.media;
        Shelly2DeviceStatusMediaPlayback playback = media != null ? media.playback : null;
        if (playback == null) {
            thingHandler.removeChannels(CHANNEL_IDS);
            return;
        }

        Map<String, Channel> channels = new LinkedHashMap<>();
        for (String channel : CHANNELS) {
            ShellyChannelDefinitions.addChannel(thingHandler.getThing(), channels, true, CHANNEL_GROUP_MEDIA, channel);
        }
        thingHandler.updateThingChannels(Map.of(), channels);

        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL,
                getBool(playback.enable) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        Integer volume = playback.volume;
        if (volume != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_VOLUME,
                    new PercentType(volumeToPercent(volume)));
        }
        // Only called with a full status, so missing metadata means nothing is playing (anymore)
        Shelly2DeviceStatusMediaMeta meta = playback.mediaMeta;
        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_TYPE, toStringOrUndef(playback.mediaType));
        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_TITLE,
                toStringOrUndef(meta != null ? meta.title : null));
        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ARTIST,
                toStringOrUndef(meta != null ? meta.artist : null));
        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ALBUM,
                toStringOrUndef(meta != null ? meta.album : null));
    }

    public static void handleCommand(ShellyThingInterface thingHandler, String channel, Command command)
            throws ShellyApiException {
        if (!(thingHandler.getApi() instanceof Shelly2ApiRpc api)) {
            throw new ShellyApiException("Media commands require a Gen2+ device");
        }
        String thingName = thingHandler.getThingName();
        switch (channel) {
            case CHANNEL_MEDIA_CONTROL:
                LOGGER.debug("{}: Media control command {}", thingName, command);
                if (command instanceof PlayPauseType playPause) {
                    // the device only exposes a PlayOrPause toggle, so only toggle when the requested state
                    // differs from the cached one, which stays the toggle reference until the next poll
                    if (!playPause.equals(thingHandler.getChannelValue(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL))) {
                        api.apiRequest(SHELLYRPC_METHOD_MEDIA_PLAYORPAUSE, null, String.class);
                        thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL, playPause);
                    }
                } else if (command == NextPreviousType.NEXT) {
                    api.apiRequest(SHELLYRPC_METHOD_MEDIA_NEXT, null, String.class);
                } else if (command == NextPreviousType.PREVIOUS) {
                    api.apiRequest(SHELLYRPC_METHOD_MEDIA_PREVIOUS, null, String.class);
                }
                break;
            case CHANNEL_MEDIA_VOLUME:
                State current = thingHandler.getChannelValue(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_VOLUME);
                int percent = commandToVolumePercent(command,
                        current instanceof PercentType currentPercent ? currentPercent.intValue() : 0);
                if (percent >= 0) {
                    LOGGER.debug("{}: Set media volume to {}", thingName, percent);
                    int deviceVolume = percentToVolume(percent);
                    api.apiRequest(new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_MEDIA_SETVOLUME)
                            .withVolume(deviceVolume));
                    thingHandler.updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_VOLUME,
                            new PercentType(volumeToPercent(deviceVolume)));
                }
                break;
            case CHANNEL_MEDIA_PLAY_MEDIA_ID:
                if (command instanceof Number id) {
                    LOGGER.debug("{}: Play media id {}", thingName, id);
                    api.apiRequest(
                            new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_MEDIA_PLAY).withId(id.intValue()));
                }
                break;
            case CHANNEL_MEDIA_PLAY_RADIO_FAV_ID:
                if (command instanceof Number id) {
                    LOGGER.debug("{}: Play radio favorite id {}", thingName, id);
                    api.apiRequest(new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_MEDIA_RADIO_PLAYFAVOURITE)
                            .withId(id.intValue()));
                }
                break;
        }
    }

    /**
     * @return the requested volume in percent, or -1 if the command doesn't change the volume
     */
    static int commandToVolumePercent(Command command, int currentPercent) {
        if (command instanceof PercentType percent) {
            return percent.intValue();
        } else if (command instanceof OnOffType onOff) {
            return onOff == OnOffType.ON ? 100 : 0;
        } else if (command == IncreaseDecreaseType.INCREASE) {
            return Math.min(currentPercent + VOLUME_STEPSIZE, 100);
        } else if (command == IncreaseDecreaseType.DECREASE) {
            return Math.max(currentPercent - VOLUME_STEPSIZE, 0);
        }
        return -1;
    }

    /**
     * Convert the device's coarse 0-10 volume to the percent scale of the Dimmer channel.
     */
    static int volumeToPercent(int volume) {
        return Math.min(Math.max(volume * 100 / VOLUME_DEVICE_MAX, 0), 100);
    }

    /**
     * Inverse of {@link #volumeToPercent}: every percent value maps to the nearest device volume step.
     */
    static int percentToVolume(int percent) {
        return Math.round(Math.min(Math.max(percent, 0), 100) * VOLUME_DEVICE_MAX / 100f);
    }
}
