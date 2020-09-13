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
package org.openhab.binding.lametrictime.api.cloud.model;

public class Thumb
{
    private String original;
    private String small;
    private String large;
    private String xlarge;

    public String getOriginal()
    {
        return original;
    }

    public void setOriginal(String original)
    {
        this.original = original;
    }

    public Thumb withOriginal(String original)
    {
        this.original = original;
        return this;
    }

    public String getSmall()
    {
        return small;
    }

    public void setSmall(String small)
    {
        this.small = small;
    }

    public Thumb withSmall(String small)
    {
        this.small = small;
        return this;
    }

    public String getLarge()
    {
        return large;
    }

    public void setLarge(String large)
    {
        this.large = large;
    }

    public Thumb withLarge(String large)
    {
        this.large = large;
        return this;
    }

    public String getXlarge()
    {
        return xlarge;
    }

    public void setXlarge(String xlarge)
    {
        this.xlarge = xlarge;
    }

    public Thumb withXlarge(String xlarge)
    {
        this.xlarge = xlarge;
        return this;
    }
}
