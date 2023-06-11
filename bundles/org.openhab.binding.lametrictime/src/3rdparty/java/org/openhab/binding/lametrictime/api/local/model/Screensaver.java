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

public class Screensaver
{
    private Boolean enabled;
    private Modes modes;
    private String widget;

    public Boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Screensaver withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public Modes getModes()
    {
        return modes;
    }

    public void setModes(Modes modes)
    {
        this.modes = modes;
    }

    public Screensaver withModes(Modes modes)
    {
        this.modes = modes;
        return this;
    }

    public String getWidget()
    {
        return widget;
    }

    public void setWidget(String widget)
    {
        this.widget = widget;
    }

    public Screensaver withWidget(String widget)
    {
        this.widget = widget;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Screensaver [enabled=");
        builder.append(enabled);
        builder.append(", modes=");
        builder.append(modes);
        builder.append(", widget=");
        builder.append(widget);
        builder.append("]");
        return builder.toString();
    }
}
