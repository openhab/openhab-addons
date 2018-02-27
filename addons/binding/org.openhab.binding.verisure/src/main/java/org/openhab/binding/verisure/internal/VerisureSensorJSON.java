/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.verisure.internal;

import com.google.gson.annotations.SerializedName;

/**
 * An sensor in the verisure system, normally smoke detectors.
 *
 * @author Jarle Hjortland
 *
 */
public class VerisureSensorJSON extends VerisureBaseObjectJSON {

    @SerializedName("temperatureBelowMinAlertValue")

    private String temperatureBelowMinAlertValue;
    @SerializedName("temperatureAboveMaxAlertValue")

    private String temperatureAboveMaxAlertValue;
    @SerializedName("temperature")

    private String temperature;
    @SerializedName("plottable")

    private Boolean plottable;
    @SerializedName("monitorable")

    private Boolean monitorable;
    @SerializedName("humidity")

    private String humidity;
    @SerializedName("humidityBelowMinAlertValue")

    private String humidityBelowMinAlertValue;
    @SerializedName("humidityAboveMaxAlertValue")

    private String humidityAboveMaxAlertValue;
    @SerializedName("timestamp")

    private String timestamp;

    /**
     *
     * @return
     *         The date
     */
    @Override
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date
     *            The date
     */
    @Override
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     *         The notAllowedReason
     */
    @Override
    public String getNotAllowedReason() {
        return notAllowedReason;
    }

    /**
     *
     * @param notAllowedReason
     *            The notAllowedReason
     */
    @Override
    public void setNotAllowedReason(String notAllowedReason) {
        this.notAllowedReason = notAllowedReason;
    }

    /**
     *
     * @return
     *         The name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     *            The name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     *         The changeAllowed
     */
    @Override
    public Boolean getChangeAllowed() {
        return changeAllowed;
    }

    /**
     *
     * @param changeAllowed
     *            The changeAllowed
     */
    @Override
    public void setChangeAllowed(Boolean changeAllowed) {
        this.changeAllowed = changeAllowed;
    }

    /**
     *
     * @return
     *         The label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     *            The label
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     *         The temperatureBelowMinAlertValue
     */
    public String getTemperatureBelowMinAlertValue() {
        return temperatureBelowMinAlertValue;
    }

    /**
     *
     * @param temperatureBelowMinAlertValue
     *            The temperatureBelowMinAlertValue
     */
    public void setTemperatureBelowMinAlertValue(String temperatureBelowMinAlertValue) {
        this.temperatureBelowMinAlertValue = temperatureBelowMinAlertValue;
    }

    /**
     *
     * @return
     *         The temperatureAboveMaxAlertValue
     */
    public String getTemperatureAboveMaxAlertValue() {
        return temperatureAboveMaxAlertValue;
    }

    /**
     *
     * @param temperatureAboveMaxAlertValue
     *            The temperatureAboveMaxAlertValue
     */
    public void setTemperatureAboveMaxAlertValue(String temperatureAboveMaxAlertValue) {
        this.temperatureAboveMaxAlertValue = temperatureAboveMaxAlertValue;
    }

    /**
     *
     * @return
     *         The temperature
     */
    public String getTemperature() {
        return temperature;
    }

    /**
     *
     * @param temperature
     *            The temperature
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     *
     * @return
     *         The plottable
     */
    public Boolean getPlottable() {
        return plottable;
    }

    /**
     *
     * @param plottable
     *            The plottable
     */
    public void setPlottable(Boolean plottable) {
        this.plottable = plottable;
    }

    /**
     *
     * @return
     *         The monitorable
     */
    public Boolean getMonitorable() {
        return monitorable;
    }

    /**
     *
     * @param monitorable
     *            The monitorable
     */
    public void setMonitorable(Boolean monitorable) {
        this.monitorable = monitorable;
    }

    /**
     *
     * @return
     *         The humidity
     */
    public String getHumidity() {
        return humidity;
    }

    /**
     *
     * @param humidity
     *            The humidity
     */
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    /**
     *
     * @return
     *         The location
     */
    @Override
    public String getLocation() {
        return location;
    }

    /**
     *
     * @param location
     *            The location
     */
    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     *
     * @return
     *         The humidityBelowMinAlertValue
     */
    public String getHumidityBelowMinAlertValue() {
        return humidityBelowMinAlertValue;
    }

    /**
     *
     * @param humidityBelowMinAlertValue
     *            The humidityBelowMinAlertValue
     */
    public void setHumidityBelowMinAlertValue(String humidityBelowMinAlertValue) {
        this.humidityBelowMinAlertValue = humidityBelowMinAlertValue;
    }

    /**
     *
     * @return
     *         The id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     *            The id
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     *         The type
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     *            The type
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     *         The humidityAboveMaxAlertValue
     */
    public String getHumidityAboveMaxAlertValue() {
        return humidityAboveMaxAlertValue;
    }

    /**
     *
     * @param humidityAboveMaxAlertValue
     *            The humidityAboveMaxAlertValue
     */
    public void setHumidityAboveMaxAlertValue(String humidityAboveMaxAlertValue) {
        this.humidityAboveMaxAlertValue = humidityAboveMaxAlertValue;
    }

    /**
     *
     * @return
     *         The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     *            The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getModelID() {
        // TODO Auto-generated method stub
        return "climatesensor";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeAllowed == null) ? 0 : changeAllowed.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((humidity == null) ? 0 : humidity.hashCode());
        result = prime * result + ((humidityAboveMaxAlertValue == null) ? 0 : humidityAboveMaxAlertValue.hashCode());
        result = prime * result + ((humidityBelowMinAlertValue == null) ? 0 : humidityBelowMinAlertValue.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((monitorable == null) ? 0 : monitorable.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((notAllowedReason == null) ? 0 : notAllowedReason.hashCode());
        result = prime * result + ((plottable == null) ? 0 : plottable.hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureSensorJSON other = (VerisureSensorJSON) obj;
        if (changeAllowed == null) {
            if (other.changeAllowed != null) {
                return false;
            }
        } else if (!changeAllowed.equals(other.changeAllowed)) {
            return false;
        }
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (humidity == null) {
            if (other.humidity != null) {
                return false;
            }
        } else if (!humidity.equals(other.humidity)) {
            return false;
        }
        if (humidityAboveMaxAlertValue == null) {
            if (other.humidityAboveMaxAlertValue != null) {
                return false;
            }
        } else if (!humidityAboveMaxAlertValue.equals(other.humidityAboveMaxAlertValue)) {
            return false;
        }
        if (humidityBelowMinAlertValue == null) {
            if (other.humidityBelowMinAlertValue != null) {
                return false;
            }
        } else if (!humidityBelowMinAlertValue.equals(other.humidityBelowMinAlertValue)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (monitorable == null) {
            if (other.monitorable != null) {
                return false;
            }
        } else if (!monitorable.equals(other.monitorable)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (notAllowedReason == null) {
            if (other.notAllowedReason != null) {
                return false;
            }
        } else if (!notAllowedReason.equals(other.notAllowedReason)) {
            return false;
        }
        if (plottable == null) {
            if (other.plottable != null) {
                return false;
            }
        } else if (!plottable.equals(other.plottable)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (temperature == null) {
            if (other.temperature != null) {
                return false;
            }
        } else if (!temperature.equals(other.temperature)) {
            return false;
        }
        if (temperatureAboveMaxAlertValue == null) {
            if (other.temperatureAboveMaxAlertValue != null) {
                return false;
            }
        } else if (!temperatureAboveMaxAlertValue.equals(other.temperatureAboveMaxAlertValue)) {
            return false;
        }
        if (temperatureBelowMinAlertValue == null) {
            if (other.temperatureBelowMinAlertValue != null) {
                return false;
            }
        } else if (!temperatureBelowMinAlertValue.equals(other.temperatureBelowMinAlertValue)) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
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
        builder.append("VerisureSensorJSON [");
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
        if (date != null) {
            builder.append("date=");
            builder.append(date);
            builder.append(", ");
        }
        if (id != null) {
            builder.append("id=");
            builder.append(id);
            builder.append(", ");
        }
        if (location != null) {
            builder.append("location=");
            builder.append(location);
        }
        builder.append("]");
        return builder.toString();
    }
}
