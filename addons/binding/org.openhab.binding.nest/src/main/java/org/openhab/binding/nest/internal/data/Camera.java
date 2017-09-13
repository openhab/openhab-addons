/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
    private boolean isStreaming;
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
    private Event lastEvent;

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

    public Event getLastEvent() {
        return lastEvent;
    }

    public static class ActivityZone {
        @SerializedName("name")
        private boolean name;
        @SerializedName("id")
        private boolean id;

        public boolean isName() {
            return name;
        }

        public boolean isId() {
            return id;
        }
    }

    /** Internal class to handle the camera event data. */
    public static class Event {
        @SerializedName("has_sound")
        private boolean hasSound;
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
        private String animatedImageUrl;
        @SerializedName("activity_zone_ids")
        private List<String> activityZones;

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

        public String getAnimatedImageUrl() {
            return animatedImageUrl;
        }

        public List<String> getActivityZones() {
            return activityZones;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((activityZones == null) ? 0 : activityZones.hashCode());
            result = prime * result + ((animatedImageUrl == null) ? 0 : animatedImageUrl.hashCode());
            result = prime * result + ((appUrl == null) ? 0 : appUrl.hashCode());
            result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
            result = prime * result + (hasMotion ? 1231 : 1237);
            result = prime * result + (hasPerson ? 1231 : 1237);
            result = prime * result + (hasSound ? 1231 : 1237);
            result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
            result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
            result = prime * result + ((urlsExpireTime == null) ? 0 : urlsExpireTime.hashCode());
            result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Event other = (Event) obj;
            if (activityZones == null) {
                if (other.activityZones != null) {
                    return false;
                }
            } else if (!activityZones.equals(other.activityZones)) {
                return false;
            }
            if (animatedImageUrl == null) {
                if (other.animatedImageUrl != null) {
                    return false;
                }
            } else if (!animatedImageUrl.equals(other.animatedImageUrl)) {
                return false;
            }
            if (appUrl == null) {
                if (other.appUrl != null) {
                    return false;
                }
            } else if (!appUrl.equals(other.appUrl)) {
                return false;
            }
            if (endTime == null) {
                if (other.endTime != null) {
                    return false;
                }
            } else if (!endTime.equals(other.endTime)) {
                return false;
            }
            if (hasMotion != other.hasMotion) {
                return false;
            }
            if (hasPerson != other.hasPerson) {
                return false;
            }
            if (hasSound != other.hasSound) {
                return false;
            }
            if (imageUrl == null) {
                if (other.imageUrl != null) {
                    return false;
                }
            } else if (!imageUrl.equals(other.imageUrl)) {
                return false;
            }
            if (startTime == null) {
                if (other.startTime != null) {
                    return false;
                }
            } else if (!startTime.equals(other.startTime)) {
                return false;
            }
            if (urlsExpireTime == null) {
                if (other.urlsExpireTime != null) {
                    return false;
                }
            } else if (!urlsExpireTime.equals(other.urlsExpireTime)) {
                return false;
            }
            if (webUrl == null) {
                if (other.webUrl != null) {
                    return false;
                }
            } else if (!webUrl.equals(other.webUrl)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return "Camera [isStreaming=" + isStreaming + ", isAudioInputEnabled=" + isAudioInputEnabled
                + ", LastIsOnlineChange=" + LastIsOnlineChange + ", isVideoHistoryEnabled=" + isVideoHistoryEnabled
                + ", webUrl=" + webUrl + ", appUrl=" + appUrl + ", isPublicShareEnabled=" + isPublicShareEnabled
                + ", activityZones=" + activityZones + ", publicShareUrl=" + publicShareUrl + ", snapshotUrl="
                + snapshotUrl + ", lastEvent=" + lastEvent + ", getName()=" + getName() + ", getDeviceId()="
                + getDeviceId() + ", getLastConnection()=" + getLastConnection() + ", getNameLong()=" + getNameLong()
                + ", getSoftwareVersion()=" + getSoftwareVersion() + ", isOnline()=" + isOnline()
                + ", getStructureId()=" + getStructureId() + ", getWhereId()=" + getWhereId() + "]";
    }
}
