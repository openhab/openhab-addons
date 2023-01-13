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
package org.openhab.binding.freeboxos.internal.api.player;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.PlaybackState;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.SubtitleTrack;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.VideoTrack;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerContext {
    private final Player player;

    public Player getPlayer() {
        return player;
    }

    public static final class Player {
        private final int audioIndex;
        private final List<AudioTrack> audioList;
        private final long curPos;
        private final int duration;
        private final long livePos;
        private final long maxPos;
        private final MediaState mediaState;
        private final Metadata metadata;
        private final long minPos;
        private final PlaybackState playbackState;
        private final long position;
        private final String source;
        private final int subtitleIndex;
        private final List<SubtitleTrack> subtitleList;
        private final int videoIndex;
        private final List<VideoTrack> videoList;

        public static final class AudioTrack {
            private final int bitrate;
            private final int channelCount;
            private final String codec;
            private final String codecId;
            private final String language;
            private final String metadataId;
            private final int pid;
            private final int samplerate;
            private final long uid;

            public AudioTrack(int pid, long uid, int samplerate, int channelCount, int bitrate, String codec,
                    String codecId, String language, String metadataId) {
                this.pid = pid;
                this.uid = uid;
                this.samplerate = samplerate;
                this.channelCount = channelCount;
                this.bitrate = bitrate;
                this.codec = codec;
                this.codecId = codecId;
                this.language = language;
                this.metadataId = metadataId;
            }

            public int getBitrate() {
                return this.bitrate;
            }

            public int getChannelCount() {
                return this.channelCount;
            }

            public String getCodec() {
                return this.codec;
            }

            public String getCodecId() {
                return this.codecId;
            }

            public String getLanguage() {
                return this.language;
            }

            public String getMetadataId() {
                return this.metadataId;
            }

            public int getPid() {
                return this.pid;
            }

            public int getSamplerate() {
                return this.samplerate;
            }

            public long getUid() {
                return this.uid;
            }

        }

        public enum MediaState {
            ready
        }

        public static final class Metadata {
            private final String album;
            private final String albumArtist;
            private final String artist;
            private final String author;
            private final int bpm;
            private final String comment;
            private final boolean compilation;
            private final String composer;
            private final String container;
            private final String copyright;
            private final long date;
            private final String discId;
            private final int discNumber;
            private final int discTotal;
            private final String genre;
            private final String musicbrainzDiscId;
            private final String performer;
            private final String title;
            private final int trackNumber;
            private final int trackTotal;
            private final String url;

            public Metadata(String musicbrainzDiscId, String discId, long date, String genre, String composer,
                    String albumArtist, String url, String container, int discNumber, String copyright, String author,
                    String artist, int trackTotal, int discTotal, int trackNumber, String title, String album, int bpm,
                    boolean compilation, String comment, String performer) {
                this.musicbrainzDiscId = musicbrainzDiscId;
                this.discId = discId;
                this.date = date;
                this.genre = genre;
                this.composer = composer;
                this.albumArtist = albumArtist;
                this.url = url;
                this.container = container;
                this.discNumber = discNumber;
                this.copyright = copyright;
                this.author = author;
                this.artist = artist;
                this.trackTotal = trackTotal;
                this.discTotal = discTotal;
                this.trackNumber = trackNumber;
                this.title = title;
                this.album = album;
                this.bpm = bpm;
                this.compilation = compilation;
                this.comment = comment;
                this.performer = performer;
            }

            public String getAlbum() {
                return this.album;
            }

            public String getAlbumArtist() {
                return this.albumArtist;
            }

            public String getArtist() {
                return this.artist;
            }

            public String getAuthor() {
                return this.author;
            }

            public int getBpm() {
                return this.bpm;
            }

            public String getComment() {
                return this.comment;
            }

            public boolean getCompilation() {
                return this.compilation;
            }

            public String getComposer() {
                return this.composer;
            }

            public String getContainer() {
                return this.container;
            }

            public String getCopyright() {
                return this.copyright;
            }

            public long getDate() {
                return this.date;
            }

            public String getDiscId() {
                return this.discId;
            }

            public int getDiscNumber() {
                return this.discNumber;
            }

            public int getDiscTotal() {
                return this.discTotal;
            }

            public String getGenre() {
                return this.genre;
            }

            public String getMusicbrainzDiscId() {
                return this.musicbrainzDiscId;
            }

            public String getPerformer() {
                return this.performer;
            }

            public String getTitle() {
                return this.title;
            }

            public int getTrackNumber() {
                return this.trackNumber;
            }

            public int getTrackTotal() {
                return this.trackTotal;
            }

            public String getUrl() {
                return this.url;
            }

            public enum PlaybackState {
                play,
                pause
            }

            public static final class SubtitleTrack {
                private final String codec;
                private final String language;
                private final String pid;
                private final Type type;
                private final String uid;

                public enum Type {
                    Normal,
                    HearingImpaired
                }

                public SubtitleTrack(String codec, String pid, String uid, String language, Type type) {
                    this.codec = codec;
                    this.pid = pid;
                    this.uid = uid;
                    this.language = language;
                    this.type = type;
                }

                public String getCodec() {
                    return this.codec;
                }

                public String getLanguage() {
                    return this.language;
                }

                public String getPid() {
                    return this.pid;
                }

                public Type getType() {
                    return this.type;
                }

                public String getUid() {
                    return this.uid;
                }
            }

            public static final class VideoTrack {
                private final int bitrate;
                private final String codec;
                private final Framerate framerate;
                private final int height;
                private final int pid;
                private final int uid;
                private final int width;

                public static final class Framerate {
                    private final int den;
                    private final int num;

                    public Framerate(int den, int num) {
                        this.den = den;
                        this.num = num;
                    }

                    public int getDen() {
                        return this.den;
                    }

                    public int getNum() {
                        return this.num;
                    }
                }

                public VideoTrack(int bitrate, String codec, int pid, int height, Framerate framerate, int uid,
                        int width) {
                    this.bitrate = bitrate;
                    this.codec = codec;
                    this.pid = pid;
                    this.height = height;
                    this.framerate = framerate;
                    this.uid = uid;
                    this.width = width;
                }

                public int getBitrate() {
                    return this.bitrate;
                }

                public String getCodec() {
                    return this.codec;
                }

                public Framerate getFramerate() {
                    return this.framerate;
                }

                public int getHeight() {
                    return this.height;
                }

                public int getPid() {
                    return this.pid;
                }

                public int getUid() {
                    return this.uid;
                }

                public int getWidth() {
                    return this.width;
                }
            }
        }

        public Player(String source, PlaybackState playbackState, long j, long j2, long j3, long j4, long j5, int i,
                MediaState mediaState, int i2, int i3, int i4, Metadata metadata, List<AudioTrack> audioList,
                List<VideoTrack> videoList, List<SubtitleTrack> subtitleList) {
            this.source = source;
            this.playbackState = playbackState;
            this.livePos = j;
            this.minPos = j2;
            this.maxPos = j3;
            this.curPos = j4;
            this.position = j5;
            this.duration = i;
            this.mediaState = mediaState;
            this.subtitleIndex = i2;
            this.audioIndex = i3;
            this.videoIndex = i4;
            this.metadata = metadata;
            this.audioList = audioList;
            this.videoList = videoList;
            this.subtitleList = subtitleList;
        }

        public int getAudioIndex() {
            return this.audioIndex;
        }

        public List<AudioTrack> getAudioList() {
            return this.audioList;
        }

        public long getCurPos() {
            return this.curPos;
        }

        public int getDuration() {
            return this.duration;
        }

        public long getLivePos() {
            return this.livePos;
        }

        public long getMaxPos() {
            return this.maxPos;
        }

        public MediaState getMediaState() {
            return this.mediaState;
        }

        public Metadata getMetadata() {
            return this.metadata;
        }

        public long getMinPos() {
            return this.minPos;
        }

        public PlaybackState getPlaybackState() {
            return this.playbackState;
        }

        public long getPosition() {
            return this.position;
        }

        public String getSource() {
            return this.source;
        }

        public int getSubtitleIndex() {
            return this.subtitleIndex;
        }

        public List<SubtitleTrack> getSubtitleList() {
            return this.subtitleList;
        }

        public int getVideoIndex() {
            return this.videoIndex;
        }

        public List<VideoTrack> getVideoList() {
            return this.videoList;
        }
    }

    public PlayerContext(Player player) {
        this.player = player;
    }

}
