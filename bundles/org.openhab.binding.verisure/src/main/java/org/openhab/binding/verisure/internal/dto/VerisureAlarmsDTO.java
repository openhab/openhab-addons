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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_ALARM;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The alarms of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmsDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_ALARM;
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

    public static class ArmState {

        private @Nullable String type;
        private @Nullable String statusType;
        private @Nullable String date;
        private @Nullable String name;
        private @Nullable String changedVia;
        private boolean allowedForFirstLine;
        private boolean allowed;
        private List<Object> errorCodes = new ArrayList<>();
        private @Nullable String typename;

        public @Nullable String getType() {
            return type;
        }

        public @Nullable String getStatusType() {
            return statusType;
        }

        public void setStatusType(@Nullable String statusType) {
            this.statusType = statusType;
        }

        public @Nullable String getDate() {
            return date;
        }

        public @Nullable String getName() {
            return name;
        }

        public @Nullable String getChangedVia() {
            return changedVia;
        }

        public boolean getAllowedForFirstLine() {
            return allowedForFirstLine;
        }

        public boolean getAllowed() {
            return allowed;
        }

        public List<Object> getErrorCodes() {
            return errorCodes;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (allowed ? 1231 : 1237);
            result = prime * result + (allowedForFirstLine ? 1231 : 1237);
            String localChangedVia = changedVia;
            result = prime * result + ((localChangedVia == null) ? 0 : localChangedVia.hashCode());
            String localDate = date;
            result = prime * result + ((localDate == null) ? 0 : localDate.hashCode());
            result = prime * result + errorCodes.hashCode();
            String localName = name;
            result = prime * result + ((localName == null) ? 0 : localName.hashCode());
            String localStatusType = statusType;
            result = prime * result + ((localStatusType == null) ? 0 : localStatusType.hashCode());
            String localType = type;
            result = prime * result + ((localType == null) ? 0 : localType.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
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
            ArmState other = (ArmState) obj;
            if (allowed != other.allowed) {
                return false;
            }
            if (allowedForFirstLine != other.allowedForFirstLine) {
                return false;
            }
            String localChangedVia = changedVia;
            if (localChangedVia == null) {
                if (other.changedVia != null) {
                    return false;
                }
            } else if (!localChangedVia.equals(other.changedVia)) {
                return false;
            }
            String localdate = date;
            if (localdate == null) {
                if (other.date != null) {
                    return false;
                }
            } else if (!localdate.equals(other.date)) {
                return false;
            }
            if (!errorCodes.equals(other.errorCodes)) {
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
            String localStatusType = statusType;
            if (localStatusType == null) {
                if (other.statusType != null) {
                    return false;
                }
            } else if (!localStatusType.equals(other.statusType)) {
                return false;
            }
            String localType = type;
            if (localType == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!localType.equals(other.type)) {
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
            return true;
        }

        @Override
        public String toString() {
            return "ArmState [type=" + type + ", statusType=" + statusType + ", date=" + date + ", name=" + name
                    + ", changedVia=" + changedVia + ", allowedForFirstLine=" + allowedForFirstLine + ", allowed="
                    + allowed + ", errorCodes=" + errorCodes + ", typename=" + typename + "]";
        }
    }
}
