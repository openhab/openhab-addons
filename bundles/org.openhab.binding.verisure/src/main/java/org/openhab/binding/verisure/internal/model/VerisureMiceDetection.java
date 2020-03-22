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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_MICE_DETECTION;

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
 * The Mice detection status of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureMiceDetection extends VerisureBaseThing {

    public static final int UNDEFINED = -1;
    private Data data = new Data();
    private double temperatureValue = UNDEFINED;
    private @Nullable String temperatureTimestamp;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

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
    public int hashCode() {
        return new HashCodeBuilder().append(data).append(temperatureValue).append(temperatureTimestamp).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureMiceDetection)) {
            return false;
        }
        VerisureMiceDetection rhs = ((VerisureMiceDetection) other);
        return new EqualsBuilder().append(data, rhs.data).append(temperatureValue, rhs.temperatureValue)
                .append(temperatureTimestamp, rhs.temperatureTimestamp).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
        }

        public void setInstallation(Installation installation) {
            this.installation = installation;
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

    @NonNullByDefault
    public static class Installation {

        private List<Mouse> mice = new ArrayList<>();
        private @Nullable String typename;

        public List<Mouse> getMice() {
            return mice;
        }

        public void setMice(List<Mouse> mice) {
            this.mice = mice;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("mice", mice).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(mice).append(typename).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Installation)) {
                return false;
            }
            Installation rhs = ((Installation) other);
            return new EqualsBuilder().append(mice, rhs.mice).append(typename, rhs.typename).isEquals();
        }
    }

    @NonNullByDefault
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
        public int hashCode() {
            return new HashCodeBuilder().append(type).append(device).append(typename).append(detections).toHashCode();
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

    @NonNullByDefault
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
            return new ToStringBuilder(this).append("count", count).append("gatewayTime", gatewayTime)
                    .append("nodeTime", nodeTime).append("duration", duration).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(count).append(duration).append(gatewayTime).append(typename)
                    .append(nodeTime).toHashCode();
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

    @NonNullByDefault
    public static class Device {

        private @Nullable String deviceLabel;
        private @Nullable String area;
        private @Nullable Gui gui;
        @SerializedName("__typename")
        private @Nullable String typename;

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public @Nullable String getArea() {
            return area;
        }

        public @Nullable Gui getGui() {
            return gui;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("deviceLabel", deviceLabel).append("area", area).append("gui", gui)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(area).append(deviceLabel).append(gui).append(typename).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Device)) {
                return false;
            }
            Device rhs = ((Device) other);
            return new EqualsBuilder().append(area, rhs.area).append(deviceLabel, rhs.deviceLabel).append(gui, rhs.gui)
                    .append(typename, rhs.typename).isEquals();
        }
    }

    @NonNullByDefault
    public static class Gui {

        private @Nullable String support;
        private @Nullable String typename;

        public @Nullable String getSupport() {
            return support;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("support", support).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(support).append(typename).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Gui)) {
                return false;
            }
            Gui rhs = ((Gui) other);
            return new EqualsBuilder().append(support, rhs.support).append(typename, rhs.typename).isEquals();
        }
    }
}
