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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The status of a door or window.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowJSON extends VerisureBaseThingJSON {

    private @Nullable String area;
    private @Nullable String state;
    private @Nullable String deviceLabel;

    public VerisureDoorWindowJSON(String deviceId, String state, String location) {
        super();
        this.area = location;
        this.state = state;
        this.deviceLabel = deviceId;
    }

    public @Nullable String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public @Nullable String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public @Nullable String getDeviceLabel() {
        return deviceLabel;
    }

    public void setDeviceLabel(String deviceLabel) {
        this.area = deviceLabel;
    }

    @Override
    public @Nullable String getLocation() {
        return area;
    }

    @Override
    public void setDeviceId(@Nullable String deviceId) {
        this.deviceLabel = deviceId;
    }

    @Override
    public @Nullable String getDeviceId() {
        return deviceLabel;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((area == null) ? 0 : area.hashCode());
        result = prime * result + ((deviceLabel == null) ? 0 : deviceLabel.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VerisureDoorWindowJSON)) {
            return false;
        }

        VerisureDoorWindowJSON other = (VerisureDoorWindowJSON) obj;
        if (area == null) {
            if (other.area != null) {
                return false;
            }
        } else if (area != null && !area.equals(other.area)) {
            return false;
        }
        if (deviceLabel == null) {
            if (other.deviceLabel != null) {
                return false;
            }
        } else if (deviceLabel != null && !deviceLabel.equals(other.deviceLabel)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (state != null && !state.equals(other.state)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureDoorWindowJSON [");
        if (area != null) {
            builder.append("area=");
            builder.append(area);
            builder.append(", ");
        }
        if (state != null) {
            builder.append("state=");
            builder.append(state);
            builder.append(", ");
        }
        if (deviceLabel != null) {
            builder.append("deviceLabel=");
            builder.append(deviceLabel);
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }

}
