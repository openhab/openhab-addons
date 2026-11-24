/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.List;
import java.util.Objects;

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

    @Override
    public ThingTypeUID getThingTypeUID() {
        Gui gui = getData().getInstallation().getClimates().get(0).getDevice().getGui();
        String type = gui != null ? gui.getLabel() : null;
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
        return true;
    }

    public static class Climate {

        private Device device = new Device();
        private boolean humidityEnabled;
        private @Nullable String humidityTimestamp;
        private double humidityValue;
        private @Nullable String temperatureTimestamp;
        private double temperatureValue;
        private @Nullable List<Threshold> thresholds;
        @SerializedName("__typename")
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public List<Threshold> getThresholds() {
            List<Threshold> localThresholds = thresholds;
            return localThresholds != null ? localThresholds : List.of();
        }

        public void setThresholds(List<Threshold> thresholds) {
            this.thresholds = thresholds;
        }

        public @Nullable Threshold getThreshold(String sensorType) {
            return getThresholds().stream().filter(threshold -> sensorType.equals(threshold.getSensorType()))
                    .findFirst().orElse(null);
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
            result = prime * result + Objects.hashCode(thresholds);
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
            return Objects.equals(thresholds, other.thresholds);
        }

        @Override
        public String toString() {
            return "Climate [device=" + device + ", humidityEnabled=" + humidityEnabled + ", humidityTimestamp="
                    + humidityTimestamp + ", humidityValue=" + humidityValue + ", temperatureTimestamp="
                    + temperatureTimestamp + ", temperatureValue=" + temperatureValue + ", thresholds=" + thresholds
                    + ", typename=" + typename + "]";
        }
    }

    /**
     * Limit for the alert that Verisure sends when a temperature or humidity value is too low or too high.
     */
    public static class Threshold {

        public static final String SENSOR_TYPE_TEMPERATURE = "TEMPERATURE";
        public static final String SENSOR_TYPE_HUMIDITY = "HUMIDITY";

        private @Nullable String sensorType;
        private @Nullable Double min;
        private @Nullable Double max;
        private boolean minActive;
        private boolean maxActive;

        public Threshold() {
        }

        public Threshold(Threshold other) {
            sensorType = other.sensorType;
            min = other.min;
            max = other.max;
            minActive = other.minActive;
            maxActive = other.maxActive;
        }

        public @Nullable String getSensorType() {
            return sensorType;
        }

        public @Nullable Double getMin() {
            return min;
        }

        public void setMin(@Nullable Double min) {
            this.min = min;
        }

        public @Nullable Double getMax() {
            return max;
        }

        public void setMax(@Nullable Double max) {
            this.max = max;
        }

        public boolean isMinActive() {
            return minActive;
        }

        public void setMinActive(boolean minActive) {
            this.minActive = minActive;
        }

        public boolean isMaxActive() {
            return maxActive;
        }

        public void setMaxActive(boolean maxActive) {
            this.maxActive = maxActive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sensorType, min, max, minActive, maxActive);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof Threshold other && Objects.equals(sensorType, other.sensorType)
                    && Objects.equals(min, other.min) && Objects.equals(max, other.max) && minActive == other.minActive
                    && maxActive == other.maxActive;
        }

        @Override
        public String toString() {
            return "Threshold [sensorType=" + sensorType + ", min=" + min + ", max=" + max + ", minActive=" + minActive
                    + ", maxActive=" + maxActive + "]";
        }
    }
}
