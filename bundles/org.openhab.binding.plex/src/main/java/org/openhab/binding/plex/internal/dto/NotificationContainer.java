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
package org.openhab.binding.plex.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
public class NotificationContainer {

    @SerializedName("NotificationContainer")
    @Expose
    private InnerNotificationContainer notificationContainer;

    public InnerNotificationContainer getNotificationContainer() {
        return notificationContainer;
    }

    public void setNotificationContainer(InnerNotificationContainer notificationContainer) {
        this.notificationContainer = notificationContainer;
    }

    public class InnerNotificationContainer {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("size")
        @Expose
        private Integer size;
        @SerializedName("PlaySessionStateNotification")
        @Expose
        private List<PlaySessionStateNotification> playSessionStateNotification = null;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public List<PlaySessionStateNotification> getPlaySessionStateNotification() {
            return playSessionStateNotification;
        }

        public void setPlaySessionStateNotification(List<PlaySessionStateNotification> playSessionStateNotification) {
            this.playSessionStateNotification = playSessionStateNotification;
        }

        public class PlaySessionStateNotification {

            @SerializedName("sessionKey")
            @Expose
            private String sessionKey;
            @SerializedName("guid")
            @Expose
            private String guid;
            @SerializedName("ratingKey")
            @Expose
            private String ratingKey;
            @SerializedName("url")
            @Expose
            private String url;
            @SerializedName("key")
            @Expose
            private String key;
            @SerializedName("viewOffset")
            @Expose
            private Integer viewOffset;
            @SerializedName("playQueueItemID")
            @Expose
            private Integer playQueueItemID;
            @SerializedName("state")
            @Expose
            private String state;

            public String getSessionKey() {
                return sessionKey;
            }

            public void setSessionKey(String sessionKey) {
                this.sessionKey = sessionKey;
            }

            public String getGuid() {
                return guid;
            }

            public void setGuid(String guid) {
                this.guid = guid;
            }

            public String getRatingKey() {
                return ratingKey;
            }

            public void setRatingKey(String ratingKey) {
                this.ratingKey = ratingKey;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public Integer getViewOffset() {
                return viewOffset;
            }

            public void setViewOffset(Integer viewOffset) {
                this.viewOffset = viewOffset;
            }

            public Integer getPlayQueueItemID() {
                return playQueueItemID;
            }

            public void setPlayQueueItemID(Integer playQueueItemID) {
                this.playQueueItemID = playQueueItemID;
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }
        }
    }
}
