/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.dto;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_USERPRESENCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The user presences of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureUserPresencesDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_USERPRESENCE;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    public static class UserTracking {
        private boolean isCallingUser;
        private @Nullable String webAccount;
        private @Nullable String status;
        private @Nullable String xbnContactId;
        private @Nullable String currentLocationName;
        private String deviceId = "";
        private @Nullable String name;
        private @Nullable String currentLocationTimestamp;
        private @Nullable String deviceName;
        private @Nullable String currentLocationId;
        @SerializedName("__typename")
        private @Nullable String typename;

        public boolean getIsCallingUser() {
            return isCallingUser;
        }

        public @Nullable String getWebAccount() {
            return webAccount;
        }

        public @Nullable String getStatus() {
            return status;
        }

        public @Nullable String getXbnContactId() {
            return xbnContactId;
        }

        public @Nullable String getCurrentLocationName() {
            return currentLocationName;
        }

        public @Nullable String getDeviceId() {
            return deviceId;
        }

        public @Nullable String getName() {
            return name;
        }

        public @Nullable String getCurrentLocationTimestamp() {
            return currentLocationTimestamp;
        }

        public @Nullable String getDeviceName() {
            return deviceName;
        }

        public @Nullable String getCurrentLocationId() {
            return currentLocationId;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localCurrentLocationId = currentLocationId;
            result = prime * result + ((localCurrentLocationId == null) ? 0 : localCurrentLocationId.hashCode());
            String localCurrentLocationName = currentLocationName;
            result = prime * result + ((localCurrentLocationName == null) ? 0 : localCurrentLocationName.hashCode());
            String localCurrentLocationTimestamp = currentLocationTimestamp;
            result = prime * result
                    + ((localCurrentLocationTimestamp == null) ? 0 : localCurrentLocationTimestamp.hashCode());
            result = prime * result + deviceId.hashCode();
            String localDeviceName = deviceName;
            result = prime * result + ((localDeviceName == null) ? 0 : localDeviceName.hashCode());
            result = prime * result + (isCallingUser ? 1231 : 1237);
            String localName = name;
            result = prime * result + ((localName == null) ? 0 : localName.hashCode());
            String localStatus = status;
            result = prime * result + ((localStatus == null) ? 0 : localStatus.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            String localWebAccount = webAccount;
            result = prime * result + ((localWebAccount == null) ? 0 : localWebAccount.hashCode());
            String localXbnContactId = xbnContactId;
            result = prime * result + ((localXbnContactId == null) ? 0 : localXbnContactId.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            UserTracking other = (UserTracking) obj;
            String localCurrentLocationId = currentLocationId;
            if (localCurrentLocationId == null) {
                if (other.currentLocationId != null) {
                    return false;
                }
            } else if (!localCurrentLocationId.equals(other.currentLocationId)) {
                return false;
            }
            String localCurrentLocationName = currentLocationName;
            if (localCurrentLocationName == null) {
                if (other.currentLocationName != null) {
                    return false;
                }
            } else if (!localCurrentLocationName.equals(other.currentLocationName)) {
                return false;
            }
            String localCurrentLocationTimestamp = currentLocationTimestamp;
            if (localCurrentLocationTimestamp == null) {
                if (other.currentLocationTimestamp != null) {
                    return false;
                }
            } else if (!localCurrentLocationTimestamp.equals(other.currentLocationTimestamp)) {
                return false;
            }
            if (!deviceId.equals(other.deviceId)) {
                return false;
            }
            String localDeviceName = deviceName;
            if (localDeviceName == null) {
                if (other.deviceName != null) {
                    return false;
                }
            } else if (!localDeviceName.equals(other.deviceName)) {
                return false;
            }
            if (isCallingUser != other.isCallingUser) {
                return false;
            }
            String localName = name;
            if (localName == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!localName.equals(other.name)) {
                return false;
            }
            String localStatus = status;
            if (localStatus == null) {
                if (other.status != null) {
                    return false;
                }
            } else if (!localStatus.equals(other.status)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            String localWebAccount = webAccount;
            if (localWebAccount == null) {
                if (other.webAccount != null) {
                    return false;
                }
            } else if (!localWebAccount.equals(other.webAccount)) {
                return false;
            }
            String localXbnContactId = xbnContactId;
            if (localXbnContactId == null) {
                if (other.xbnContactId != null) {
                    return false;
                }
            } else if (!localXbnContactId.equals(other.xbnContactId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "UserTracking [isCallingUser=" + isCallingUser + ", webAccount=" + webAccount + ", status=" + status
                    + ", xbnContactId=" + xbnContactId + ", currentLocationName=" + currentLocationName + ", deviceId="
                    + deviceId + ", name=" + name + ", currentLocationTimestamp=" + currentLocationTimestamp
                    + ", deviceName=" + deviceName + ", currentLocationId=" + currentLocationId + ", typename="
                    + typename + "]";
        }
    }
}
