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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An sensor in the verisure system, normally smoke and water detectors.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimateBaseJSON extends VerisureBaseThingJSON {

    protected @Nullable String temperatureBelowMinAlertValue;
    protected @Nullable String temperatureAboveMaxAlertValue;
    protected @Nullable String temperature;
    protected @Nullable Boolean plottable;
    protected @Nullable Boolean monitorable;
    protected @Nullable String humidity;
    protected @Nullable String humidityBelowMinAlertValue;
    protected @Nullable String humidityAboveMaxAlertValue;
    protected @Nullable String type;
    protected @Nullable String timestamp;

    /**
     *
     * @return
     *         The temperatureBelowMinAlertValue
     */
    public @Nullable String getTemperatureBelowMinAlertValue() {
        return temperatureBelowMinAlertValue;
    }

    /**
     *
     * @param temperatureBelowMinAlertValue
     *                                          The temperatureBelowMinAlertValue
     */
    public void setTemperatureBelowMinAlertValue(String temperatureBelowMinAlertValue) {
        this.temperatureBelowMinAlertValue = temperatureBelowMinAlertValue;
    }

    /**
     *
     * @return
     *         The temperatureAboveMaxAlertValue
     */
    public @Nullable String getTemperatureAboveMaxAlertValue() {
        return temperatureAboveMaxAlertValue;
    }

    /**
     *
     * @param temperatureAboveMaxAlertValue
     *                                          The temperatureAboveMaxAlertValue
     */
    public void setTemperatureAboveMaxAlertValue(String temperatureAboveMaxAlertValue) {
        this.temperatureAboveMaxAlertValue = temperatureAboveMaxAlertValue;
    }

    /**
     *
     * @return
     *         The temperature
     */
    public @Nullable String getTemperature() {
        return temperature;
    }

    /**
     *
     * @param temperature
     *                        The temperature
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     *
     * @return
     *         The plottable
     */
    public @Nullable Boolean getPlottable() {
        return plottable;
    }

    /**
     *
     * @param plottable
     *                      The plottable
     */
    public void setPlottable(Boolean plottable) {
        this.plottable = plottable;
    }

    /**
     *
     * @return
     *         The monitorable
     */
    public @Nullable Boolean getMonitorable() {
        return monitorable;
    }

    /**
     *
     * @param monitorable
     *                        The monitorable
     */
    public void setMonitorable(Boolean monitorable) {
        this.monitorable = monitorable;
    }

    /**
     *
     * @return
     *         The humidity
     */
    public @Nullable String getHumidity() {
        return humidity;
    }

    /**
     *
     * @param humidity
     *                     The humidity
     */
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    /**
     *
     * @return
     *         The humidityBelowMinAlertValue
     */
    public @Nullable String getHumidityBelowMinAlertValue() {
        return humidityBelowMinAlertValue;
    }

    /**
     *
     * @param humidityBelowMinAlertValue
     *                                       The humidityBelowMinAlertValue
     */
    public void setHumidityBelowMinAlertValue(String humidityBelowMinAlertValue) {
        this.humidityBelowMinAlertValue = humidityBelowMinAlertValue;
    }

    /**
     *
     * @return
     *         The humidityAboveMaxAlertValue
     */
    public @Nullable String getHumidityAboveMaxAlertValue() {
        return humidityAboveMaxAlertValue;
    }

    /**
     *
     * @param humidityAboveMaxAlertValue
     *                                       The humidityAboveMaxAlertValue
     */
    public void setHumidityAboveMaxAlertValue(String humidityAboveMaxAlertValue) {
        this.humidityAboveMaxAlertValue = humidityAboveMaxAlertValue;
    }

    /**
     *
     * @return
     *         The timestamp
     */
    public @Nullable String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     *                      The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @return
     *         The type of climate sensor
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     *
     * @param type
     *                 The type of cliemate sensor
     */
    public void setType(String type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((humidity == null) ? 0 : humidity.hashCode());
        result = prime * result + ((humidityAboveMaxAlertValue == null) ? 0 : humidityAboveMaxAlertValue.hashCode());
        result = prime * result + ((humidityBelowMinAlertValue == null) ? 0 : humidityBelowMinAlertValue.hashCode());
        result = prime * result + ((monitorable == null) ? 0 : monitorable.hashCode());
        result = prime * result + ((plottable == null) ? 0 : plottable.hashCode());
        result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
        result = prime * result
                + ((temperatureAboveMaxAlertValue == null) ? 0 : temperatureAboveMaxAlertValue.hashCode());
        result = prime * result
                + ((temperatureBelowMinAlertValue == null) ? 0 : temperatureBelowMinAlertValue.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VerisureClimateBaseJSON)) {
            return false;
        }

        VerisureClimateBaseJSON other = (VerisureClimateBaseJSON) obj;
        if (humidity == null) {
            if (other.humidity != null) {
                return false;
            }
        } else if (humidity != null && !humidity.equals(other.humidity)) {
            return false;
        }
        if (humidityAboveMaxAlertValue == null) {
            if (other.humidityAboveMaxAlertValue != null) {
                return false;
            }
        } else if (humidityAboveMaxAlertValue != null
                && !humidityAboveMaxAlertValue.equals(other.humidityAboveMaxAlertValue)) {
            return false;
        }
        if (humidityBelowMinAlertValue == null) {
            if (other.humidityBelowMinAlertValue != null) {
                return false;
            }
        } else if (humidityBelowMinAlertValue != null
                && !humidityBelowMinAlertValue.equals(other.humidityBelowMinAlertValue)) {
            return false;
        }
        if (monitorable == null) {
            if (other.monitorable != null) {
                return false;
            }
        } else if (monitorable != null && !monitorable.equals(other.monitorable)) {
            return false;
        }
        if (plottable == null) {
            if (other.plottable != null) {
                return false;
            }
        } else if (plottable != null && !plottable.equals(other.plottable)) {
            return false;
        }
        if (temperature == null) {
            if (other.temperature != null) {
                return false;
            }
        } else if (temperature != null && !temperature.equals(other.temperature)) {
            return false;
        }
        if (temperatureAboveMaxAlertValue == null) {
            if (other.temperatureAboveMaxAlertValue != null) {
                return false;
            }
        } else if (temperatureAboveMaxAlertValue != null
                && !temperatureAboveMaxAlertValue.equals(other.temperatureAboveMaxAlertValue)) {
            return false;
        }
        if (temperatureBelowMinAlertValue == null) {
            if (other.temperatureBelowMinAlertValue != null) {
                return false;
            }
        } else if (temperatureBelowMinAlertValue != null
                && !temperatureBelowMinAlertValue.equals(other.temperatureBelowMinAlertValue)) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (timestamp != null && !timestamp.equals(other.timestamp)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (type != null && !type.equals(other.type)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureClimateBaseJSON [");
        if (temperature != null) {
            builder.append("temperature=");
            builder.append(temperature);
            builder.append(", ");
        }
        if (humidity != null) {
            builder.append("humidity=");
            builder.append(humidity);
            builder.append(", ");
        }
        if (timestamp != null) {
            builder.append("timestamp=");
            builder.append(timestamp);
            builder.append(", ");
        }
        if (type != null) {
            builder.append("type=");
            builder.append(type);
            builder.append(", ");
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }
}
