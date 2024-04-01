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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTPLUG;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

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
    public int hashCode() {
        return super.hashCode();
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localCurrentState = currentState;
            result = prime * result + ((localCurrentState == null) ? 0 : localCurrentState.hashCode());
            result = prime * result + device.hashCode();
            String localIcon = icon;
            result = prime * result + ((localIcon == null) ? 0 : localIcon.hashCode());
            result = prime * result + (isHazardous ? 1231 : 1237);
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
            Smartplug other = (Smartplug) obj;
            String localCurrentState = currentState;
            if (localCurrentState == null) {
                if (other.currentState != null) {
                    return false;
                }
            } else if (!localCurrentState.equals(other.currentState)) {
                return false;
            }
            if (!device.equals(other.device)) {
                return false;
            }
            String localIcon = icon;
            if (localIcon == null) {
                if (other.icon != null) {
                    return false;
                }
            } else if (!localIcon.equals(other.icon)) {
                return false;
            }
            if (isHazardous != other.isHazardous) {
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
            return "Smartplug [device=" + device + ", currentState=" + currentState + ", icon=" + icon
                    + ", isHazardous=" + isHazardous + ", typename=" + typename + "]";
        }
    }
}
