/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api.dto;

/**
 * The {@link SenseEnergyApiDevice } is the dto for the api sense discovered devices
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiDevice {
    public String id;
    public String name;
    public String icon;
    public SenseEnergyApiDeviceTags tags;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SenseEnergyApiDevice that = (SenseEnergyApiDevice) o;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (icon != null ? !icon.equals(that.icon) : that.icon != null)
            return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
