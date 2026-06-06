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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SenseEnergyApiDevice that = (SenseEnergyApiDevice) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(icon, that.icon)
                && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, tags);
    }
}
