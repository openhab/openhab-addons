/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BedSideStatus} holds the BedSideStatus response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BedSideStatus {
    @SerializedName("isInBed")
    private Boolean inBed;
    private String alertDetailedMessage;
    private Integer sleepNumber;
    private Long alertId;
    private TimeSince lastLink;
    private Integer pressure; // appears to be in kPa

    public Boolean isInBed() {
        return inBed;
    }

    public void setInBed(Boolean inBed) {
        this.inBed = inBed;
    }

    public BedSideStatus withInBed(Boolean inBed) {
        setInBed(inBed);
        return this;
    }

    public String getAlertDetailedMessage() {
        return alertDetailedMessage;
    }

    public void setAlertDetailedMessage(String alertDetailedMessage) {
        this.alertDetailedMessage = alertDetailedMessage;
    }

    public BedSideStatus withAlertDetailedMessage(String alertDetailedMessage) {
        setAlertDetailedMessage(alertDetailedMessage);
        return this;
    }

    public Integer getSleepNumber() {
        return sleepNumber;
    }

    public void setSleepNumber(Integer sleepNumber) {
        this.sleepNumber = sleepNumber;
    }

    public BedSideStatus withSleepNumber(Integer sleepNumber) {
        setSleepNumber(sleepNumber);
        return this;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public BedSideStatus withAlertId(Long alertId) {
        setAlertId(alertId);
        return this;
    }

    public TimeSince getLastLink() {
        return lastLink;
    }

    public void setLastLink(TimeSince lastLink) {
        this.lastLink = lastLink;
    }

    public BedSideStatus withLastLink(TimeSince lastLink) {
        setLastLink(lastLink);
        return this;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public BedSideStatus withPressure(Integer pressure) {
        setPressure(pressure);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alertDetailedMessage == null) ? 0 : alertDetailedMessage.hashCode());
        result = prime * result + ((alertId == null) ? 0 : alertId.hashCode());
        result = prime * result + ((inBed == null) ? 0 : inBed.hashCode());
        result = prime * result + ((lastLink == null) ? 0 : lastLink.hashCode());
        result = prime * result + ((pressure == null) ? 0 : pressure.hashCode());
        result = prime * result + ((sleepNumber == null) ? 0 : sleepNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BedSideStatus)) {
            return false;
        }
        BedSideStatus other = (BedSideStatus) obj;
        if (alertDetailedMessage == null) {
            if (other.alertDetailedMessage != null) {
                return false;
            }
        } else if (!alertDetailedMessage.equals(other.alertDetailedMessage)) {
            return false;
        }
        if (alertId == null) {
            if (other.alertId != null) {
                return false;
            }
        } else if (!alertId.equals(other.alertId)) {
            return false;
        }
        if (inBed == null) {
            if (other.inBed != null) {
                return false;
            }
        } else if (!inBed.equals(other.inBed)) {
            return false;
        }
        if (lastLink == null) {
            if (other.lastLink != null) {
                return false;
            }
        } else if (!lastLink.equals(other.lastLink)) {
            return false;
        }
        if (pressure == null) {
            if (other.pressure != null) {
                return false;
            }
        } else if (!pressure.equals(other.pressure)) {
            return false;
        }
        if (sleepNumber == null) {
            if (other.sleepNumber != null) {
                return false;
            }
        } else if (!sleepNumber.equals(other.sleepNumber)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BedSideStatus [inBed=");
        builder.append(inBed);
        builder.append(", alertDetailedMessage=");
        builder.append(alertDetailedMessage);
        builder.append(", sleepNumber=");
        builder.append(sleepNumber);
        builder.append(", alertId=");
        builder.append(alertId);
        builder.append(", lastLink=");
        builder.append(lastLink);
        builder.append(", pressure=");
        builder.append(pressure);
        builder.append("]");
        return builder.toString();
    }
}
