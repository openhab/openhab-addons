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

import java.util.List;

public class SleepersResponse
{
    private List<Sleeper> sleepers;

    public List<Sleeper> getSleepers()
    {
        return sleepers;
    }

    public void setSleepers(List<Sleeper> sleepers)
    {
        this.sleepers = sleepers;
    }

    public SleepersResponse withSleepers(List<Sleeper> sleepers)
    {
        setSleepers(sleepers);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sleepers == null) ? 0 : sleepers.hashCode());
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
        if (!(obj instanceof SleepersResponse))
        {
            return false;
        }
        SleepersResponse other = (SleepersResponse)obj;
        if (sleepers == null)
        {
            if (other.sleepers != null)
            {
                return false;
            }
        }
        else if (!sleepers.equals(other.sleepers))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepersResponse [sleepers=");
        builder.append(sleepers);
        builder.append("]");
        return builder.toString();
    }
}
