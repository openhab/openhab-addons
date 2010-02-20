/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
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
package org.openhab.binding.lametrictime.api.local.model;

public class GoalData
{
    private Integer start;
    private Integer current;
    private Integer end;
    private String unit;

    public Integer getStart()
    {
        return start;
    }

    public void setStart(Integer start)
    {
        this.start = start;
    }

    public GoalData withStart(Integer start)
    {
        this.start = start;
        return this;
    }

    public Integer getCurrent()
    {
        return current;
    }

    public void setCurrent(Integer current)
    {
        this.current = current;
    }

    public GoalData withCurrent(Integer current)
    {
        this.current = current;
        return this;
    }

    public Integer getEnd()
    {
        return end;
    }

    public void setEnd(Integer end)
    {
        this.end = end;
    }

    public GoalData withEnd(Integer end)
    {
        this.end = end;
        return this;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public GoalData withUnit(String unit)
    {
        this.unit = unit;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((current == null) ? 0 : current.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        GoalData other = (GoalData)obj;
        if (current == null)
        {
            if (other.current != null)
            {
                return false;
            }
        }
        else if (!current.equals(other.current))
        {
            return false;
        }
        if (end == null)
        {
            if (other.end != null)
            {
                return false;
            }
        }
        else if (!end.equals(other.end))
        {
            return false;
        }
        if (start == null)
        {
            if (other.start != null)
            {
                return false;
            }
        }
        else if (!start.equals(other.start))
        {
            return false;
        }
        if (unit == null)
        {
            if (other.unit != null)
            {
                return false;
            }
        }
        else if (!unit.equals(other.unit))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("GoalData [start=");
        builder.append(start);
        builder.append(", current=");
        builder.append(current);
        builder.append(", end=");
        builder.append(end);
        builder.append(", unit=");
        builder.append(unit);
        builder.append("]");
        return builder.toString();
    }
}
