/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_GATEWAY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The gateway in the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureGatewayDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_GATEWAY;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

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

    public static class CommunicationState {

        private @Nullable String hardwareCarrierType;
        private @Nullable String result;
        private @Nullable String mediaType;
        private Device device = new Device();
        private @Nullable String testDate;
        private @Nullable String typename;

        public @Nullable String getHardwareCarrierType() {
            return hardwareCarrierType;
        }

        public @Nullable String getResult() {
            return result;
        }

        public @Nullable String getMediaType() {
            return mediaType;
        }

        public Device getDevice() {
            return device;
        }

        public @Nullable String getTestDate() {
            return testDate;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + device.hashCode();
            String locaHardwareCarrierType = hardwareCarrierType;
            result = prime * result + ((locaHardwareCarrierType == null) ? 0 : locaHardwareCarrierType.hashCode());
            String localMediaType = mediaType;
            result = prime * result + ((localMediaType == null) ? 0 : localMediaType.hashCode());
            String localResult = this.result;
            result = prime * result + ((localResult == null) ? 0 : localResult.hashCode());
            String localTestDate = testDate;
            result = prime * result + ((localTestDate == null) ? 0 : localTestDate.hashCode());
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
            CommunicationState other = (CommunicationState) obj;
            if (!device.equals(other.device)) {
                return false;
            }
            String locaHardwareCarrierType = hardwareCarrierType;
            if (locaHardwareCarrierType == null) {
                if (other.hardwareCarrierType != null) {
                    return false;
                }
            } else if (!locaHardwareCarrierType.equals(other.hardwareCarrierType)) {
                return false;
            }
            String localMediaType = mediaType;
            if (localMediaType == null) {
                if (other.mediaType != null) {
                    return false;
                }
            } else if (!localMediaType.equals(other.mediaType)) {
                return false;
            }
            String localResult = result;
            if (localResult == null) {
                if (other.result != null) {
                    return false;
                }
            } else if (!localResult.equals(other.result)) {
                return false;
            }
            String localTestDate = testDate;
            if (localTestDate == null) {
                if (other.testDate != null) {
                    return false;
                }
            } else if (!localTestDate.equals(other.testDate)) {
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
            return "CommunicationState [hardwareCarrierType=" + hardwareCarrierType + ", result=" + result
                    + ", mediaType=" + mediaType + ", device=" + device + ", testDate=" + testDate + ", typename="
                    + typename + "]";
        }
    }
}
