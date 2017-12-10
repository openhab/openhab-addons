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
 * THe presence detector in Verisure.
 *
 * @author Jarle Hjortland
 *
 */
public class VerisureUserTrackingJSON implements VerisureObjectJSON {
    @SerializedName("name")
    private String name;
    @SerializedName("webAccount")
    private String webAccount;
    @SerializedName("userTrackingLocationStatus")
    private String userTrackingLocationStatus;
    @SerializedName("currentLocationName")
    private String currentLocationName;

    public VerisureUserTrackingJSON(String id, String webAccount, String userLocationStatus, String userLocatonName) {
        super();
        this.name = id;
        this.webAccount = webAccount;
        this.userTrackingLocationStatus = userLocationStatus;
        this.currentLocationName = userLocatonName;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public void setId(String id) {
        this.name = id;
    }

    public String getWebAccount() {
        return webAccount;
    }

    public void setWebAccount(String webAccount) {
        this.webAccount = webAccount;
    }

    public String getUserLocationStatus() {
        return userTrackingLocationStatus;
    }

    public void setUserLocationStatus(String userLocationStatus) {
        this.userTrackingLocationStatus = userLocationStatus;
    }

    public String getUserLocatonName() {
        return currentLocationName;
    }

    public void setUserLocatonName(String userLocatonName) {
        this.currentLocationName = userLocatonName;
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
        result = prime * result + ((userTrackingLocationStatus == null) ? 0 : userTrackingLocationStatus.hashCode());
        result = prime * result + ((webAccount == null) ? 0 : webAccount.hashCode());
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
        if (!(obj instanceof VerisureUserTrackingJSON)) {
            return false;
        }
        VerisureUserTrackingJSON other = (VerisureUserTrackingJSON) obj;
        if (userTrackingLocationStatus == null) {
            if (other.userTrackingLocationStatus != null) {
                return false;
            }
        } else if (!userTrackingLocationStatus.equals(other.userTrackingLocationStatus)) {
            return false;
        }
        if (webAccount == null) {
            if (other.webAccount != null) {
                return false;
            }
        } else if (!webAccount.equals(other.webAccount)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDescription() {
        return webAccount;
    }

    @Override
    public String toString() {
        return "VerisureUserTrackingJSON [name=" + name + ", userTrackingLocationStatus=" + userTrackingLocationStatus
                + ", currentLocationName=" + currentLocationName + "]";
    }
}
