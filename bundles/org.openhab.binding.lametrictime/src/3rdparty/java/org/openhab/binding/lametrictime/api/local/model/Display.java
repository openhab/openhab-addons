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

public class Display
{
    private Integer brightness;
    private String brightnessMode;
    private Integer height;
    private Screensaver screensaver;
    private String type;
    private Integer width;

    public Integer getBrightness()
    {
        return brightness;
    }

    public void setBrightness(Integer brightness)
    {
        this.brightness = brightness;
    }

    public Display withBrightness(Integer brightness)
    {
        this.brightness = brightness;
        return this;
    }

    public String getBrightnessMode()
    {
        return brightnessMode;
    }

    public void setBrightnessMode(String brightnessMode)
    {
        this.brightnessMode = brightnessMode;
    }

    public Display withBrightnessMode(String brightnessMode)
    {
        this.brightnessMode = brightnessMode;
        return this;
    }

    public Integer getHeight()
    {
        return height;
    }

    public void setHeight(Integer height)
    {
        this.height = height;
    }

    public Display withHeight(Integer height)
    {
        this.height = height;
        return this;
    }

    public Screensaver getScreensaver()
    {
        return screensaver;
    }

    public void setScreensaver(Screensaver screensaver)
    {
        this.screensaver = screensaver;
    }

    public Display withScreensaver(Screensaver screensaver)
    {
        this.screensaver = screensaver;
        return this;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Display withType(String type)
    {
        this.type = type;
        return this;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(Integer width)
    {
        this.width = width;
    }

    public Display withWidth(Integer width)
    {
        this.width = width;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Display [brightness=");
        builder.append(brightness);
        builder.append(", brightnessMode=");
        builder.append(brightnessMode);
        builder.append(", height=");
        builder.append(height);
        builder.append(", screensaver=");
        builder.append(screensaver);
        builder.append(", type=");
        builder.append(type);
        builder.append(", width=");
        builder.append(width);
        builder.append("]");
        return builder.toString();
    }
}
