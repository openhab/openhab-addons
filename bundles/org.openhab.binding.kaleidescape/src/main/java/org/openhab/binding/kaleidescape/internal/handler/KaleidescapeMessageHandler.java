/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.kaleidescape.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeFormatter;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeStatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KaleidescapeMessageHandler} class processes all messages received
 * by the event listener and then updates the appropriate states
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum KaleidescapeMessageHandler {
    UI_STATE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.UI_STATE, new StringType(message));
        }
    },
    HIGHLIGHTED_SELECTION {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.HIGHLIGHTED_SELECTION, new StringType(message));
        }
    },
    DEVICE_POWER_STATE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 1:1
            // power_state, zone 1 state, zone n state
            final Pattern p = Pattern.compile("^(\\d{1}):(.*)$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                if (("1").equals(matcher.group(1))) {
                    handler.updateChannel(KaleidescapeBindingConstants.POWER, OnOffType.ON);
                } else if (("0").equals(matcher.group(1))) {
                    handler.updateChannel(KaleidescapeBindingConstants.POWER, OnOffType.OFF);
                }
            } else {
                logger.debug("DEVICE_POWER_STATE - no match on message: {}", message);
            }
        }
    },
    TITLE_NAME {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.TITLE_NAME,
                    new StringType(KaleidescapeFormatter.formatString(message)));
        }
    },
    PLAY_STATUS {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 0:0:00:00000:00000:000:00000:00000
            // mode, speed, title_num, title_length, title_loc, chapter_num, chapter_length, chapter_loc
            final Pattern p = Pattern
                    .compile("^(\\d{1}):(\\d{1}):(\\d{2}):(\\d{5}):(\\d{5}):(\\d{3}):(\\d{5}):(\\d{5})$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.PLAY_MODE,
                        new StringType(KaleidescapeStatusCodes.PLAY_MODE.get(matcher.group(1))));

                handler.updateChannel(KaleidescapeBindingConstants.PLAY_SPEED, new StringType(matcher.group(2)));

                handler.updateChannel(KaleidescapeBindingConstants.TITLE_NUM,
                        new DecimalType(Integer.parseInt(matcher.group(3))));

                handler.updateChannel(KaleidescapeBindingConstants.TITLE_LENGTH,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(4)), handler.apiSecondUnit));

                handler.updateChannel(KaleidescapeBindingConstants.TITLE_LOC,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(5)), handler.apiSecondUnit));

                handler.updateChannel(KaleidescapeBindingConstants.CHAPTER_NUM,
                        new DecimalType(Integer.parseInt(matcher.group(6))));

                handler.updateChannel(KaleidescapeBindingConstants.CHAPTER_LENGTH,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(7)), handler.apiSecondUnit));

                handler.updateChannel(KaleidescapeBindingConstants.CHAPTER_LOC,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(8)), handler.apiSecondUnit));
            } else {
                logger.debug("PLAY_STATUS - no match on message: {}", message);
            }
        }
    },
    MOVIE_MEDIA_TYPE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.MOVIE_MEDIA_TYPE,
                    new StringType(KaleidescapeStatusCodes.MEDIA_TYPE.get(message)));
        }
    },
    MOVIE_LOCATION {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.MOVIE_LOCATION,
                    new StringType(KaleidescapeStatusCodes.MOVIE_LOCATION.get(message)));
        }
    },
    VIDEO_MODE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            handler.updateChannel(KaleidescapeBindingConstants.VIDEO_MODE, new StringType(message));

            // example: 00:00:00
            // composite, component, hdmi
            final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.VIDEO_MODE_COMPOSITE,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(1))));

                handler.updateChannel(KaleidescapeBindingConstants.VIDEO_MODE_COMPONENT,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(2))));

                handler.updateChannel(KaleidescapeBindingConstants.VIDEO_MODE_HDMI,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(3))));
            } else {
                logger.debug("VIDEO_MODE - no match on message: {}", message);
            }
        }
    },
    VIDEO_COLOR {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            handler.updateChannel(KaleidescapeBindingConstants.VIDEO_COLOR, new StringType(message));

            // example: 02:01:24:01
            // eotf, color_space, color_depth, color_sampling
            final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2})$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.VIDEO_COLOR_EOTF,
                        new StringType(KaleidescapeStatusCodes.EOTF.get(matcher.group(1))));
            } else {
                logger.debug("VIDEO_COLOR - no match on message: {}", message);
            }
        }
    },
    CONTENT_COLOR {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            handler.updateChannel(KaleidescapeBindingConstants.CONTENT_COLOR, new StringType(message));

            // example: 02:01:24:01
            // eotf, color_space, color_depth, color_sampling
            final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2})$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.CONTENT_COLOR_EOTF,
                        new StringType(KaleidescapeStatusCodes.EOTF.get(matcher.group(1))));
            } else {
                logger.debug("CONTENT_COLOR - no match on message: {}", message);
            }
        }
    },
    SCALE_MODE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.SCALE_MODE, new StringType(message));
        }
    },
    SCREEN_MASK {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.SCREEN_MASK, new StringType(message));

            // per API reference rev 3.3.1, ASPECT_RATIO message should not be used
            // the first element of SCREEN_MASK now provides this info
            if (!message.equals("")) {
                String[] msgSplit = message.split(":", 2);
                handler.updateChannel(KaleidescapeBindingConstants.ASPECT_RATIO,
                        new StringType(KaleidescapeStatusCodes.ASPECT_RATIO.get(msgSplit[0])));
            }
        }
    },
    SCREEN_MASK2 {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.SCREEN_MASK2, new StringType(message));
        }
    },
    CINEMASCAPE_MASK {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.CINEMASCAPE_MASK, new StringType(message));
        }
    },
    CINEMASCAPE_MODE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.CINEMASCAPE_MODE, new StringType(message));
        }
    },
    CHILD_MODE_STATE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.CHILD_MODE_STATE, new StringType(message));
        }
    },
    MUSIC_TITLE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: You:Radiohead:Pablo Honey:1.9b5f4d786d7e2c49-t301_577:1.R_1493833:2.200c5
            // track, artist, album, track handle, album handle, now playing handle
            final Pattern p = Pattern.compile("^(.*):(.*):(.*):(.*):(.*):(.*)$");

            // first replace delimited : in track/artist/album name with ||, fix it later in formatString()
            Matcher matcher = p.matcher(message.replace("\\:", "||"));
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_TRACK,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(1))));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_ARTIST,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(2))));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_ALBUM,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(3))));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_TRACK_HANDLE,
                        new StringType(matcher.group(4)));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_ALBUM_HANDLE,
                        new StringType(matcher.group(5)));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_NOWPLAY_HANDLE,
                        new StringType(matcher.group(6)));
            } else {
                logger.debug("MUSIC_TITLE - no match on message: {}", message);
            }
        }
    },
    MUSIC_PLAY_STATUS {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 2:0:00207:+00000:000.00
            // 2:0:00331:+00183:055.29
            // mode, speed, track length, track position, track progress %
            final Pattern p = Pattern.compile("^(\\d{1}):(\\d{1}):(\\d{5}):(.\\d{5}):(\\d{3}\\.\\d{2})$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_PLAY_MODE,
                        new StringType(KaleidescapeStatusCodes.PLAY_MODE.get(matcher.group(1))));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_PLAY_SPEED, new StringType(matcher.group(2)));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_TRACK_LENGTH,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(3)), handler.apiSecondUnit));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_TRACK_POSITION,
                        new QuantityType<Time>(Integer.parseInt(matcher.group(4)), handler.apiSecondUnit));

                handler.updateChannel(KaleidescapeBindingConstants.MUSIC_TRACK_PROGRESS,
                        new DecimalType(BigDecimal.valueOf(Math.round(Double.parseDouble(matcher.group(5))))));
            } else {
                logger.debug("MUSIC_PLAY_STATUS - no match on message: {}", message);
            }
        }
    },
    MUSIC_NOW_PLAYING_STATUS {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 00013:00000:0:0:0000000238:2.200c5
            // total # tracks in list, list index, repeat, random, generation, now_playing handle
            // only using repeat & random right now
            final Pattern p = Pattern.compile("^(\\d{5}):(\\d{5}):(\\d{1}):(\\d{1}):(\\d{10}):(.*)$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                // update REPEAT switch state
                if (("1").equals(matcher.group(3))) {
                    handler.updateChannel(KaleidescapeBindingConstants.MUSIC_REPEAT, OnOffType.ON);
                } else {
                    handler.updateChannel(KaleidescapeBindingConstants.MUSIC_REPEAT, OnOffType.OFF);
                }

                // update RANDOM switch state
                if (("1").equals(matcher.group(4))) {
                    handler.updateChannel(KaleidescapeBindingConstants.MUSIC_RANDOM, OnOffType.ON);
                } else {
                    handler.updateChannel(KaleidescapeBindingConstants.MUSIC_RANDOM, OnOffType.OFF);
                }
            } else {
                logger.debug("MUSIC_NOW_PLAYING_STATUS - no match on message: {}", message);
            }
        }
    },
    PLAYING_MUSIC_INFORMATION {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: R_1493833:Radiohead - Pablo Honey
            // album handle, artist - album
            // do nothing; redundant
        }
    },
    CONTENT_DETAILS {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // g1=meta id, g2=meta type, g3=data
            // example: 6:Year:1995
            // or: 10:Genres:Pop\/Rock
            final Pattern p = Pattern.compile("^(\\d{1,2}):([^:^/]*):(.*)$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                String metaType = matcher.group(2).toLowerCase();
                String value = KaleidescapeFormatter.formatString(matcher.group(3));

                // the CONTENT_DETAILS message with id=1 tells us what type of meta data is coming
                if ("1".equals(matcher.group(1))) {
                    if (("content_handle").equals(metaType)) {
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_TYPE, new StringType("movie"));
                        handler.metaRuntimeMultiple = 60;

                        // null out album specific
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_ALBUM_TITLE, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_ARTIST, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_REVIEW, UnDefType.NULL);

                    } else if (("album_content_handle").equals(metaType)) {
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_TYPE, new StringType("album"));
                        handler.metaRuntimeMultiple = 1;

                        // null out movie specific
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_TITLE, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_RATING, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_ACTORS, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_DIRECTORS, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_RATING_REASON, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_SYNOPSIS, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COLOR_DESCRIPTION,
                                UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COUNTRY, UnDefType.NULL);
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_ASPECT_RATIO, UnDefType.NULL);

                    } else {
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_TYPE, UnDefType.UNDEF);
                    }
                    // otherwise update the channel if it is one we care about
                } else if (KaleidescapeBindingConstants.metadataChannels.contains(metaType)) {
                    // special case for cover art image
                    if (KaleidescapeBindingConstants.DETAIL_COVER_URL.equals(metaType)) {
                        handler.updateDetailChannel(metaType, new StringType(value));
                        if (!value.isEmpty()) {
                            try {
                                ContentResponse contentResponse = handler.httpClient.newRequest(value).method(GET)
                                        .timeout(20, TimeUnit.SECONDS).send();
                                int httpStatus = contentResponse.getStatus();
                                if (httpStatus == OK_200) {
                                    handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COVER_ART,
                                            new RawType(contentResponse.getContent()));
                                } else {
                                    handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COVER_ART,
                                            UnDefType.NULL);
                                }
                            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                logger.debug("Error updating Cover Art Image channel for url: {}", value);
                                handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COVER_ART,
                                        UnDefType.NULL);
                            }
                        } else {
                            handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_COVER_ART, UnDefType.NULL);
                        }
                        // special case for running time to create a QuantityType<Time>
                    } else if (KaleidescapeBindingConstants.DETAIL_RUNNING_TIME.equals(metaType)) {
                        handler.updateDetailChannel(KaleidescapeBindingConstants.DETAIL_RUNNING_TIME,
                                new QuantityType<Time>(Integer.parseInt(value) * handler.metaRuntimeMultiple,
                                        handler.apiSecondUnit));
                        // everything else just send it as a string
                    } else {
                        handler.updateDetailChannel(metaType, new StringType(value));
                    }
                }
            } else {
                logger.debug("CONTENT_DETAILS - no match on message: {}", message);
            }
        }
    },
    TIME {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // do nothing
        }
    },
    STATUS_CUE_PERIOD {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // do nothing
        }
    },
    ASPECT_RATIO {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // per API reference rev 3.3.1, ASPECT_RATIO message should not be used
            // the first element of SCREEN_MASK now provides this info
        }
    },
    USER_DEFINED_EVENT {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: SELECT_KALEIDESCAPE_INPUT
            try {
                switch (message) {
                    // when the ipad or phone app is started up, it sends a VOLUME_QUERY,
                    // so we respond to enable volume controls and set the initial volume and mute
                    case "VOLUME_QUERY":
                        if (handler.volumeEnabled) {
                            handler.connector.sendCommand("SEND_EVENT:VOLUME_CAPABILITIES=15");
                            handler.connector.sendCommand("SEND_EVENT:VOLUME_LEVEL=" + handler.volume);
                            handler.connector
                                    .sendCommand("SEND_EVENT:MUTE_" + (handler.isMuted ? "ON" : "OFF") + "_FB");
                        }
                        break;
                    case "VOLUME_UP":
                        if (handler.volumeEnabled) {
                            handler.volume++;
                            handler.updateChannel(KaleidescapeBindingConstants.VOLUME,
                                    new PercentType(BigDecimal.valueOf(handler.volume)));
                            handler.connector.sendCommand("SEND_EVENT:VOLUME_LEVEL=" + handler.volume);
                        }
                        break;
                    case "VOLUME_DOWN":
                        if (handler.volumeEnabled) {
                            handler.volume--;
                            handler.updateChannel(KaleidescapeBindingConstants.VOLUME,
                                    new PercentType(BigDecimal.valueOf(handler.volume)));
                            handler.connector.sendCommand("SEND_EVENT:VOLUME_LEVEL=" + handler.volume);
                        }
                        break;
                    case "TOGGLE_MUTE":
                        State state = UnDefType.UNDEF;
                        if (handler.isMuted) {
                            state = OnOffType.OFF;
                            handler.isMuted = false;
                        } else {
                            state = OnOffType.ON;
                            handler.isMuted = true;
                        }
                        handler.connector.sendCommand("SEND_EVENT:MUTE_" + (handler.isMuted ? "ON" : "OFF") + "_FB");
                        handler.updateChannel(KaleidescapeBindingConstants.MUTE, state);
                        break;
                    // the default is to just publish all other USER_DEFINED_EVENTs
                    default:
                        handler.updateChannel(KaleidescapeBindingConstants.USER_DEFINED_EVENT, new StringType(message));
                }
            } catch (KaleidescapeException e) {
                logger.debug("USER_DEFINED_EVENT - exception on message: {}", message);
            }
        }
    },
    USER_INPUT {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 01:Search for title:ABC
            handler.updateChannel(KaleidescapeBindingConstants.USER_INPUT, new StringType(message));
        }
    },
    USER_INPUT_PROMPT {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 00:00::00:0:1
            handler.updateChannel(KaleidescapeBindingConstants.USER_INPUT, new StringType(message));
        }
    },
    SYSTEM_READINESS_STATE {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example 1, 2 or 3
            handler.updateChannel(KaleidescapeBindingConstants.SYSTEM_READINESS_STATE,
                    new StringType(KaleidescapeStatusCodes.READINESS_STATE.get(message)));
        }
    },
    SYSTEM_VERSION {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 16:8.6.0-21023
            // protocol version, kOS version
            final Pattern p = Pattern.compile("^(\\d{2}):(.*)$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.PROTOCOL_VERSION, new StringType(matcher.group(1)));
                handler.updateChannel(KaleidescapeBindingConstants.SYSTEM_VERSION, new StringType(matcher.group(2)));
            } else {
                logger.debug("SYSTEM_VERSION - no match on message: {}", message);
            }
        }
    },
    DEVICE_INFO {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

            // example: 07:000000000000558F:00:192.168.001.100
            // device type (deprecated), serial number, cpdid, ip address
            final Pattern p = Pattern.compile("^(\\d{2}):(.*):(\\d{2}):(.*)$");

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(KaleidescapeBindingConstants.SERIAL_NUMBER,
                        new StringType(matcher.group(2).replaceFirst("^0+(?!$)", ""))); // take off leading zeros

                handler.updateChannel(KaleidescapeBindingConstants.CONTROL_PROTOCOL_ID,
                        new StringType(matcher.group(3)));
            } else {
                logger.debug("DEVICE_INFO - no match on message: {}", message);
            }
        }
    },
    DEVICE_TYPE_NAME {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 'Player' or 'Strato'
            handler.updateChannel(KaleidescapeBindingConstants.COMPONENT_TYPE, new StringType(message));
        }
    },
    FRIENDLY_NAME {
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 'Living Room'
            handler.friendlyName = message;
            handler.updateChannel(KaleidescapeBindingConstants.FRIENDLY_NAME, new StringType(message));
        }
    };

    public abstract void handleMessage(String message, KaleidescapeHandler handler);
}
