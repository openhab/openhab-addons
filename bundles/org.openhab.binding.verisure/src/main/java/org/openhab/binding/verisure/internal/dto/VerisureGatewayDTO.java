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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_GATEWAY;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureGatewayDTO)) {
            return false;
        }
        VerisureGatewayDTO rhs = ((VerisureGatewayDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
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
        public String toString() {
            return result != null ? new ToStringBuilder(this).append("hardwareCarrierType", hardwareCarrierType)
                    .append("result", result).append("mediaType", mediaType).append("device", device)
                    .append("testDate", testDate).append("typename", typename).toString() : "";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof CommunicationState)) {
                return false;
            }
            CommunicationState rhs = ((CommunicationState) other);
            return new EqualsBuilder().append(result, rhs.result).append(hardwareCarrierType, rhs.hardwareCarrierType)
                    .append(mediaType, rhs.mediaType).append(device, rhs.device).append(testDate, rhs.testDate)
                    .append(typename, rhs.typename).isEquals();
        }
    }
}
