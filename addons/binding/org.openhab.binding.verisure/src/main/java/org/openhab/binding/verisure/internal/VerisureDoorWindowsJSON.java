/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import com.google.gson.annotations.SerializedName;

/**
 * The status of a door or window.
 *
 * @author Jarle Hjortland
 *
 */
public class VerisureDoorWindowsJSON implements VerisureObjectJSON {
    @SerializedName("area")
    private String area;
    @SerializedName("state")
    private String state;
    @SerializedName("deviceLabel")
    private String deviceLabel;

    public VerisureDoorWindowsJSON(String id, String state, String area) {
        super();
        this.area = area;
        this.state = state;
        this.deviceLabel = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VerisureDoorWindowsJSON)) {
            return false;
        }
        VerisureDoorWindowsJSON other = (VerisureDoorWindowsJSON) obj;
        if (area == null) {
            if (other.area != null) {
                return false;
            }
        } else if (!area.equals(other.area)) {
            return false;
        }
        if (deviceLabel == null) {
            if (other.deviceLabel != null) {
                return false;
            }
        } else if (!deviceLabel.equals(other.deviceLabel)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        return true;
    }

    @Override
    public String getId() {
        return deviceLabel;
    }

    @Override
    public void setId(String id) {
        this.deviceLabel = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String getDescription() {
        return area;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureDoorWindowsJSON [");
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
        return builder.toString();
    }

}
