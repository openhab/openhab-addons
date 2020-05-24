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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_MICE_DETECTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The Mice detection status of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureMiceDetectionDTO extends VerisureBaseThingDTO {

    public static final int UNDEFINED = -1;
    private double temperatureValue = UNDEFINED;
    private @Nullable String temperatureTimestamp;

    public double getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(double temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public @Nullable String getTemperatureTime() {
        return temperatureTimestamp;
    }

    public void setTemperatureTime(@Nullable String temperatureTimestamp) {
        this.temperatureTimestamp = temperatureTimestamp;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_MICE_DETECTION;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("data", data).append("temperatureValue", temperatureValue)
                .append("temperatureTimestamp", temperatureTimestamp).toString();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureMiceDetectionDTO)) {
            return false;
        }
        VerisureMiceDetectionDTO rhs = ((VerisureMiceDetectionDTO) other);
        return new EqualsBuilder().append(data, rhs.data).append(temperatureValue, rhs.temperatureValue)
                .append(temperatureTimestamp, rhs.temperatureTimestamp).isEquals();
    }

    public static class Mouse {

        private Device device = new Device();
        private @Nullable Object type;
        private List<Detection> detections = new ArrayList<>();
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public @Nullable Object getType() {
            return type;
        }

        public List<Detection> getDetections() {
            return detections;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("device", device).append("type", type)
                    .append("detections", detections).append("typename", typename).toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Mouse)) {
                return false;
            }
            Mouse rhs = ((Mouse) other);
            return new EqualsBuilder().append(type, rhs.type).append(device, rhs.device).append(typename, rhs.typename)
                    .append(detections, rhs.detections).isEquals();
        }
    }

    public static class Detection {

        private int count;
        private @Nullable String gatewayTime;
        private @Nullable String nodeTime;
        private int duration;
        private @Nullable String typename;

        public int getCount() {
            return count;
        }

        public @Nullable String getGatewayTime() {
            return gatewayTime;
        }

        public @Nullable String getNodeTime() {
            return nodeTime;
        }

        public int getDuration() {
            return duration;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return gatewayTime != null ? new ToStringBuilder(this).append("count", count)
                    .append("gatewayTime", gatewayTime).append("nodeTime", nodeTime).append("duration", duration)
                    .append("typename", typename).toString() : "";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Detection)) {
                return false;
            }
            Detection rhs = ((Detection) other);
            return new EqualsBuilder().append(count, rhs.count).append(duration, rhs.duration)
                    .append(gatewayTime, rhs.gatewayTime).append(typename, rhs.typename).append(nodeTime, rhs.nodeTime)
                    .isEquals();
        }
    }
}
