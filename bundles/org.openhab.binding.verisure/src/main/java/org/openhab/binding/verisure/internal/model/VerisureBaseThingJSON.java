/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.model;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A base JSON thing for other Verisure things to inherit.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBaseThingJSON implements VerisureThingJSON {

    protected String deviceId = "";
    protected @Nullable String name;
    protected @Nullable String location;
    protected @Nullable String status;
    protected @Nullable String siteName;
    protected @Nullable BigDecimal siteId;

    public VerisureBaseThingJSON() {
        super();
    }

    /**
     *
     * @return
     *         The status
     */
    public @Nullable String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     *            The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the deviceId
     */
    @Override
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    @Override
    public void setDeviceId(@Nullable String deviceId) {
        // Make sure device id is normalized, i.e. replace all non character/digits with empty string
        this.deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
    }

    /**
     * @return the location
     */
    @Override
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    @Override
    public @Nullable String getSiteName() {
        return siteName;
    }

    @Override
    public void setSiteName(@Nullable String siteName) {
        this.siteName = siteName;
    }

    @Override
    public @Nullable BigDecimal getSiteId() {
        return siteId;
    }

    @Override
    public void setSiteId(@Nullable BigDecimal siteId) {
        this.siteId = siteId;
    }

    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((siteName == null) ? 0 : siteName.hashCode());
        result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VerisureBaseThingJSON)) {
            return false;
        }

        VerisureBaseThingJSON other = (VerisureBaseThingJSON) obj;
        if (deviceId == null) {
            if (other.deviceId != null) {
                return false;
            }
        } else if (deviceId != null && !deviceId.equals(other.deviceId)) {
            return false;
        }

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (name != null && !name.equals(other.name)) {
            return false;
        }

        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (location != null && !location.equals(other.location)) {
            return false;
        }

        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (status != null && !status.equals(other.status)) {
            return false;
        }

        if (siteName == null) {
            if (other.siteName != null) {
                return false;
            }
        } else if (siteName != null && !siteName.equals(other.siteName)) {
            return false;
        }

        if (siteId != other.siteId) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureBaseThingJSON [");
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (deviceId != null) {
            builder.append(", deviceId=");
            builder.append(deviceId);
        }
        if (location != null) {
            builder.append(", location=");
            builder.append(location);
        }
        if (status != null) {
            builder.append(", status=");
            builder.append(status);
        }
        if (siteName != null) {
            builder.append(", siteName=");
            builder.append(siteName);
        }
        builder.append(", siteId=");
        builder.append(siteId);
        builder.append("]");
        return builder.toString();
    }
}
