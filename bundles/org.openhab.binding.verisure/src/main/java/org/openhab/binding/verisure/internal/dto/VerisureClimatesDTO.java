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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The climate devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimatesDTO extends VerisureBaseThingDTO {

    private @Nullable VerisureBatteryStatusDTO batteryStatus;

    public @Nullable VerisureBatteryStatusDTO getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(@Nullable VerisureBatteryStatusDTO batteryStatus) {
        this.batteryStatus = batteryStatus;
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
        VerisureClimatesDTO other = (VerisureClimatesDTO) obj;
        VerisureBatteryStatusDTO localBatteryStatusJSON = batteryStatus;
        if (localBatteryStatusJSON == null) {
            if (other.batteryStatus != null) {
                return false;
            }
        } else if (!localBatteryStatusJSON.equals(other.batteryStatus)) {
            return false;
        }
        return true;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + device.hashCode();
            result = prime * result + (humidityEnabled ? 1231 : 1237);
            String localHumidityTimestamp = humidityTimestamp;
            result = prime * result + ((localHumidityTimestamp == null) ? 0 : localHumidityTimestamp.hashCode());
            long temp;
            temp = Double.doubleToLongBits(humidityValue);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            String localTemperatureTimestamp = temperatureTimestamp;
            result = prime * result + ((localTemperatureTimestamp == null) ? 0 : localTemperatureTimestamp.hashCode());
            temp = Double.doubleToLongBits(temperatureValue);
            result = prime * result + (int) (temp ^ (temp >>> 32));
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
            Climate other = (Climate) obj;
            if (!device.equals(other.device)) {
                return false;
            }
            if (humidityEnabled != other.humidityEnabled) {
                return false;
            }
            String localHumidityTimestamp = humidityTimestamp;
            if (localHumidityTimestamp == null) {
                if (other.humidityTimestamp != null) {
                    return false;
                }
            } else if (!localHumidityTimestamp.equals(other.humidityTimestamp)) {
                return false;
            }
            if (Double.doubleToLongBits(humidityValue) != Double.doubleToLongBits(other.humidityValue)) {
                return false;
            }
            String localTemperatureTimestamp = temperatureTimestamp;
            if (localTemperatureTimestamp == null) {
                if (other.temperatureTimestamp != null) {
                    return false;
                }
            } else if (!localTemperatureTimestamp.equals(other.temperatureTimestamp)) {
                return false;
            }
            if (Double.doubleToLongBits(temperatureValue) != Double.doubleToLongBits(other.temperatureValue)) {
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
            return "Climate [device=" + device + ", humidityEnabled=" + humidityEnabled + ", humidityTimestamp="
                    + humidityTimestamp + ", humidityValue=" + humidityValue + ", temperatureTimestamp="
                    + temperatureTimestamp + ", temperatureValue=" + temperatureValue + ", typename=" + typename + "]";
        }
    }
}
