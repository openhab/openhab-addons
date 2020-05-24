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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTPLUG;

import org.apache.commons.lang.builder.EqualsBuilder;
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
public class VerisureSmartPlugsDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_SMARTPLUG;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureSmartPlugsDTO)) {
            return false;
        }
        VerisureSmartPlugsDTO rhs = ((VerisureSmartPlugsDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

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
            return currentState != null ? new ToStringBuilder(this).append("device", device)
                    .append("currentState", currentState).append("icon", icon).append("isHazardous", isHazardous)
                    .append("typename", typename).toString() : "";
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
