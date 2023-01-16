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

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TvContext {
    private @Nullable Channel channel;
    private PlayerContext.@Nullable Player player;

    public class Channel {
        @SerializedName("bouquetId")
        private long bouquetId;

        @SerializedName("bouquetName")
        private @Nullable String bouquetName;

        @SerializedName("bouquetType")
        private BouquetType bouquetType = BouquetType.UNKNOWN;

        @SerializedName("channelName")
        private @Nullable String channelName;

        @SerializedName("channelNumber")
        private int channelNumber;

        @SerializedName("channelSubNumber")
        private int channelSubNumber;

        @SerializedName("channelType")
        private ChannelType channelType = ChannelType.UNKNOWN;

        @SerializedName("channelUuid")
        private @Nullable String channelUuid;

        @SerializedName("currentServiceIndex")
        private int currentServiceIndex;

        @SerializedName("isTimeShifting")
        private boolean isTimeShifting;

        private List<Service> services = List.of();

        @SerializedName("videoIsVisible")
        private boolean videoIsVisible;

        public enum BouquetType {
            ADSL,
            UNKNOWN;
        }

        public enum ChannelType {
            REGULAR,
            UNKNOWN;
        }

        public class Service {
            private long id;
            private @Nullable String name;

            @SerializedName("qualityLabel")
            private @Nullable String qualityLabel;

            @SerializedName("qualityName")
            private @Nullable String qualityName;

            @SerializedName("sortInfo")
            private int sortInfo;

            @SerializedName("typeLabel")
            private @Nullable String typeLabel;

            @SerializedName("typeName")
            private @Nullable String typeName;

            private @Nullable String url;

            public long getId() {
                return id;
            }

            public @Nullable String getName() {
                return name;
            }

            public @Nullable String getQualityLabel() {
                return qualityLabel;
            }

            public @Nullable String getQualityName() {
                return qualityName;
            }

            public int getSortInfo() {
                return sortInfo;
            }

            public @Nullable String getTypeLabel() {
                return typeLabel;
            }

            public @Nullable String getTypeName() {
                return typeName;
            }

            public @Nullable String getUrl() {
                return url;
            }
        }

        public long getBouquetId() {
            return bouquetId;
        }

        public @Nullable String getBouquetName() {
            return bouquetName;
        }

        public BouquetType getBouquetType() {
            return bouquetType;
        }

        public @Nullable String getChannelName() {
            return channelName;
        }

        public int getChannelNumber() {
            return channelNumber;
        }

        public int getChannelSubNumber() {
            return channelSubNumber;
        }

        public ChannelType getChannelType() {
            return channelType;
        }

        public @Nullable String getChannelUuid() {
            return channelUuid;
        }

        public int getCurrentServiceIndex() {
            return currentServiceIndex;
        }

        public List<Service> getServices() {
            return services;
        }

        public boolean getVideoIsVisible() {
            return videoIsVisible;
        }

        public boolean isTimeShifting() {
            return isTimeShifting;
        }
    }

    public @Nullable Channel getChannel() {
        return channel;
    }

    public PlayerContext.@Nullable Player getPlayer() {
        return player;
    }
}
