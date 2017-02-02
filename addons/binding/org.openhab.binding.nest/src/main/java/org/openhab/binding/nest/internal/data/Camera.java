package org.openhab.binding.nest.internal.data;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The data for the camera.
 *
 * @author David Bennett
 */
public class Camera extends BaseNestDevice {
    private boolean isStreaming;

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setStreaming(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    public boolean isAudioInputEnabled() {
        return isAudioInputEnabled;
    }

    public Date getLastIsOnlineChange() {
        return LastIsOnlineChange;
    }

    public boolean isVideoHistoryEnabled() {
        return isVideoHistoryEnabled;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public boolean isPublicShareEnabled() {
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

    @SerializedName("is_audio_input_enabled")
    private boolean isAudioInputEnabled;
    @SerializedName("last_is_online_change")
    private Date LastIsOnlineChange;
    @SerializedName("is_video_history_enabled")
    private boolean isVideoHistoryEnabled;
    @SerializedName("web_url")
    private String webUrl;
    @SerializedName("app_url")
    private String appUrl;
    @SerializedName("is_public_share_enabled")
    private boolean isPublicShareEnabled;
    @SerializedName("activity_zones")
    private List<ActivityZone> activityZones;
    @SerializedName("public_share_url")
    private String publicShareUrl;
    @SerializedName("snapshot_url")
    private String snapshotUrl;
    @SerializedName("last_event")
    private List<Event> lastEvent;

    public static class ActivityZone {
        public boolean isName() {
            return name;
        }

        public boolean isId() {
            return id;
        }

        @SerializedName("name")
        private boolean name;
        @SerializedName("id")
        private boolean id;
    }

    /** Internal class to handle the camera event data. */
    public static class Event {
        private boolean hasSound;

        public boolean isHasSound() {
            return hasSound;
        }

        public boolean isHasMotion() {
            return hasMotion;
        }

        public boolean isHasPerson() {
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

        public String getAnimated_image_url() {
            return animated_image_url;
        }

        public List<String> getActivityZones() {
            return activityZones;
        }

        @SerializedName("has_motion")
        private boolean hasMotion;
        @SerializedName("has_person")
        private boolean hasPerson;
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
        private String animated_image_url;
        @SerializedName("activity_zones")
        private List<String> activityZones;
    }
}
