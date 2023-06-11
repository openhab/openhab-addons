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
package org.openhab.binding.nest.internal.wwn.dto;

import java.util.Date;
import java.util.List;

/**
 * The data for the WWN camera.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class WWNCamera extends BaseWWNDevice {

    private Boolean isStreaming;
    private Boolean isAudioInputEnabled;
    private Date lastIsOnlineChange;
    private Boolean isVideoHistoryEnabled;
    private String webUrl;
    private String appUrl;
    private Boolean isPublicShareEnabled;
    private List<WWNActivityZone> activityZones;
    private String publicShareUrl;
    private String snapshotUrl;
    private WWNCameraEvent lastEvent;

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

    public List<WWNActivityZone> getActivityZones() {
        return activityZones;
    }

    public String getPublicShareUrl() {
        return publicShareUrl;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public WWNCameraEvent getLastEvent() {
        return lastEvent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WWNCamera other = (WWNCamera) obj;
        if (activityZones == null) {
            if (other.activityZones != null) {
                return false;
            }
        } else if (!activityZones.equals(other.activityZones)) {
            return false;
        }
        if (appUrl == null) {
            if (other.appUrl != null) {
                return false;
            }
        } else if (!appUrl.equals(other.appUrl)) {
            return false;
        }
        if (isAudioInputEnabled == null) {
            if (other.isAudioInputEnabled != null) {
                return false;
            }
        } else if (!isAudioInputEnabled.equals(other.isAudioInputEnabled)) {
            return false;
        }
        if (isPublicShareEnabled == null) {
            if (other.isPublicShareEnabled != null) {
                return false;
            }
        } else if (!isPublicShareEnabled.equals(other.isPublicShareEnabled)) {
            return false;
        }
        if (isStreaming == null) {
            if (other.isStreaming != null) {
                return false;
            }
        } else if (!isStreaming.equals(other.isStreaming)) {
            return false;
        }
        if (isVideoHistoryEnabled == null) {
            if (other.isVideoHistoryEnabled != null) {
                return false;
            }
        } else if (!isVideoHistoryEnabled.equals(other.isVideoHistoryEnabled)) {
            return false;
        }
        if (lastEvent == null) {
            if (other.lastEvent != null) {
                return false;
            }
        } else if (!lastEvent.equals(other.lastEvent)) {
            return false;
        }
        if (lastIsOnlineChange == null) {
            if (other.lastIsOnlineChange != null) {
                return false;
            }
        } else if (!lastIsOnlineChange.equals(other.lastIsOnlineChange)) {
            return false;
        }
        if (publicShareUrl == null) {
            if (other.publicShareUrl != null) {
                return false;
            }
        } else if (!publicShareUrl.equals(other.publicShareUrl)) {
            return false;
        }
        if (snapshotUrl == null) {
            if (other.snapshotUrl != null) {
                return false;
            }
        } else if (!snapshotUrl.equals(other.snapshotUrl)) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((activityZones == null) ? 0 : activityZones.hashCode());
        result = prime * result + ((appUrl == null) ? 0 : appUrl.hashCode());
        result = prime * result + ((isAudioInputEnabled == null) ? 0 : isAudioInputEnabled.hashCode());
        result = prime * result + ((isPublicShareEnabled == null) ? 0 : isPublicShareEnabled.hashCode());
        result = prime * result + ((isStreaming == null) ? 0 : isStreaming.hashCode());
        result = prime * result + ((isVideoHistoryEnabled == null) ? 0 : isVideoHistoryEnabled.hashCode());
        result = prime * result + ((lastEvent == null) ? 0 : lastEvent.hashCode());
        result = prime * result + ((lastIsOnlineChange == null) ? 0 : lastIsOnlineChange.hashCode());
        result = prime * result + ((publicShareUrl == null) ? 0 : publicShareUrl.hashCode());
        result = prime * result + ((snapshotUrl == null) ? 0 : snapshotUrl.hashCode());
        result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
        return result;
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
