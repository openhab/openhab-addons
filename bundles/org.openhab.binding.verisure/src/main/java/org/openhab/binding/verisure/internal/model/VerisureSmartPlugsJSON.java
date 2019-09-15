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

import com.google.gson.annotations.SerializedName;

/**
 * The smart plugs of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugsJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureSmartPlugsJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureSmartPlugsJSON(@Nullable Data data) {
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
        if (!(other instanceof VerisureSmartPlugsJSON)) {
            return false;
        }
        VerisureSmartPlugsJSON rhs = ((VerisureSmartPlugsJSON) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
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

        private @Nullable List<Smartplug> smartplugs = null;
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
         * @param typename
         * @param smartplugs
         */
        public Installation(@Nullable List<Smartplug> smartplugs, @Nullable String typename) {
            super();
            this.smartplugs = smartplugs;
            this.typename = typename;
        }

        public @Nullable List<Smartplug> getSmartplugs() {
            return smartplugs;
        }

        public void setSmartplugs(@Nullable List<Smartplug> smartplugs) {
            this.smartplugs = smartplugs;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("smartplugs", smartplugs).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(smartplugs).toHashCode();
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
            return new EqualsBuilder().append(typename, rhs.typename).append(smartplugs, rhs.smartplugs).isEquals();
        }

    }

    @NonNullByDefault
    public static class Smartplug {

        private @Nullable Device device;
        private @Nullable String currentState;
        private @Nullable String icon;
        private @Nullable Boolean isHazardous;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Smartplug() {
        }

        /**
         *
         * @param icon
         * @param typename
         * @param currentState
         * @param device
         * @param isHazardous
         */
        public Smartplug(@Nullable Device device, @Nullable String currentState, @Nullable String icon,
                @Nullable Boolean isHazardous, @Nullable String typename) {
            super();
            this.device = device;
            this.currentState = currentState;
            this.icon = icon;
            this.isHazardous = isHazardous;
            this.typename = typename;
        }

        public @Nullable Device getDevice() {
            return device;
        }

        public void setDevice(@Nullable Device device) {
            this.device = device;
        }

        public @Nullable String getCurrentState() {
            return currentState;
        }

        public void setCurrentState(@Nullable String currentState) {
            this.currentState = currentState;
        }

        public @Nullable String getIcon() {
            return icon;
        }

        public void setIcon(@Nullable String icon) {
            this.icon = icon;
        }

        public @Nullable Boolean isHazardous() {
            return isHazardous;
        }

        public void setIsHazardous(@Nullable Boolean isHazardous) {
            this.isHazardous = isHazardous;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("device", device).append("currentState", currentState)
                    .append("icon", icon).append("isHazardous", isHazardous).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(icon).append(typename).append(currentState).append(device)
                    .append(isHazardous).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Smartplug)) {
                return false;
            }
            Smartplug rhs = ((Smartplug) other);
            return new EqualsBuilder().append(icon, rhs.icon).append(typename, rhs.typename)
                    .append(currentState, rhs.currentState).append(device, rhs.device)
                    .append(isHazardous, rhs.isHazardous).isEquals();
        }

    }

    @NonNullByDefault
    public static class Device {

        private @Nullable String deviceLabel;
        private @Nullable String area;
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Device() {
        }

        /**
         *
         * @param area
         * @param typename
         * @param deviceLabel
         */
        public Device(@Nullable String deviceLabel, @Nullable String area, @Nullable String typename) {
            super();
            this.deviceLabel = deviceLabel;
            this.area = area;
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

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("deviceLabel", deviceLabel).append("area", area)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(area).append(typename).append(deviceLabel).toHashCode();
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
            return new EqualsBuilder().append(area, rhs.area).append(typename, rhs.typename)
                    .append(deviceLabel, rhs.deviceLabel).isEquals();
        }

    }

}
