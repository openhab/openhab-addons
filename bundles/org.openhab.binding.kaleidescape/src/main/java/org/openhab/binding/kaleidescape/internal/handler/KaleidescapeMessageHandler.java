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
package org.openhab.binding.kaleidescape.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeFormatter;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeStatusCodes;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.HIGHLIGHTED_SELECTION, new StringType(message));

            if (handler.isLoadHighlightedDetails) {
                try {
                    handler.connector.sendCommand(GET_CONTENT_DETAILS + message + ":");
                } catch (KaleidescapeException e) {
                    logger.debug("GET_CONTENT_DETAILS - exception loading content details for handle: {}", message);
                }
            }
        }
    },
    DEVICE_POWER_STATE {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 1:1
        // power_state, zone 1 state, zone n state
        private final Pattern p = Pattern.compile("^(\\d{1}):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(POWER, OnOffType.from(ONE.equals(matcher.group(1))));
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 0:0:00:00000:00000:000:00000:00000
        // mode, speed, title_num, title_length, title_loc, chapter_num, chapter_length, chapter_loc
        private final Pattern p = Pattern
                .compile("^(\\d{1}):(\\d{1}):(\\d{2}):(\\d{5}):(\\d{5}):(\\d{3}):(\\d{5}):(\\d{5})$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(PLAY_MODE,
                        new StringType(KaleidescapeStatusCodes.PLAY_MODE.get(matcher.group(1))));

                handler.updateChannel(CONTROL, "2".equals(matcher.group(1)) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);

                handler.updateChannel(PLAY_SPEED, new StringType(matcher.group(2)));

                handler.updateChannel(TITLE_NUM, new DecimalType(Integer.parseInt(matcher.group(3))));

                final int titleLength = Integer.parseInt(matcher.group(4));
                final int titleLoc = Integer.parseInt(matcher.group(5));

                handler.updateChannel(TITLE_LENGTH, new QuantityType<>(titleLength, handler.apiSecondUnit));

                handler.updateChannel(TITLE_LOC, new QuantityType<>(titleLoc, handler.apiSecondUnit));

                handler.updateChannel(ENDTIME, titleLength < 1 ? UnDefType.UNDEF
                        : new DateTimeType(ZonedDateTime.now().plusSeconds(titleLength - titleLoc)));

                handler.updateChannel(CHAPTER_NUM, new DecimalType(Integer.parseInt(matcher.group(6))));

                handler.updateChannel(CHAPTER_LENGTH,
                        new QuantityType<>(Integer.parseInt(matcher.group(7)), handler.apiSecondUnit));

                handler.updateChannel(CHAPTER_LOC,
                        new QuantityType<>(Integer.parseInt(matcher.group(8)), handler.apiSecondUnit));
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 00:00:00
        // composite, component, hdmi
        private final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.VIDEO_MODE, new StringType(message));

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(VIDEO_MODE_COMPOSITE,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(1))));

                handler.updateChannel(VIDEO_MODE_COMPONENT,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(2))));

                handler.updateChannel(VIDEO_MODE_HDMI,
                        new StringType(KaleidescapeStatusCodes.VIDEO_MODE.get(matcher.group(3))));
            } else {
                logger.debug("VIDEO_MODE - no match on message: {}", message);
            }
        }
    },
    VIDEO_COLOR {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 02:01:24:01
        // eotf, color_space, color_depth, color_sampling
        private final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2})$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.VIDEO_COLOR, new StringType(message));

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(VIDEO_COLOR_EOTF,
                        new StringType(KaleidescapeStatusCodes.EOTF.get(matcher.group(1))));
            } else {
                logger.debug("VIDEO_COLOR - no match on message: {}", message);
            }
        }
    },
    CONTENT_COLOR {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 02:01:24:01
        // eotf, color_space, color_depth, color_sampling
        private final Pattern p = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2})$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            handler.updateChannel(KaleidescapeBindingConstants.CONTENT_COLOR, new StringType(message));

            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(CONTENT_COLOR_EOTF,
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
            if (!message.equals(EMPTY)) {
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: You:Radiohead:Pablo Honey:1.9b5f4d786d7e2c49-t301_577:1.R_1493833:2.200c5
        // track, artist, album, track handle, album handle, now playing handle
        private final Pattern p = Pattern.compile("^(.*):(.*):(.*):(.*):(.*):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // first replace delimited : in track/artist/album name with ||, fix it later in formatString()
            Matcher matcher = p.matcher(message.replace("\\:", "||"));
            if (matcher.find()) {
                // if not an empty message, the colon delimiters in raw MUSIC_TITLE message are changed to pipe
                handler.updateChannel(MUSIC_TITLE_RAW, ":::::".equals(matcher.group(0)) ? UnDefType.NULL
                        : new StringType(KaleidescapeFormatter.formatString(matcher.group(0).replace(":", "|"))));

                handler.updateChannel(MUSIC_TRACK,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(1))));

                handler.updateChannel(MUSIC_ARTIST,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(2))));

                handler.updateChannel(MUSIC_ALBUM,
                        new StringType(KaleidescapeFormatter.formatString(matcher.group(3))));

                handler.updateChannel(MUSIC_TRACK_HANDLE, new StringType(matcher.group(4)));

                handler.updateChannel(MUSIC_ALBUM_HANDLE, new StringType(matcher.group(5)));

                handler.updateChannel(MUSIC_NOWPLAY_HANDLE, new StringType(matcher.group(6)));

                if (handler.isLoadAlbumDetails) {
                    try {
                        handler.connector.sendCommand(GET_CONTENT_DETAILS + matcher.group(5) + ":");
                    } catch (KaleidescapeException e) {
                        logger.debug("GET_CONTENT_DETAILS - exception loading album details for handle: {}",
                                matcher.group(5));
                    }
                }
            } else {
                logger.debug("MUSIC_TITLE - no match on message: {}", message);
            }
        }
    },
    MUSIC_PLAY_STATUS {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 2:0:00207:+00000:000.00
        // 2:0:00331:+00183:055.29
        // mode, speed, track length, track position, track progress %
        private final Pattern p = Pattern.compile("^(\\d{1}):(\\d{1}):(\\d{5}):(.\\d{5}):(\\d{3}\\.\\d{2})$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateChannel(MUSIC_PLAY_MODE,
                        new StringType(KaleidescapeStatusCodes.PLAY_MODE.get(matcher.group(1))));

                handler.updateChannel(MUSIC_CONTROL,
                        "2".equals(matcher.group(1)) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);

                handler.updateChannel(MUSIC_PLAY_SPEED, new StringType(matcher.group(2)));

                handler.updateChannel(MUSIC_TRACK_LENGTH,
                        new QuantityType<>(Integer.parseInt(matcher.group(3)), handler.apiSecondUnit));

                handler.updateChannel(MUSIC_TRACK_POSITION,
                        new QuantityType<>(Integer.parseInt(matcher.group(4)), handler.apiSecondUnit));

                handler.updateChannel(MUSIC_TRACK_PROGRESS,
                        new DecimalType(BigDecimal.valueOf(Math.round(Double.parseDouble(matcher.group(5))))));
            } else {
                logger.debug("MUSIC_PLAY_STATUS - no match on message: {}", message);
            }
        }
    },
    MUSIC_NOW_PLAYING_STATUS {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 00013:00000:0:0:0000000238:2.200c5
        // total # tracks in list, list index, repeat, random, generation, now_playing handle
        // only using repeat & random right now
        private final Pattern p = Pattern.compile("^(\\d{5}):(\\d{5}):(\\d{1}):(\\d{1}):(\\d{10}):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                // update REPEAT switch state
                handler.updateChannel(MUSIC_REPEAT, OnOffType.from(ONE.equals(matcher.group(3))));

                // update RANDOM switch state
                handler.updateChannel(MUSIC_RANDOM, OnOffType.from(ONE.equals(matcher.group(4))));
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // g1=meta id, g2=meta type, g3=data
        // example: 6:Year:1995
        // or: 10:Genres:Pop\/Rock
        private final Pattern p = Pattern.compile("^(\\d{1,2}):([^:^/]*):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                String metaType = matcher.group(2).toLowerCase();
                String value = KaleidescapeFormatter.formatString(matcher.group(3));

                // the CONTENT_DETAILS message with id=1 tells us what type of meta data is coming
                if (ONE.equals(matcher.group(1))) {
                    if ((CONTENT_HANDLE).equals(metaType)) {
                        handler.updateDetailChannel(DETAIL_TYPE, new StringType(MOVIE));
                        handler.metaRuntimeMultiple = 60;

                        // null out album specific
                        handler.updateDetailChannel(DETAIL_ALBUM_TITLE, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_ARTIST, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_REVIEW, UnDefType.NULL);

                    } else if ((ALBUM_CONTENT_HANDLE).equals(metaType)) {
                        handler.updateDetailChannel(DETAIL_TYPE, new StringType(ALBUM));
                        handler.metaRuntimeMultiple = 1;

                        // null out movie specific
                        handler.updateDetailChannel(DETAIL_TITLE, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_RATING, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_ACTORS, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_DIRECTORS, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_RATING_REASON, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_SYNOPSIS, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_COLOR_DESCRIPTION, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_COUNTRY, UnDefType.NULL);
                        handler.updateDetailChannel(DETAIL_ASPECT_RATIO, UnDefType.NULL);

                    } else {
                        handler.updateDetailChannel(DETAIL_TYPE, UnDefType.UNDEF);
                    }
                    // otherwise update the channel if it is one we care about
                } else if (METADATA_CHANNELS.contains(metaType)) {
                    // special case for cover art image
                    if (DETAIL_COVER_URL.equals(metaType)) {
                        handler.updateDetailChannel(metaType, new StringType(value));
                        if (!value.isEmpty() && handler.isChannelLinked(DETAIL + DETAIL_COVER_ART)) {
                            try {
                                ContentResponse contentResponse = handler.httpClient.newRequest(value).method(GET)
                                        .timeout(10, TimeUnit.SECONDS).send();
                                int httpStatus = contentResponse.getStatus();
                                if (httpStatus == OK_200) {
                                    handler.updateDetailChannel(DETAIL_COVER_ART,
                                            new RawType(contentResponse.getContent(), "image/jpeg"));
                                } else {
                                    handler.updateDetailChannel(DETAIL_COVER_ART, UnDefType.NULL);
                                }
                            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                logger.debug("Error updating Cover Art Image channel for url: {}", value);
                                handler.updateDetailChannel(DETAIL_COVER_ART, UnDefType.NULL);
                            }
                        } else {
                            handler.updateDetailChannel(DETAIL_COVER_ART, UnDefType.NULL);
                        }
                        // special case for running time to create a QuantityType<Time>
                    } else if (DETAIL_RUNNING_TIME.equals(metaType)) {
                        handler.updateDetailChannel(DETAIL_RUNNING_TIME, new QuantityType<>(
                                Integer.parseInt(value) * handler.metaRuntimeMultiple, handler.apiSecondUnit));
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: SELECT_KALEIDESCAPE_INPUT
            try {
                switch (message) {
                    // when the ipad or phone app is started up, it sends a VOLUME_QUERY,
                    // so we respond to enable volume controls and set the initial volume and mute
                    case "VOLUME_QUERY":
                        if (handler.volumeEnabled) {
                            synchronized (handler.sequenceLock) {
                                handler.connector.sendCommand(SEND_EVENT_VOLUME_CAPABILITIES_15);
                                handler.connector.sendCommand(SEND_EVENT_VOLUME_LEVEL_EQ + handler.volume);
                                handler.connector.sendCommand(SEND_EVENT_MUTE + (handler.isMuted ? MUTE_ON : MUTE_OFF));
                            }
                        } else if (handler.volumeBasicEnabled) {
                            synchronized (handler.sequenceLock) {
                                handler.connector.sendCommand(SEND_EVENT_VOLUME_CAPABILITIES_3);
                            }
                        }
                        break;
                    case "VOLUME_UP":
                        if (handler.volumeEnabled) {
                            synchronized (handler.sequenceLock) {
                                handler.volume++;
                                handler.updateChannel(VOLUME, new PercentType(BigDecimal.valueOf(handler.volume)));
                                handler.connector.sendCommand(SEND_EVENT_VOLUME_LEVEL_EQ + handler.volume);
                            }
                        }
                        break;
                    case "VOLUME_DOWN":
                        if (handler.volumeEnabled) {
                            synchronized (handler.sequenceLock) {
                                handler.volume--;
                                handler.updateChannel(VOLUME, new PercentType(BigDecimal.valueOf(handler.volume)));
                                handler.connector.sendCommand(SEND_EVENT_VOLUME_LEVEL_EQ + handler.volume);
                            }
                        }
                        break;
                    case "TOGGLE_MUTE":
                        if (handler.volumeEnabled) {
                            State state = UnDefType.UNDEF;
                            synchronized (handler.sequenceLock) {
                                if (handler.isMuted) {
                                    state = OnOffType.OFF;
                                    handler.isMuted = false;
                                } else {
                                    state = OnOffType.ON;
                                    handler.isMuted = true;
                                }
                                handler.connector.sendCommand(SEND_EVENT_MUTE + (handler.isMuted ? MUTE_ON : MUTE_OFF));
                                handler.updateChannel(MUTE, state);
                            }
                        }
                        break;
                }
                handler.updateChannel(KaleidescapeBindingConstants.USER_DEFINED_EVENT, new StringType(message));
            } catch (KaleidescapeException e) {
                logger.debug("USER_DEFINED_EVENT - exception on message: {}", message);
            }
        }
    },
    USER_INPUT {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 01:Search for title:ABC
            handler.updateChannel(KaleidescapeBindingConstants.USER_INPUT, new StringType(message));
        }
    },
    USER_INPUT_PROMPT {
        @Override
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
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 16:8.6.0-21023
        // protocol version, kOS version
        private final Pattern p = Pattern.compile("^(\\d{2}):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                handler.updateThingProperty(PROPERTY_PROTOCOL_VERSION, matcher.group(1));
                handler.updateThingProperty(PROPERTY_SYSTEM_VERSION, matcher.group(2));
            } else {
                logger.debug("SYSTEM_VERSION - no match on message: {}", message);
            }
        }
    },
    DEVICE_INFO {
        private final Logger logger = LoggerFactory.getLogger(KaleidescapeMessageHandler.class);

        // example: 07:000000000000558F:00:192.168.001.100
        // device type (deprecated), serial number, cpdid, ip address
        private final Pattern p = Pattern.compile("^(\\d{2}):(.*):(\\d{2}):(.*)$");

        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            Matcher matcher = p.matcher(message);
            if (matcher.find()) {
                // replaceFirst takes off leading zeros
                handler.updateThingProperty(PROPERTY_SERIAL_NUMBER, matcher.group(2).replaceFirst("^0+(?!$)", EMPTY));
                handler.updateThingProperty(PROPERTY_CONTROL_PROTOCOL_ID, matcher.group(3));
            } else {
                logger.debug("DEVICE_INFO - no match on message: {}", message);
            }
        }
    },
    DEVICE_TYPE_NAME {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 'Player' or 'Strato'
            handler.updateThingProperty(PROPERTY_COMPONENT_TYPE, message);
        }
    },
    FRIENDLY_NAME {
        @Override
        public void handleMessage(String message, KaleidescapeHandler handler) {
            // example: 'Living Room'
            handler.friendlyName = message;
            handler.updateThingProperty(PROPERTY_FRIENDLY_NAME, message);
        }
    };

    public abstract void handleMessage(String message, KaleidescapeHandler handler);
}
