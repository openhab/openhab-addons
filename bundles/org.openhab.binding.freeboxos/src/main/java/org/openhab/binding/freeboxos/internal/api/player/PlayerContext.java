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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.PlaybackState;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.SubtitleTrack;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext.Player.Metadata.VideoTrack;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerContext {
    private @Nullable Player player;

    public @Nullable Player getPlayer() {
        return player;
    }

    public class Player {
        @SerializedName("audioIndex")
        private int audioIndex;

        @SerializedName("audioList")
        private List<AudioTrack> audioList = List.of();

        @SerializedName("curPos")
        private long curPos;

        private int duration;

        @SerializedName("livePos")
        private long livePos;

        @SerializedName("maxPos")
        private long maxPos;

        @SerializedName("mediaState")
        private MediaState mediaState = MediaState.UNKNOWN;

        private @Nullable Metadata metadata;

        @SerializedName("minPos")
        private long minPos;

        @SerializedName("playbackState")
        private PlaybackState playbackState = PlaybackState.UNKNOWN;

        private long position;

        private @Nullable String source;

        @SerializedName("subtitleIndex")
        private int subtitleIndex;

        @SerializedName("subtitleList")
        private List<SubtitleTrack> subtitleList = List.of();

        @SerializedName("videoIndex")
        private int videoIndex;

        @SerializedName("videoList")
        private List<VideoTrack> videoList = List.of();

        public class AudioTrack {
            private int bitrate;

            @SerializedName("channelCount")
            private int channelCount;

            private @Nullable String codec;

            @SerializedName("codecId")
            private @Nullable String codecId;

            private @Nullable String language;

            @SerializedName("metadataId")
            private @Nullable String metadataId;

            private int pid;
            private int samplerate;
            private long uid;

            public int getBitrate() {
                return bitrate;
            }

            public int getChannelCount() {
                return channelCount;
            }

            public @Nullable String getCodec() {
                return codec;
            }

            public @Nullable String getCodecId() {
                return codecId;
            }

            public @Nullable String getLanguage() {
                return language;
            }

            public @Nullable String getMetadataId() {
                return metadataId;
            }

            public int getPid() {
                return pid;
            }

            public int getSamplerate() {
                return samplerate;
            }

            public long getUid() {
                return uid;
            }

        }

        public enum MediaState {
            READY,
            UNKNOWN;
        }

        public class Metadata {
            private @Nullable String album;

            @SerializedName("albumArtist")
            private @Nullable String albumArtist;

            private @Nullable String artist;
            private @Nullable String author;
            private int bpm;
            private @Nullable String comment;
            private boolean compilation;
            private @Nullable String composer;
            private @Nullable String container;
            private @Nullable String copyright;
            private long date;

            @SerializedName("discId")
            private @Nullable String discId;

            @SerializedName("discNumber")
            private int discNumber;

            @SerializedName("discTotal")
            private int discTotal;

            private @Nullable String genre;

            @SerializedName("musicbrainzDiscId")
            private @Nullable String musicbrainzDiscId;

            private @Nullable String performer;
            private @Nullable String title;

            @SerializedName("trackNumber")
            private int trackNumber;

            @SerializedName("trackTotal")
            private int trackTotal;

            private @Nullable String url;

            public @Nullable String getAlbum() {
                return album;
            }

            public @Nullable String getAlbumArtist() {
                return albumArtist;
            }

            public @Nullable String getArtist() {
                return artist;
            }

            public @Nullable String getAuthor() {
                return author;
            }

            public int getBpm() {
                return bpm;
            }

            public @Nullable String getComment() {
                return comment;
            }

            public boolean getCompilation() {
                return compilation;
            }

            public @Nullable String getComposer() {
                return composer;
            }

            public @Nullable String getContainer() {
                return container;
            }

            public @Nullable String getCopyright() {
                return copyright;
            }

            public long getDate() {
                return date;
            }

            public @Nullable String getDiscId() {
                return discId;
            }

            public int getDiscNumber() {
                return discNumber;
            }

            public int getDiscTotal() {
                return discTotal;
            }

            public @Nullable String getGenre() {
                return genre;
            }

            public @Nullable String getMusicbrainzDiscId() {
                return musicbrainzDiscId;
            }

            public @Nullable String getPerformer() {
                return performer;
            }

            public @Nullable String getTitle() {
                return title;
            }

            public int getTrackNumber() {
                return trackNumber;
            }

            public int getTrackTotal() {
                return trackTotal;
            }

            public @Nullable String getUrl() {
                return url;
            }

            public enum PlaybackState {
                PLAY,
                PAUSE,
                UNKNOWN;
            }

            public class SubtitleTrack {
                private @Nullable String codec;
                private @Nullable String language;
                private @Nullable String pid;
                private Type type = Type.UNKNOWN;
                private @Nullable String uid;

                public enum Type {
                    NORMAL,
                    HEARINGIMPAIRED,
                    UNKNOWN;
                }

                public @Nullable String getCodec() {
                    return codec;
                }

                public @Nullable String getLanguage() {
                    return language;
                }

                public @Nullable String getPid() {
                    return pid;
                }

                public Type getType() {
                    return type;
                }

                public @Nullable String getUid() {
                    return uid;
                }
            }

            public class VideoTrack {
                private int bitrate;
                private @Nullable String codec;
                private @Nullable Framerate framerate;
                private int height;
                private int pid;
                private int uid;
                private int width;

                public class Framerate {
                    private int den;
                    private int num;

                    public int getDen() {
                        return den;
                    }

                    public int getNum() {
                        return num;
                    }
                }

                public int getBitrate() {
                    return bitrate;
                }

                public @Nullable String getCodec() {
                    return codec;
                }

                public @Nullable Framerate getFramerate() {
                    return framerate;
                }

                public int getHeight() {
                    return height;
                }

                public int getPid() {
                    return pid;
                }

                public int getUid() {
                    return uid;
                }

                public int getWidth() {
                    return width;
                }
            }
        }

        public int getAudioIndex() {
            return audioIndex;
        }

        public List<AudioTrack> getAudioList() {
            return audioList;
        }

        public long getCurPos() {
            return curPos;
        }

        public int getDuration() {
            return duration;
        }

        public long getLivePos() {
            return livePos;
        }

        public long getMaxPos() {
            return maxPos;
        }

        public MediaState getMediaState() {
            return mediaState;
        }

        public @Nullable Metadata getMetadata() {
            return metadata;
        }

        public long getMinPos() {
            return minPos;
        }

        public PlaybackState getPlaybackState() {
            return playbackState;
        }

        public long getPosition() {
            return position;
        }

        public @Nullable String getSource() {
            return source;
        }

        public int getSubtitleIndex() {
            return subtitleIndex;
        }

        public List<SubtitleTrack> getSubtitleList() {
            return subtitleList;
        }

        public int getVideoIndex() {
            return videoIndex;
        }

        public List<VideoTrack> getVideoList() {
            return videoList;
        }
    }

}
