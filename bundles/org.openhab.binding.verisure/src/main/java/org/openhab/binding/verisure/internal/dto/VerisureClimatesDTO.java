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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import org.apache.commons.lang.builder.EqualsBuilder;
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
public class VerisureClimatesDTO extends VerisureBaseThingDTO {

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
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureClimatesDTO)) {
            return false;
        }
        VerisureClimatesDTO rhs = ((VerisureClimatesDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

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
            return temperatureTimestamp != null ? new ToStringBuilder(this).append("device", device)
                    .append("humidityEnabled", humidityEnabled).append("humidityTimestamp", humidityTimestamp)
                    .append("humidityValue", humidityValue).append("temperatureTimestamp", temperatureTimestamp)
                    .append("temperatureValue", temperatureValue).append("typename", typename).toString() : "";
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
}
