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

import java.util.SortedMap;

import com.google.gson.annotations.SerializedName;

public class Application
{
    private SortedMap<String, Action> actions;
    @SerializedName("package")
    private String packageName;
    private String vendor;
    private String version;
    private String versionCode;
    private SortedMap<String, Widget> widgets;

    public SortedMap<String, Action> getActions()
    {
        return actions;
    }

    public void setActions(SortedMap<String, Action> actions)
    {
        this.actions = actions;
    }

    public Application withActions(SortedMap<String, Action> actions)
    {
        setActions(actions);
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

    public Application withPackageName(String packageName)
    {
        setPackageName(packageName);
        return this;
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public Application withVendor(String vendor)
    {
        setVendor(vendor);
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Application withVersion(String version)
    {
        setVersion(version);
        return this;
    }

    public String getVersionCode()
    {
        return versionCode;
    }

    public void setVersionCode(String versionCode)
    {
        this.versionCode = versionCode;
    }

    public Application withVersionCode(String versionCode)
    {
        setVersionCode(versionCode);
        return this;
    }

    public SortedMap<String, Widget> getWidgets()
    {
        return widgets;
    }

    public void setWidgets(SortedMap<String, Widget> widgets)
    {
        this.widgets = widgets;
    }

    public Application withWidgets(SortedMap<String, Widget> widgets)
    {
        setWidgets(widgets);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + ((versionCode == null) ? 0 : versionCode.hashCode());
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
        Application other = (Application)obj;
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
        if (versionCode == null)
        {
            if (other.versionCode != null)
            {
                return false;
            }
        }
        else if (!versionCode.equals(other.versionCode))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Application [actions=");
        builder.append(actions);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", vendor=");
        builder.append(vendor);
        builder.append(", version=");
        builder.append(version);
        builder.append(", versionCode=");
        builder.append(versionCode);
        builder.append(", widgets=");
        builder.append(widgets);
        builder.append("]");
        return builder.toString();
    }
}
