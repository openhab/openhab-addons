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
package org.openhab.binding.dirigera.internal.handler.speaker;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
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

/**
 * The {@link SpeakerHandler} to control speaker devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SpeakerHandler extends BaseHandler {

    public SpeakerHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_SOUND_CONTROLLER);
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
                            super.sendAttributes(attributes);
                        } else if (command instanceof NextPreviousType nextPrevious) {
                            String playState = (NextPreviousType.NEXT.equals(nextPrevious) ? "playbackNext"
                                    : "playbackPrevious");
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, playState);
                            super.sendAttributes(attributes);
                        }
                        break;
                    case CHANNEL_VOLUME:
                        if (command instanceof PercentType percent) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, percent.intValue());
                            super.sendAttributes(attributes);
                        }
                        break;
                    case CHANNEL_MUTE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, OnOffType.ON.equals(onOff));
                            super.sendAttributes(attributes);
                        }
                        break;
                }
            } else {
                // handle channels not in map due to deeper nesting objects
                switch (channel) {
                    case CHANNEL_SHUFFLE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject mode = new JSONObject();
                            mode.put("shuffle", OnOffType.ON.equals(onOff));
                            JSONObject attributes = new JSONObject();
                            attributes.put("playbackModes", mode);
                            super.sendAttributes(attributes);
                        }
                        break;
                    case CHANNEL_CROSSFADE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject mode = new JSONObject();
                            mode.put("crossfade", OnOffType.ON.equals(onOff));
                            JSONObject attributes = new JSONObject();
                            attributes.put("playbackModes", mode);
                            super.sendAttributes(attributes);
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
                                super.sendAttributes(attributes);
                            }
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
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
                            case "playbackIdle":
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
                            if (playMode != -1) {
                                updateState(new ChannelUID(thing.getUID(), CHANNEL_REPEAT), new DecimalType(playMode));
                            }
                        }

                    } else if (CHANNEL_TRACK.equals(targetChannel)) {
                        // track is nested into attributes playItem
                        State track = UnDefType.UNDEF;
                        State image = UnDefType.UNDEF;
                        JSONObject audio = attributes.getJSONObject(key);
                        if (audio.has("playItem")) {
                            JSONObject playItem = audio.getJSONObject("playItem");
                            if (playItem.has("title")) {
                                track = new StringType(playItem.getString("title"));
                            }
                            if (playItem.has("imageURL")) {
                                String imageURL = playItem.getString("imageURL");
                                image = gateway().api().getImage(imageURL);
                            }
                        } else if (audio.has("playlist")) {
                            JSONObject playlist = audio.getJSONObject("playlist");
                            if (playlist.has("title")) {
                                track = new StringType(playlist.getString("title"));
                            }
                        }
                        updateState(new ChannelUID(thing.getUID(), targetChannel), track);
                        updateState(new ChannelUID(thing.getUID(), CHANNEL_IMAGE), image);
                    }
                }
            }
        }
    }
}
