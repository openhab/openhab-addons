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

public class Device
{
    private Audio audio;
    private Bluetooth bluetooth;
    private Display display;
    private String id;
    private String mode;
    private String model;
    private String name;
    private String osVersion;
    private String serialNumber;
    private Wifi wifi;

    public Audio getAudio()
    {
        return audio;
    }

    public void setAudio(Audio audio)
    {
        this.audio = audio;
    }

    public Device withAudio(Audio audio)
    {
        this.audio = audio;
        return this;
    }

    public Bluetooth getBluetooth()
    {
        return bluetooth;
    }

    public void setBluetooth(Bluetooth bluetooth)
    {
        this.bluetooth = bluetooth;
    }

    public Device withBluetooth(Bluetooth bluetooth)
    {
        this.bluetooth = bluetooth;
        return this;
    }

    public Display getDisplay()
    {
        return display;
    }

    public void setDisplay(Display display)
    {
        this.display = display;
    }

    public Device withDisplay(Display display)
    {
        this.display = display;
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

    public Device withId(String id)
    {
        this.id = id;
        return this;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public Device withMode(String mode)
    {
        this.mode = mode;
        return this;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public Device withModel(String model)
    {
        this.model = model;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Device withName(String name)
    {
        this.name = name;
        return this;
    }

    public String getOsVersion()
    {
        return osVersion;
    }

    public void setOsVersion(String osVersion)
    {
        this.osVersion = osVersion;
    }

    public Device withOsVersion(String osVersion)
    {
        this.osVersion = osVersion;
        return this;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public Device withSerialNumber(String serialNumber)
    {
        this.serialNumber = serialNumber;
        return this;
    }

    public Wifi getWifi()
    {
        return wifi;
    }

    public void setWifi(Wifi wifi)
    {
        this.wifi = wifi;
    }

    public Device withWifi(Wifi wifi)
    {
        this.wifi = wifi;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Device [audio=");
        builder.append(audio);
        builder.append(", bluetooth=");
        builder.append(bluetooth);
        builder.append(", display=");
        builder.append(display);
        builder.append(", id=");
        builder.append(id);
        builder.append(", mode=");
        builder.append(mode);
        builder.append(", model=");
        builder.append(model);
        builder.append(", name=");
        builder.append(name);
        builder.append(", osVersion=");
        builder.append(osVersion);
        builder.append(", serialNumber=");
        builder.append(serialNumber);
        builder.append(", wifi=");
        builder.append(wifi);
        builder.append("]");
        return builder.toString();
    }
}
