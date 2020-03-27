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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTPLUG;

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
 * The smart plugs of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugs extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_SMARTPLUG;
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
        if (!(other instanceof VerisureSmartPlugs)) {
            return false;
        }
        VerisureSmartPlugs rhs = ((VerisureSmartPlugs) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
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

        private List<Smartplug> smartplugs = new ArrayList<>();

        @SerializedName("__typename")
        private @Nullable String typename;

        public List<Smartplug> getSmartplugs() {
            return smartplugs;
        }

        public void setSmartplugs(List<Smartplug> smartplugs) {
            this.smartplugs = smartplugs;
        }

        public @Nullable String getTypename() {
            return typename;
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

        private Device device = new Device();
        private @Nullable String currentState;
        private @Nullable String icon;
        private boolean isHazardous;
        @SerializedName("__typename")
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public @Nullable String getCurrentState() {
            return currentState;
        }

        public @Nullable String getIcon() {
            return icon;
        }

        public boolean isHazardous() {
            return isHazardous;
        }

        public @Nullable String getTypename() {
            return typename;
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
}
