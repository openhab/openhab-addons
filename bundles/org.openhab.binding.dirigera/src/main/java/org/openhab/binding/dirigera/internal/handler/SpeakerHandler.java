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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpeakerHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SpeakerHandler extends BaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(SpeakerHandler.class);

    public SpeakerHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            String targetProperty = channel2PropertyMap.get(channel);
            if (targetProperty != null) {
                switch (channel) {
                    case CHANNEL_PLAYER:
                        if (command instanceof PlayPauseType playPause) {
                            String playState = (PlayPauseType.PLAY.equals(playPause) ? "playbackPlaying"
                                    : "playbackPaused");
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, playState);
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else if (command instanceof NextPreviousType nextPrevious) {
                            String playState = (NextPreviousType.NEXT.equals(nextPrevious) ? "playbackNext"
                                    : "playbackPrevious");
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, playState);
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                    case CHANNEL_VOLUME:
                        if (command instanceof PercentType percent) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, percent.intValue());
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                    case CHANNEL_MUTE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                }
            } else {
                // handle channels not in map due to deeper nesting objects
                switch (channel) {
                    case CHANNEL_SHUFFLE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject mode = new JSONObject();
                            mode.put("shuffle", onOff.equals(OnOffType.ON));
                            JSONObject attributes = new JSONObject();
                            attributes.put("playbackModes", mode);
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                    case CHANNEL_CROSSFADE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject mode = new JSONObject();
                            mode.put("crossfade", onOff.equals(OnOffType.ON));
                            JSONObject attributes = new JSONObject();
                            attributes.put("playbackModes", mode);
                            logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                    case CHANNEL_REPEAT:
                        if (command instanceof DecimalType decimal) {
                            int repeatModeInt = decimal.intValue();
                            String repeatModeStr = "";
                            switch (repeatModeInt) {
                                case 0:
                                    repeatModeStr = "off";
                                    break;
                                case 1:
                                    repeatModeStr = "playItem";
                                    break;
                                case 2:
                                    repeatModeStr = "playlist";
                                    break;
                            }
                            if (!repeatModeStr.isBlank()) {
                                JSONObject mode = new JSONObject();
                                mode.put("repeat", repeatModeStr);
                                JSONObject attributes = new JSONObject();
                                attributes.put("playbackModes", mode);
                                logger.trace("DIRIGERA SPEAKER_DEVICE send to API {}", attributes);
                                gateway().api().sendPatch(config.id, attributes);
                            }
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE command {} doesn't fit to channel {}", command,
                                    channel);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            // logger.trace("DIRIGERA LIGHT_DEVICE update delivered {} attributes", attributes.length());
            logger.trace("DIRIGERA LIGHT_DEVICE update delivered {}", attributes);
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    if (CHANNEL_PLAYER.equals(targetChannel)) {
                        String playerState = attributes.getString(key);
                        switch (playerState) {
                            case "playbackPlaying":
                                updateState(new ChannelUID(thing.getUID(), targetChannel), PlayPauseType.PLAY);
                                break;
                            case "playbackPaused":
                                updateState(new ChannelUID(thing.getUID(), targetChannel), PlayPauseType.PAUSE);
                                break;
                        }
                    } else if (CHANNEL_VOLUME.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                new PercentType(attributes.getInt(key)));
                    } else if (CHANNEL_MUTE.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                OnOffType.from(attributes.getBoolean(key)));
                    } else if (CHANNEL_PLAY_MODES.equals(targetChannel)) {
                        JSONObject playbackModes = attributes.getJSONObject(key);
                        if (playbackModes.has("crossfade")) {
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_CROSSFADE),
                                    OnOffType.from(playbackModes.getBoolean("crossfade")));
                        }
                        if (playbackModes.has("shuffle")) {
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_SHUFFLE),
                                    OnOffType.from(playbackModes.getBoolean("shuffle")));
                        }
                        if (playbackModes.has("repeat")) {
                            String repeatMode = playbackModes.getString("repeat");
                            logger.trace("DIRIGERA SPEAKER_DEVICE repeat mode {}", repeatMode);
                            int playMode = -1;
                            switch (repeatMode) {
                                case "off":
                                    playMode = 0;
                                    break;
                                case "playItem":
                                    playMode = 1;
                                    break;
                                case "playlist":
                                    playMode = 2;
                                    break;
                            }
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_REPEAT), new DecimalType(playMode));
                        } else {
                            logger.trace("DIRIGERA SPEAKER_DEVICE no repeat mode in {}", playbackModes);
                        }
                    } else if (CHANNEL_TRACK.equals(targetChannel)) {
                        // track is nested into attributes playItem:id
                        JSONObject audio = attributes.getJSONObject(key);
                        if (audio.has("playItem")) {
                            JSONObject playItem = audio.getJSONObject("playItem");
                            if (playItem.has("title")) {
                                updateState(new ChannelUID(thing.getUID(), targetChannel),
                                        new StringType(playItem.getString("title")));
                                continue;
                            }
                        } else if (audio.has("playlist")) {
                            JSONObject playlist = audio.getJSONObject("playlist");
                            if (playlist.has("title")) {
                                updateState(new ChannelUID(thing.getUID(), targetChannel),
                                        new StringType(playlist.getString("title")));
                                continue;
                            }
                        }
                        updateState(new ChannelUID(thing.getUID(), targetChannel), UnDefType.UNDEF);
                    } else {
                        logger.trace("DIRIGERA SPEAKER_DEVICE no channel for {} available", key);
                    }
                } else {
                    logger.trace("DIRIGERA SPEAKER_DEVICE no targetChannel for {}", key);
                }
            }
            // outside of channel mapping - image
            if (attributes.has("playbackAudio")) {
                JSONObject playbackAudio = attributes.getJSONObject("playbackAudio");
                if (playbackAudio.has("playItem")) {
                    // only change picture if update changes playItem
                    // in this case change picture to imageUrl or Undef if playItem doesn't contain a picture
                    State imageState = UnDefType.UNDEF;
                    JSONObject playItem = playbackAudio.getJSONObject("playItem");
                    if (playItem.has("imageURL")) {
                        String imageURL = playItem.getString("imageURL");
                        imageState = gateway().api().getImage(imageURL);
                        logger.trace("DIRIGERA SPEAKER_DEVICE image received");
                    }
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_IMAGE), imageState);
                }
            }
        }
    }
}
