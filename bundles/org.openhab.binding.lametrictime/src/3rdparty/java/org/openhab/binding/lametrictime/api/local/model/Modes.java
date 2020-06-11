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

public class Modes
{
    private TimeBased timeBased;
    private WhenDark whenDark;

    public TimeBased getTimeBased()
    {
        return timeBased;
    }

    public void setTimeBased(TimeBased timeBased)
    {
        this.timeBased = timeBased;
    }

    public Modes withTimeBased(TimeBased timeBased)
    {
        this.timeBased = timeBased;
        return this;
    }

    public WhenDark getWhenDark()
    {
        return whenDark;
    }

    public void setWhenDark(WhenDark whenDark)
    {
        this.whenDark = whenDark;
    }

    public Modes withWhenDark(WhenDark whenDark)
    {
        this.whenDark = whenDark;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Modes [timeBased=");
        builder.append(timeBased);
        builder.append(", whenDark=");
        builder.append(whenDark);
        builder.append("]");
        return builder.toString();
    }
}
