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

public class Bluetooth
{
    private Boolean active;

    /*
     * API sometimes calls this field 'mac' and other times calls it 'address'.
     * Additionally, Gson uses fields only (not methods). Therefore, if use the
     * same instance of this class to read one value and then try to write the
     * other without calling the setter, it won't work (the other value will be
     * null).
     */
    private String mac;
    private String address;

    private Boolean available;
    private Boolean discoverable;
    private String name;
    private Boolean pairable;

    public Boolean isActive()
    {
        return active;
    }

    public void setActive(Boolean active)
    {
        this.active = active;
    }

    public Bluetooth withActive(Boolean active)
    {
        this.active = active;
        return this;
    }

    public String getMac()
    {
        return mac == null ? address : mac;
    }

    public void setMac(String mac)
    {
        this.mac = mac;
        this.address = mac;
    }

    public Bluetooth withMac(String mac)
    {
        setMac(mac);
        return this;
    }

    public Boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(Boolean available)
    {
        this.available = available;
    }

    public Bluetooth withAvailable(Boolean available)
    {
        this.available = available;
        return this;
    }

    public Boolean isDiscoverable()
    {
        return discoverable;
    }

    public void setDiscoverable(Boolean discoverable)
    {
        this.discoverable = discoverable;
    }

    public Bluetooth withDiscoverable(Boolean discoverable)
    {
        this.discoverable = discoverable;
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

    public Bluetooth withName(String name)
    {
        this.name = name;
        return this;
    }

    public Boolean isPairable()
    {
        return pairable;
    }

    public void setPairable(Boolean pairable)
    {
        this.pairable = pairable;
    }

    public Bluetooth withPairable(Boolean pairable)
    {
        this.pairable = pairable;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Bluetooth [active=");
        builder.append(active);
        builder.append(", mac=");
        builder.append(getMac());
        builder.append(", available=");
        builder.append(available);
        builder.append(", discoverable=");
        builder.append(discoverable);
        builder.append(", name=");
        builder.append(name);
        builder.append(", pairable=");
        builder.append(pairable);
        builder.append("]");
        return builder.toString();
    }
}
