/*
 * Copyright 2021 Mark Hilbush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import org.openhab.binding.sleepiq.api.enums.Side;

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
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
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
