/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The data for the camera.
 *
 * @author David Bennett - Initial Contribution
 */
public class Camera extends BaseNestDevice {
    @SerializedName("is_streaming")
    private Boolean isStreaming;
    @SerializedName("is_audio_input_enabled")
    private Boolean isAudioInputEnabled;
    @SerializedName("last_is_online_change")
    private Date lastIsOnlineChange;
    @SerializedName("is_video_history_enabled")
    private Boolean isVideoHistoryEnabled;
    @SerializedName("web_url")
    private String webUrl;
    @SerializedName("app_url")
    private String appUrl;
    @SerializedName("is_public_share_enabled")
    private Boolean isPublicShareEnabled;
    @SerializedName("activity_zones")
    private List<ActivityZone> activityZones;
    @SerializedName("public_share_url")
    private String publicShareUrl;
    @SerializedName("snapshot_url")
    private String snapshotUrl;
    @SerializedName("last_event")
    private Event lastEvent;

    public Boolean isStreaming() {
        return isStreaming;
    }

    public Boolean isAudioInputEnabled() {
        return isAudioInputEnabled;
    }

    public Date getLastIsOnlineChange() {
        return lastIsOnlineChange;
    }

    public Boolean isVideoHistoryEnabled() {
        return isVideoHistoryEnabled;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public Boolean isPublicShareEnabled() {
        return isPublicShareEnabled;
    }

    public List<ActivityZone> getActivityZones() {
        return activityZones;
    }

    public String getPublicShareUrl() {
        return publicShareUrl;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public Event getLastEvent() {
        return lastEvent;
    }

    public static class ActivityZone {
        @SerializedName("name")
        private String name;
        @SerializedName("id")
        private int id;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    /** Internal class to handle the camera event data. */
    public static class Event {
        @SerializedName("has_sound")
        private Boolean hasSound;
        @SerializedName("has_motion")
        private Boolean hasMotion;
        @SerializedName("has_person")
        private Boolean hasPerson;
        @SerializedName("start_time")
        private Date startTime;
        @SerializedName("end_time")
        private Date endTime;
        @SerializedName("urls_expire_time")
        private Date urlsExpireTime;
        @SerializedName("web_url")
        private String webUrl;
        @SerializedName("app_url")
        private String appUrl;
        @SerializedName("image_url")
        private String imageUrl;
        @SerializedName("animated_image_url")
        private String animatedImageUrl;
        @SerializedName("activity_zone_ids")
        private List<String> activityZones;

        public Boolean isHasSound() {
            return hasSound;
        }

        public Boolean isHasMotion() {
            return hasMotion;
        }

        public Boolean isHasPerson() {
            return hasPerson;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public Date getUrlsExpireTime() {
            return urlsExpireTime;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public String getAppUrl() {
            return appUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getAnimatedImageUrl() {
            return animatedImageUrl;
        }

        public List<String> getActivityZones() {
            return activityZones;
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Camera [isStreaming=").append(isStreaming).append(", isAudioInputEnabled=")
                .append(isAudioInputEnabled).append(", lastIsOnlineChange=").append(lastIsOnlineChange)
                .append(", isVideoHistoryEnabled=").append(isVideoHistoryEnabled).append(", webUrl=").append(webUrl)
                .append(", appUrl=").append(appUrl).append(", isPublicShareEnabled=").append(isPublicShareEnabled)
                .append(", activityZones=").append(activityZones).append(", publicShareUrl=").append(publicShareUrl)
                .append(", snapshotUrl=").append(snapshotUrl).append(", lastEvent=").append(lastEvent)
                .append(", getId()=").append(getId()).append(", getName()=").append(getName())
                .append(", getDeviceId()=").append(getDeviceId()).append(", getLastConnection()=")
                .append(getLastConnection()).append(", isOnline()=").append(isOnline()).append(", getNameLong()=")
                .append(getNameLong()).append(", getSoftwareVersion()=").append(getSoftwareVersion())
                .append(", getStructureId()=").append(getStructureId()).append(", getWhereId()=").append(getWhereId())
                .append("]");
        return builder.toString();
    }

}
