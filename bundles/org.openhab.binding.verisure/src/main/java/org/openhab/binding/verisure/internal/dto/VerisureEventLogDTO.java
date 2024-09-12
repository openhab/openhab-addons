/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (moreDataAvailable ? 1231 : 1237);
            result = prime * result + pagedList.hashCode();
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
            EventLog other = (EventLog) obj;
            if (moreDataAvailable != other.moreDataAvailable) {
                return false;
            }
            if (!pagedList.equals(other.pagedList)) {
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
            return "EventLog [moreDataAvailable=" + moreDataAvailable + ", pagedList=" + pagedList + ", typename="
                    + typename + "]";
        }
    }

    public static class PagedList {

        private @Nullable Device device = new Device();
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

        public @Nullable Device getDevice() {
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localArmState = armState;
            result = prime * result + ((localArmState == null) ? 0 : localArmState.hashCode());
            String localClimateValue = climateValue;
            result = prime * result + ((localClimateValue == null) ? 0 : localClimateValue.hashCode());
            Device localDevice = device;
            result = prime * result + ((localDevice == null) ? 0 : localDevice.hashCode());
            String localEventCategory = eventCategory;
            result = prime * result + ((localEventCategory == null) ? 0 : localEventCategory.hashCode());
            String localEventCount = eventCount;
            result = prime * result + ((localEventCount == null) ? 0 : localEventCount.hashCode());
            String localEventId = eventId;
            result = prime * result + ((localEventId == null) ? 0 : localEventId.hashCode());
            String localEventSource = eventSource;
            result = prime * result + ((localEventSource == null) ? 0 : localEventSource.hashCode());
            String localEventTime = eventTime;
            result = prime * result + ((localEventTime == null) ? 0 : localEventTime.hashCode());
            String localEventType = eventType;
            result = prime * result + ((localEventType == null) ? 0 : localEventType.hashCode());
            String localGatewayArea = gatewayArea;
            result = prime * result + ((localGatewayArea == null) ? 0 : localGatewayArea.hashCode());
            String localSensorType = sensorType;
            result = prime * result + ((localSensorType == null) ? 0 : localSensorType.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            String localUserName = userName;
            result = prime * result + ((localUserName == null) ? 0 : localUserName.hashCode());
            String localUserType = userType;
            result = prime * result + ((localUserType == null) ? 0 : localUserType.hashCode());
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
            PagedList other = (PagedList) obj;
            String localArmState = armState;
            if (localArmState == null) {
                if (other.armState != null) {
                    return false;
                }
            } else if (!localArmState.equals(other.armState)) {
                return false;
            }
            String localClimateValue = climateValue;
            if (localClimateValue == null) {
                if (other.climateValue != null) {
                    return false;
                }
            } else if (!localClimateValue.equals(other.climateValue)) {
                return false;
            }
            Device localDevice = device;
            if (localDevice == null) {
                if (other.device != null) {
                    return false;
                }
            } else if (!localDevice.equals(other.device)) {
                return false;
            }
            String localEventCategory = eventCategory;
            if (localEventCategory == null) {
                if (other.eventCategory != null) {
                    return false;
                }
            } else if (!localEventCategory.equals(other.eventCategory)) {
                return false;
            }
            String localEventCount = eventCount;
            if (localEventCount == null) {
                if (other.eventCount != null) {
                    return false;
                }
            } else if (!localEventCount.equals(other.eventCount)) {
                return false;
            }
            String localEventId = eventId;
            if (localEventId == null) {
                if (other.eventId != null) {
                    return false;
                }
            } else if (!localEventId.equals(other.eventId)) {
                return false;
            }
            String localEventSource = eventSource;
            if (localEventSource == null) {
                if (other.eventSource != null) {
                    return false;
                }
            } else if (!localEventSource.equals(other.eventSource)) {
                return false;
            }
            String localEventTime = eventTime;
            if (localEventTime == null) {
                if (other.eventTime != null) {
                    return false;
                }
            } else if (!localEventTime.equals(other.eventTime)) {
                return false;
            }
            String localEventType = eventType;
            if (localEventType == null) {
                if (other.eventType != null) {
                    return false;
                }
            } else if (!localEventType.equals(other.eventType)) {
                return false;
            }
            String localGatewayArea = gatewayArea;
            if (localGatewayArea == null) {
                if (other.gatewayArea != null) {
                    return false;
                }
            } else if (!localGatewayArea.equals(other.gatewayArea)) {
                return false;
            }
            String localSensorType = sensorType;
            if (localSensorType == null) {
                if (other.sensorType != null) {
                    return false;
                }
            } else if (!localSensorType.equals(other.sensorType)) {
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
            String localUserName = userName;
            if (localUserName == null) {
                if (other.userName != null) {
                    return false;
                }
            } else if (!localUserName.equals(other.userName)) {
                return false;
            }
            String localUserType = userType;
            if (localUserType == null) {
                if (other.userType != null) {
                    return false;
                }
            } else if (!localUserType.equals(other.userType)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "PagedList [device=" + device + ", gatewayArea=" + gatewayArea + ", eventType=" + eventType
                    + ", eventCategory=" + eventCategory + ", eventSource=" + eventSource + ", eventId=" + eventId
                    + ", eventTime=" + eventTime + ", userName=" + userName + ", armState=" + armState + ", userType="
                    + userType + ", climateValue=" + climateValue + ", sensorType=" + sensorType + ", eventCount="
                    + eventCount + ", typename=" + typename + "]";
        }
    }
}
