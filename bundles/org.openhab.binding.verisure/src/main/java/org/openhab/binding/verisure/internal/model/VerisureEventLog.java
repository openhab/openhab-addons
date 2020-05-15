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
package org.openhab.binding.verisure.internal.model;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_EVENT_LOG;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The event log of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureEventLog extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_EVENT_LOG;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureEventLog)) {
            return false;
        }
        VerisureEventLog rhs = ((VerisureEventLog) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    public class Data {

        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("installation", installation).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(installation).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Data)) {
                return false;
            }
            Data rhs = ((Data) other);
            return new EqualsBuilder().append(installation, rhs.installation).isEquals();
        }
    }

    public class Installation {

        private EventLog eventLog = new EventLog();
        @SerializedName("__typename")
        private @Nullable String typename;

        public EventLog getEventLog() {
            return eventLog;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("eventLog", eventLog).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(eventLog).append(typename).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof Installation) == false) {
                return false;
            }
            Installation rhs = ((Installation) other);
            return new EqualsBuilder().append(eventLog, rhs.eventLog).append(typename, rhs.typename).isEquals();
        }
    }

    public class EventLog {

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
            return new ToStringBuilder(this).append("moreDataAvailable", moreDataAvailable)
                    .append("pagedList", pagedList).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(pagedList).append(moreDataAvailable).append(typename).toHashCode();
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

    public class PagedList {

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
            return new ToStringBuilder(this).append("device", device).append("gatewayArea", gatewayArea)
                    .append("eventType", eventType).append("eventCategory", eventCategory)
                    .append("eventSource", eventSource).append("eventId", eventId).append("eventTime", eventTime)
                    .append("userName", userName).append("armState", armState).append("userType", userType)
                    .append("climateValue", climateValue).append("sensorType", sensorType)
                    .append("eventCount", eventCount).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(eventId).append(eventCategory).append(eventSource).append(eventCount)
                    .append(eventType).append(userName).append(climateValue).append(gatewayArea).append(armState)
                    .append(sensorType).append(eventTime).append(userType).append(device).append(typename).toHashCode();
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
