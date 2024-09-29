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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.Side;

/**
 * The {@link SleepNumberRequest} holds the information used to set the sleep number
 * for a bed side.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepNumberRequest {
    private String bedId;
    private Integer sleepNumber;
    private Side side;

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public SleepNumberRequest withBedId(String bedId) {
        setBedId(bedId);
        return this;
    }

    public Integer getSleepNumber() {
        return sleepNumber;
    }

    public void setSleepNumber(Integer sleepNumber) {
        this.sleepNumber = sleepNumber;
    }

    public SleepNumberRequest withSleepNumber(Integer sleepNumber) {
        setSleepNumber(sleepNumber);
        return this;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public SleepNumberRequest withSide(Side side) {
        setSide(side);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
        result = prime * result + ((sleepNumber == null) ? 0 : sleepNumber.hashCode());
        result = prime * result + ((side == null) ? 0 : side.hashCode());
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
        if (!(obj instanceof SleepNumberRequest)) {
            return false;
        }
        SleepNumberRequest other = (SleepNumberRequest) obj;
        if (bedId == null) {
            if (other.bedId != null) {
                return false;
            }
        } else if (!bedId.equals(other.bedId)) {
            return false;
        }
        if (sleepNumber == null) {
            if (other.sleepNumber != null) {
                return false;
            }
        } else if (sleepNumber.equals(other.sleepNumber)) {
            return false;
        }
        if (side == null) {
            if (other.side != null) {
                return false;
            }
        } else if (!side.equals(other.side)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [bedId=");
        builder.append(bedId);
        builder.append(", sleepNumber=");
        builder.append(sleepNumber);
        builder.append(", side=");
        builder.append(side);
        builder.append("]");
        return builder.toString();
    }
}
