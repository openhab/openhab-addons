/*
 * Copyright 2017 Gregory Moyer
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

import com.google.gson.annotations.SerializedName;

public class BedSideStatus
{
    @SerializedName("isInBed")
    private Boolean inBed;
    private String alertDetailedMessage;
    private Integer sleepNumber;
    private Long alertId;
    private TimeSince lastLink;
    private Integer pressure; // appears to be in kPa

    public Boolean isInBed()
    {
        return inBed;
    }

    public void setInBed(Boolean inBed)
    {
        this.inBed = inBed;
    }

    public BedSideStatus withInBed(Boolean inBed)
    {
        setInBed(inBed);
        return this;
    }

    public String getAlertDetailedMessage()
    {
        return alertDetailedMessage;
    }

    public void setAlertDetailedMessage(String alertDetailedMessage)
    {
        this.alertDetailedMessage = alertDetailedMessage;
    }

    public BedSideStatus withAlertDetailedMessage(String alertDetailedMessage)
    {
        setAlertDetailedMessage(alertDetailedMessage);
        return this;
    }

    public Integer getSleepNumber()
    {
        return sleepNumber;
    }

    public void setSleepNumber(Integer sleepNumber)
    {
        this.sleepNumber = sleepNumber;
    }

    public BedSideStatus withSleepNumber(Integer sleepNumber)
    {
        setSleepNumber(sleepNumber);
        return this;
    }

    public Long getAlertId()
    {
        return alertId;
    }

    public void setAlertId(Long alertId)
    {
        this.alertId = alertId;
    }

    public BedSideStatus withAlertId(Long alertId)
    {
        setAlertId(alertId);
        return this;
    }

    public TimeSince getLastLink()
    {
        return lastLink;
    }

    public void setLastLink(TimeSince lastLink)
    {
        this.lastLink = lastLink;
    }

    public BedSideStatus withLastLink(TimeSince lastLink)
    {
        setLastLink(lastLink);
        return this;
    }

    public Integer getPressure()
    {
        return pressure;
    }

    public void setPressure(Integer pressure)
    {
        this.pressure = pressure;
    }

    public BedSideStatus withPressure(Integer pressure)
    {
        setPressure(pressure);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((alertDetailedMessage == null) ? 0 : alertDetailedMessage.hashCode());
        result = prime * result + ((alertId == null) ? 0 : alertId.hashCode());
        result = prime * result + ((inBed == null) ? 0 : inBed.hashCode());
        result = prime * result + ((lastLink == null) ? 0 : lastLink.hashCode());
        result = prime * result + ((pressure == null) ? 0 : pressure.hashCode());
        result = prime * result + ((sleepNumber == null) ? 0 : sleepNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof BedSideStatus))
        {
            return false;
        }
        BedSideStatus other = (BedSideStatus)obj;
        if (alertDetailedMessage == null)
        {
            if (other.alertDetailedMessage != null)
            {
                return false;
            }
        }
        else if (!alertDetailedMessage.equals(other.alertDetailedMessage))
        {
            return false;
        }
        if (alertId == null)
        {
            if (other.alertId != null)
            {
                return false;
            }
        }
        else if (!alertId.equals(other.alertId))
        {
            return false;
        }
        if (inBed == null)
        {
            if (other.inBed != null)
            {
                return false;
            }
        }
        else if (!inBed.equals(other.inBed))
        {
            return false;
        }
        if (lastLink == null)
        {
            if (other.lastLink != null)
            {
                return false;
            }
        }
        else if (!lastLink.equals(other.lastLink))
        {
            return false;
        }
        if (pressure == null)
        {
            if (other.pressure != null)
            {
                return false;
            }
        }
        else if (!pressure.equals(other.pressure))
        {
            return false;
        }
        if (sleepNumber == null)
        {
            if (other.sleepNumber != null)
            {
                return false;
            }
        }
        else if (!sleepNumber.equals(other.sleepNumber))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
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
