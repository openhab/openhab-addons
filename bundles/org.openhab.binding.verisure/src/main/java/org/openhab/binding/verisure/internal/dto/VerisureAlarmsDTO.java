/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureAlarmsDTO)) {
            return false;
        }
        VerisureAlarmsDTO rhs = ((VerisureAlarmsDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
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
        public String toString() {
            return type != null ? new ToStringBuilder(this).append("type", type).append("statusType", statusType)
                    .append("date", date).append("name", name).append("changedVia", changedVia)
                    .append("allowedForFirstLine", allowedForFirstLine).append("allowed", allowed)
                    .append("errorCodes", errorCodes).append("typename", typename).toString() : "";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof ArmState)) {
                return false;
            }
            ArmState rhs = ((ArmState) other);
            return new EqualsBuilder().append(allowed, rhs.allowed).append(statusType, rhs.statusType)
                    .append(typename, rhs.typename).append(name, rhs.name)
                    .append(allowedForFirstLine, rhs.allowedForFirstLine).append(errorCodes, rhs.errorCodes)
                    .append(date, rhs.date).append(type, rhs.type).append(changedVia, rhs.changedVia).isEquals();
        }
    }
}
