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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_EVENT_LOG;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The event log of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureEventLogDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_EVENT_LOG;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureEventLogDTO)) {
            return false;
        }
        VerisureEventLogDTO rhs = ((VerisureEventLogDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    public static class EventLog {

        private boolean moreDataAvailable;
        private List<PagedList> pagedList = new ArrayList<>();
        private @Nullable String typename;

        public boolean isMoreDataAvailable() {
            return moreDataAvailable;
        }

        public List<PagedList> getPagedList() {
            return pagedList;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return pagedList.size() > 0 ? new ToStringBuilder(this).append("moreDataAvailable", moreDataAvailable)
                    .append("pagedList", pagedList).append("typename", typename).toString() : "";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof EventLog)) {
                return false;
            }
            EventLog rhs = ((EventLog) other);
            return new EqualsBuilder().append(pagedList, rhs.pagedList).append(moreDataAvailable, rhs.moreDataAvailable)
                    .append(typename, rhs.typename).isEquals();
        }
    }

    public static class PagedList {

        private Device device = new Device();
        private @Nullable String gatewayArea;
        private @Nullable String eventType;
        private @Nullable String eventCategory;
        private @Nullable String eventSource;
        private @Nullable String eventId;
        private @Nullable String eventTime;
        private @Nullable String userName;
        private @Nullable String armState;
        private @Nullable String userType;
        private @Nullable String climateValue;
        private @Nullable String sensorType;
        private @Nullable String eventCount;
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public @Nullable String getGatewayArea() {
            return gatewayArea;
        }

        public @Nullable String getEventType() {
            return eventType;
        }

        public @Nullable String getEventCategory() {
            return eventCategory;
        }

        public @Nullable String getEventSource() {
            return eventSource;
        }

        public @Nullable String getEventId() {
            return eventId;
        }

        public @Nullable String getEventTime() {
            return eventTime;
        }

        public @Nullable String getUserName() {
            return userName;
        }

        public @Nullable String getArmState() {
            return armState;
        }

        public @Nullable String getUserType() {
            return userType;
        }

        public @Nullable String getClimateValue() {
            return climateValue;
        }

        public @Nullable String getSensorType() {
            return sensorType;
        }

        public @Nullable String getEventCount() {
            return eventCount;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return eventType != null ? new ToStringBuilder(this).append("device", device)
                    .append("gatewayArea", gatewayArea).append("eventType", eventType)
                    .append("eventCategory", eventCategory).append("eventSource", eventSource)
                    .append("eventId", eventId).append("eventTime", eventTime).append("userName", userName)
                    .append("armState", armState).append("userType", userType).append("climateValue", climateValue)
                    .append("sensorType", sensorType).append("eventCount", eventCount).append("typename", typename)
                    .toString() : "";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PagedList)) {
                return false;
            }
            PagedList rhs = ((PagedList) other);
            return new EqualsBuilder().append(eventId, rhs.eventId).append(eventCategory, rhs.eventCategory)
                    .append(eventSource, rhs.eventSource).append(eventCount, rhs.eventCount)
                    .append(eventType, rhs.eventType).append(userName, rhs.userName)
                    .append(climateValue, rhs.climateValue).append(gatewayArea, rhs.gatewayArea)
                    .append(armState, rhs.armState).append(sensorType, rhs.sensorType).append(eventTime, rhs.eventTime)
                    .append(userType, rhs.userType).append(device, rhs.device).append(typename, rhs.typename)
                    .isEquals();
        }
    }
}
