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

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TvContext {
    private final Channel channel;
    private final PlayerContext.Player player;

    public static final class Channel {
        private final long bouquetId;
        private final String bouquetName;
        private final BouquetType bouquetType;
        private final String channelName;
        private final int channelNumber;
        private final int channelSubNumber;
        private final ChannelType channelType;
        private final String channelUuid;
        private final int currentServiceIndex;
        private final boolean isTimeShifting;
        private final List<Service> services;
        private final boolean videoIsVisible;

        public enum BouquetType {
            adsl
        }

        public enum ChannelType {
            regular
        }

        public static final class Service {
            private final long id;
            private final String name;
            private final String qualityLabel;
            private final String qualityName;
            private final int sortInfo;
            private final String typeLabel;
            private final String typeName;
            private final String url;

            public Service(String qualityName, String qualityLabel, String name, int sortInfo, long id, String url,
                    String typeName, String typeLabel) {
                this.qualityName = qualityName;
                this.qualityLabel = qualityLabel;
                this.name = name;
                this.sortInfo = sortInfo;
                this.id = id;
                this.url = url;
                this.typeName = typeName;
                this.typeLabel = typeLabel;
            }

            public long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public String getQualityLabel() {
                return this.qualityLabel;
            }

            public String getQualityName() {
                return this.qualityName;
            }

            public int getSortInfo() {
                return this.sortInfo;
            }

            public String getTypeLabel() {
                return this.typeLabel;
            }

            public String getTypeName() {
                return this.typeName;
            }

            public String getUrl() {
                return this.url;
            }
        }

        public Channel(boolean isTimeShifting, boolean videoIsVisible, String bouquetName, String channelName,
                long bouquetId, ChannelType channelType, String channelUuid, BouquetType bouquetType, int channelNumber,
                int channelSubNumber, int currentServiceIndex, List<Service> services) {
            this.isTimeShifting = isTimeShifting;
            this.videoIsVisible = videoIsVisible;
            this.bouquetName = bouquetName;
            this.channelName = channelName;
            this.bouquetId = bouquetId;
            this.channelType = channelType;
            this.channelUuid = channelUuid;
            this.bouquetType = bouquetType;
            this.channelNumber = channelNumber;
            this.channelSubNumber = channelSubNumber;
            this.currentServiceIndex = currentServiceIndex;
            this.services = services;
        }

        public long getBouquetId() {
            return this.bouquetId;
        }

        public String getBouquetName() {
            return this.bouquetName;
        }

        public BouquetType getBouquetType() {
            return this.bouquetType;
        }

        public String getChannelName() {
            return this.channelName;
        }

        public int getChannelNumber() {
            return this.channelNumber;
        }

        public int getChannelSubNumber() {
            return this.channelSubNumber;
        }

        public ChannelType getChannelType() {
            return this.channelType;
        }

        public String getChannelUuid() {
            return this.channelUuid;
        }

        public int getCurrentServiceIndex() {
            return this.currentServiceIndex;
        }

        public List<Service> getServices() {
            return this.services;
        }

        public boolean getVideoIsVisible() {
            return this.videoIsVisible;
        }

        public boolean isTimeShifting() {
            return this.isTimeShifting;
        }
    }

    public TvContext(PlayerContext.Player player, Channel channel) {
        this.player = player;
        this.channel = channel;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public PlayerContext.Player getPlayer() {
        return this.player;
    }
}
