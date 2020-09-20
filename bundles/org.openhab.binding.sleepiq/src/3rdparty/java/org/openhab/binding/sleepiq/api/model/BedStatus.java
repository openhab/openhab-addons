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

public class BedStatus
{
    private Long status;
    private String bedId;
    private BedSideStatus leftSide;
    private BedSideStatus rightSide;

    public Long getStatus()
    {
        return status;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    public BedStatus withStatus(Long status)
    {
        setStatus(status);
        return this;
    }

    public String getBedId()
    {
        return bedId;
    }

    public void setBedId(String bedId)
    {
        this.bedId = bedId;
    }

    public BedStatus withBedId(String bedId)
    {
        setBedId(bedId);
        return this;
    }

    public BedSideStatus getLeftSide()
    {
        return leftSide;
    }

    public void setLeftSide(BedSideStatus leftSide)
    {
        this.leftSide = leftSide;
    }

    public BedStatus withLeftSide(BedSideStatus leftSide)
    {
        setLeftSide(leftSide);
        return this;
    }

    public BedSideStatus getRightSide()
    {
        return rightSide;
    }

    public void setRightSide(BedSideStatus rightSide)
    {
        this.rightSide = rightSide;
    }

    public BedStatus withRightSide(BedSideStatus rightSide)
    {
        setRightSide(rightSide);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
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
        if (!(obj instanceof BedStatus))
        {
            return false;
        }
        BedStatus other = (BedStatus)obj;
        if (bedId == null)
        {
            if (other.bedId != null)
            {
                return false;
            }
        }
        else if (!bedId.equals(other.bedId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BedStatus [status=");
        builder.append(status);
        builder.append(", bedId=");
        builder.append(bedId);
        builder.append(", leftSide=");
        builder.append(leftSide);
        builder.append(", rightSide=");
        builder.append(rightSide);
        builder.append("]");
        return builder.toString();
    }
}
