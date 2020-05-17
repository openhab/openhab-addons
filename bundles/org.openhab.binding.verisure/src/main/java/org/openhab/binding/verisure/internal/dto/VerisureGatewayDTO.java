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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_GATEWAY;
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
        if (!(other instanceof VerisureGatewayDTO)) {
            return false;
        }
        VerisureGatewayDTO rhs = ((VerisureGatewayDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    public class Data {

        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
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

    public class Installation {

        private List<CommunicationState> communicationState = new ArrayList<>();
        private @Nullable String typename;

        public List<CommunicationState> getCommunicationState() {
            return communicationState;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("communicationState", communicationState)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(communicationState).append(typename).toHashCode();
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
            return new EqualsBuilder().append(communicationState, rhs.communicationState).append(typename, rhs.typename)
                    .isEquals();
        }
    }

    public class CommunicationState {

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
            return new ToStringBuilder(this).append("hardwareCarrierType", hardwareCarrierType).append("result", result)
                    .append("mediaType", mediaType).append("device", device).append("testDate", testDate)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(result).append(hardwareCarrierType).append(mediaType).append(device)
                    .append(testDate).append(typename).toHashCode();
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
