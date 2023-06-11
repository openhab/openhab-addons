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
 * The data for a WWN camera event.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Extract CameraEvent object from Camera
 * @author Wouter Born - Add equals, hashCode, toString methods
 */
public class WWNCameraEvent {

    private Boolean hasSound;
    private Boolean hasMotion;
    private Boolean hasPerson;
    private Date startTime;
    private Date endTime;
    private Date urlsExpireTime;
    private String webUrl;
    private String appUrl;
    private String imageUrl;
    private String animatedImageUrl;
    private List<String> activityZoneIds;

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
        return activityZoneIds;
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
        WWNCameraEvent other = (WWNCameraEvent) obj;
        if (activityZoneIds == null) {
            if (other.activityZoneIds != null) {
                return false;
            }
        } else if (!activityZoneIds.equals(other.activityZoneIds)) {
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
        if (hasMotion == null) {
            if (other.hasMotion != null) {
                return false;
            }
        } else if (!hasMotion.equals(other.hasMotion)) {
            return false;
        }
        if (hasPerson == null) {
            if (other.hasPerson != null) {
                return false;
            }
        } else if (!hasPerson.equals(other.hasPerson)) {
            return false;
        }
        if (hasSound == null) {
            if (other.hasSound != null) {
                return false;
            }
        } else if (!hasSound.equals(other.hasSound)) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activityZoneIds == null) ? 0 : activityZoneIds.hashCode());
        result = prime * result + ((animatedImageUrl == null) ? 0 : animatedImageUrl.hashCode());
        result = prime * result + ((appUrl == null) ? 0 : appUrl.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((hasMotion == null) ? 0 : hasMotion.hashCode());
        result = prime * result + ((hasPerson == null) ? 0 : hasPerson.hashCode());
        result = prime * result + ((hasSound == null) ? 0 : hasSound.hashCode());
        result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((urlsExpireTime == null) ? 0 : urlsExpireTime.hashCode());
        result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Event [hasSound=").append(hasSound).append(", hasMotion=").append(hasMotion)
                .append(", hasPerson=").append(hasPerson).append(", startTime=").append(startTime).append(", endTime=")
                .append(endTime).append(", urlsExpireTime=").append(urlsExpireTime).append(", webUrl=").append(webUrl)
                .append(", appUrl=").append(appUrl).append(", imageUrl=").append(imageUrl).append(", animatedImageUrl=")
                .append(animatedImageUrl).append(", activityZoneIds=").append(activityZoneIds).append("]");
        return builder.toString();
    }
}
