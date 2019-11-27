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

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The climate devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimatesJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureClimatesJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureClimatesJSON(@Nullable Data data) {
        super();
        this.data = data;
    }

    public @Nullable Data getData() {
        return data;
    }

    public void setData(@Nullable Data data) {
        this.data = data;
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
        if (!(other instanceof VerisureClimatesJSON)) {
            return false;
        }
        VerisureClimatesJSON rhs = ((VerisureClimatesJSON) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Climate {

        private @Nullable Device device;
        private @Nullable Boolean humidityEnabled;
        private @Nullable String humidityTimestamp;
        private @Nullable Double humidityValue;
        private @Nullable String temperatureTimestamp;
        private @Nullable Double temperatureValue;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Climate() {
        }

        /**
         *
         * @param temperatureValue
         * @param humidityTimestamp
         * @param temperatureTimestamp
         * @param typename
         * @param humidityValue
         * @param device
         * @param humidityEnabled
         */
        public Climate(@Nullable Device device, @Nullable Boolean humidityEnabled, @Nullable String humidityTimestamp,
                @Nullable Double humidityValue, @Nullable String temperatureTimestamp,
                @Nullable Double temperatureValue, @Nullable String typename) {
            super();
            this.device = device;
            this.humidityEnabled = humidityEnabled;
            this.humidityTimestamp = humidityTimestamp;
            this.humidityValue = humidityValue;
            this.temperatureTimestamp = temperatureTimestamp;
            this.temperatureValue = temperatureValue;
            this.typename = typename;
        }

        public @Nullable Device getDevice() {
            return device;
        }

        public void setDevice(@Nullable Device device) {
            this.device = device;
        }

        public @Nullable Boolean isHumidityEnabled() {
            return humidityEnabled;
        }

        public void setHumidityEnabled(@Nullable Boolean humidityEnabled) {
            this.humidityEnabled = humidityEnabled;
        }

        public @Nullable String getHumidityTimestamp() {
            return humidityTimestamp;
        }

        public void setHumidityTimestamp(@Nullable String humidityTimestamp) {
            this.humidityTimestamp = humidityTimestamp;
        }

        public @Nullable Double getHumidityValue() {
            return humidityValue;
        }

        public void setHumidityValue(@Nullable Double humidityValue) {
            this.humidityValue = humidityValue;
        }

        public @Nullable String getTemperatureTimestamp() {
            return temperatureTimestamp;
        }

        public void setTemperatureTimestamp(@Nullable String temperatureTimestamp) {
            this.temperatureTimestamp = temperatureTimestamp;
        }

        public @Nullable Double getTemperatureValue() {
            return temperatureValue;
        }

        public void setTemperatureValue(@Nullable Double temperatureValue) {
            this.temperatureValue = temperatureValue;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("device", device).append("humidityEnabled", humidityEnabled)
                    .append("humidityTimestamp", humidityTimestamp).append("humidityValue", humidityValue)
                    .append("temperatureTimestamp", temperatureTimestamp).append("temperatureValue", temperatureValue)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(temperatureValue).append(humidityTimestamp).append(temperatureTimestamp)
                    .append(typename).append(humidityValue).append(device).append(humidityEnabled).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Climate)) {
                return false;
            }
            Climate rhs = ((Climate) other);
            return new EqualsBuilder().append(temperatureValue, rhs.temperatureValue)
                    .append(humidityTimestamp, rhs.humidityTimestamp)
                    .append(temperatureTimestamp, rhs.temperatureTimestamp).append(typename, rhs.typename)
                    .append(humidityValue, rhs.humidityValue).append(device, rhs.device)
                    .append(humidityEnabled, rhs.humidityEnabled).isEquals();
        }

    }

    @NonNullByDefault
    public static class Data {

        private @Nullable Installation installation;

        /**
         * No args constructor for use in serialization
         *
         */
        public Data() {
        }

        /**
         *
         * @param installation
         */
        public Data(@Nullable Installation installation) {
            super();
            this.installation = installation;
        }

        public @Nullable Installation getInstallation() {
            return installation;
        }

        public void setInstallation(@Nullable Installation installation) {
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

        private @Nullable List<Climate> climates = null;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Installation() {
        }

        /**
         *
         * @param climates
         * @param typename
         */
        public Installation(@Nullable List<Climate> climates, @Nullable String typename) {
            super();
            this.climates = climates;
            this.typename = typename;
        }

        public @Nullable List<Climate> getClimates() {
            return climates;
        }

        public void setClimates(@Nullable List<Climate> climates) {
            this.climates = climates;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("climates", climates).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(climates).append(typename).toHashCode();
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
            return new EqualsBuilder().append(climates, rhs.climates).append(typename, rhs.typename).isEquals();
        }

    }

    @NonNullByDefault
    public static class Device {

        private @Nullable String deviceLabel;
        private @Nullable String area;
        private @Nullable Gui gui;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Device() {
        }

        /**
         *
         * @param gui
         * @param area
         * @param typename
         * @param deviceLabel
         */
        public Device(@Nullable String deviceLabel, @Nullable String area, @Nullable Gui gui,
                @Nullable String typename) {
            super();
            this.deviceLabel = deviceLabel;
            this.area = area;
            this.gui = gui;
            this.typename = typename;
        }

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public void setDeviceLabel(@Nullable String deviceLabel) {
            this.deviceLabel = deviceLabel;
        }

        public @Nullable String getArea() {
            return area;
        }

        public void setArea(@Nullable String area) {
            this.area = area;
        }

        public @Nullable Gui getGui() {
            return gui;
        }

        public void setGui(@Nullable Gui gui) {
            this.gui = gui;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("deviceLabel", deviceLabel).append("area", area).append("gui", gui)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(gui).append(area).append(typename).append(deviceLabel).toHashCode();
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
            return new EqualsBuilder().append(gui, rhs.gui).append(area, rhs.area).append(typename, rhs.typename)
                    .append(deviceLabel, rhs.deviceLabel).isEquals();
        }

    }

    @NonNullByDefault
    public static class Gui {

        private @Nullable String label;
        @SerializedName("__typename")
        @Expose
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Gui() {
        }

        /**
         *
         * @param typename
         * @param label
         */
        public Gui(@Nullable String label, @Nullable String typename) {
            super();
            this.label = label;
            this.typename = typename;
        }

        public @Nullable String getLabel() {
            return label;
        }

        public void setLabel(@Nullable String label) {
            this.label = label;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("label", label).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(label).toHashCode();
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
            return new EqualsBuilder().append(typename, rhs.typename).append(label, rhs.label).isEquals();
        }

    }
}
