/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Home appliance model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class HomeAppliance {
    private final String name;
    private final String brand;
    private final String vib;
    private final boolean connected;
    private final String type;
    private final String enumber;
    private final String haId;

    public HomeAppliance(String haId, String name, String brand, String vib, boolean connected, String type,
            String enumber) {
        this.haId = haId;
        this.name = name;
        this.brand = brand;
        this.vib = vib;
        this.connected = connected;
        this.type = type;
        this.enumber = enumber;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getVib() {
        return vib;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getType() {
        return type;
    }

    public String getEnumber() {
        return enumber;
    }

    public String getHaId() {
        return haId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + haId.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HomeAppliance other = (HomeAppliance) obj;
        return haId.equals(other.haId);
    }

    @Override
    public String toString() {
        return "HomeAppliance [haId=" + haId + ", name=" + name + ", brand=" + brand + ", vib=" + vib + ", connected="
                + connected + ", type=" + type + ", enumber=" + enumber + "]";
    }
}
