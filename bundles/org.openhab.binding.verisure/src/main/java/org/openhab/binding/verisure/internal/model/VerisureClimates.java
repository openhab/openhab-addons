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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

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
 * The climate devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimates extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        String type = getData().getInstallation().getClimates().get(0).getDevice().getGui().getLabel();
        if ("SMOKE".equals(type)) {
            return THING_TYPE_SMOKEDETECTOR;
        } else if ("WATER".equals(type)) {
            return THING_TYPE_WATERDETECTOR;
        } else if ("HOMEPAD".equals(type)) {
            return THING_TYPE_NIGHT_CONTROL;
        } else if ("SIREN".equals(type)) {
            return THING_TYPE_SIREN;
        } else {
            return THING_TYPE_SMOKEDETECTOR;
        }
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
        if (!(other instanceof VerisureClimates)) {
            return false;
        }
        VerisureClimates rhs = ((VerisureClimates) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Climate {

        private Device device = new Device();
        private boolean humidityEnabled;
        private @Nullable String humidityTimestamp;
        private double humidityValue;
        private @Nullable String temperatureTimestamp;
        private double temperatureValue;
        @SerializedName("__typename")
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public boolean isHumidityEnabled() {
            return humidityEnabled;
        }

        public @Nullable String getHumidityTimestamp() {
            return humidityTimestamp;
        }

        public double getHumidityValue() {
            return humidityValue;
        }

        public @Nullable String getTemperatureTimestamp() {
            return temperatureTimestamp;
        }

        public double getTemperatureValue() {
            return temperatureValue;
        }

        public @Nullable String getTypename() {
            return typename;
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

        private List<Climate> climates = new ArrayList<>();
        @SerializedName("__typename")
        private @Nullable String typename;

        public List<Climate> getClimates() {
            return climates;
        }

        public void setClimates(List<Climate> climates) {
            this.climates = climates;
        }

        public @Nullable String getTypename() {
            return typename;
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
        private Gui gui = new Gui();
        @SerializedName("__typename")
        private @Nullable String typename;

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public @Nullable String getArea() {
            return area;
        }

        public Gui getGui() {
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
        private @Nullable String typename;

        public @Nullable String getLabel() {
            return label;
        }

        public @Nullable String getTypename() {
            return typename;
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
