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

public class Sound
{
    private String category;
    private String id;
    private Integer repeat;

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public Sound withCategory(String category)
    {
        this.category = category;
        return this;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Sound withId(String id)
    {
        this.id = id;
        return this;
    }

    public Integer getRepeat()
    {
        return repeat;
    }

    public void setRepeat(Integer repeat)
    {
        this.repeat = repeat;
    }

    public Sound withRepeat(Integer repeat)
    {
        this.repeat = repeat;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Sound [category=");
        builder.append(category);
        builder.append(", id=");
        builder.append(id);
        builder.append(", repeat=");
        builder.append(repeat);
        builder.append("]");
        return builder.toString();
    }
}
