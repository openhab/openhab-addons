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
 * THe presence detector in Verisure.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureUserPresenceJSON extends VerisureBaseThingJSON {

    private @Nullable String webAccount;
    private @Nullable String userTrackingLocationStatus;
    private @Nullable String currentLocationName;

    public VerisureUserPresenceJSON(String id, String webAccount, String userLocationStatus, String userLocationName) {
        super();
        this.name = id;
        this.webAccount = webAccount;
        this.userTrackingLocationStatus = userLocationStatus;
        this.currentLocationName = userLocationName;
    }

    @Override
    public @Nullable String getId() {
        return "userpresence_" + id;
    }

    public @Nullable String getWebAccount() {
        return webAccount;
    }

    public void setWebAccount(String webAccount) {
        this.webAccount = webAccount;
    }

    public @Nullable String getUserLocationStatus() {
        return userTrackingLocationStatus;
    }

    public void setUserLocationStatus(String userLocationStatus) {
        this.userTrackingLocationStatus = userLocationStatus;
    }

    @Override
    public @Nullable String getLocation() {
        return currentLocationName;
    }

    @Override
    public void setLocation(String userLocatonName) {
        this.currentLocationName = userLocatonName;
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
        result = prime * result + ((userTrackingLocationStatus == null) ? 0 : userTrackingLocationStatus.hashCode());
        result = prime * result + ((webAccount == null) ? 0 : webAccount.hashCode());
        result = prime * result + ((currentLocationName == null) ? 0 : currentLocationName.hashCode());
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
        if (!(obj instanceof VerisureUserPresenceJSON)) {
            return false;
        }

        VerisureUserPresenceJSON other = (VerisureUserPresenceJSON) obj;
        if (userTrackingLocationStatus == null) {
            if (other.userTrackingLocationStatus != null) {
                return false;
            }
        } else if (userTrackingLocationStatus != null
                && !userTrackingLocationStatus.equals(other.userTrackingLocationStatus)) {
            return false;
        }
        if (webAccount == null) {
            if (other.webAccount != null) {
                return false;
            }
        } else if (webAccount != null && !webAccount.equals(other.webAccount)) {
            return false;
        }
        if (currentLocationName == null) {
            if (other.currentLocationName != null) {
                return false;
            }
        } else if (currentLocationName != null && !currentLocationName.equals(other.currentLocationName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureUserPresenceJSON [");
        if (userTrackingLocationStatus != null) {
            builder.append("userTrackingLocationStatus=");
            builder.append(userTrackingLocationStatus);
            builder.append(", ");
        }
        if (webAccount != null) {
            builder.append("webAccount=");
            builder.append(webAccount);
            builder.append(", ");
        }
        if (currentLocationName != null) {
            builder.append("currentLocationName=");
            builder.append(currentLocationName);
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }

}
