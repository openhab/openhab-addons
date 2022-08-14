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

import java.util.Map;

import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

public class Widget
{
    private String id;
    @SerializedName("package")
    private String packageName;
    private Integer index;
    private Map<String, JsonPrimitive> settings;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Widget withId(String id)
    {
        setId(id);
        return this;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public Widget withPackageName(String packageName)
    {
        setPackageName(packageName);
        return this;
    }

    public Integer getIndex()
    {
        return index;
    }

    public void setIndex(Integer index)
    {
        this.index = index;
    }

    public Widget withIndex(Integer index)
    {
        setIndex(index);
        return this;
    }

    public Map<String, JsonPrimitive> getSettings()
    {
        return settings;
    }

    public void setSettings(Map<String, JsonPrimitive> settings)
    {
        this.settings = settings;
    }

    public Widget withSettings(Map<String, JsonPrimitive> settings)
    {
        setSettings(settings);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + ((settings == null) ? 0 : settings.hashCode());
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
        Widget other = (Widget)obj;
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!id.equals(other.id))
        {
            return false;
        }
        if (index == null)
        {
            if (other.index != null)
            {
                return false;
            }
        }
        else if (!index.equals(other.index))
        {
            return false;
        }
        if (packageName == null)
        {
            if (other.packageName != null)
            {
                return false;
            }
        }
        else if (!packageName.equals(other.packageName))
        {
            return false;
        }
        if (settings == null)
        {
            if (other.settings != null)
            {
                return false;
            }
        }
        else if (!settings.equals(other.settings))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Widget [id=");
        builder.append(id);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", index=");
        builder.append(index);
        builder.append(", settings=");
        builder.append(settings);
        builder.append("]");
        return builder.toString();
    }
}
