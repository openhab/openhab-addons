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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Device type as reported by the cloud API. {@code parentType} names the device family
 * ({@code Heaters}, {@code Sockets}, {@code Sensors}, {@code Air Purifiers}, {@code Floor Heaters})
 * and is the discriminator for the shape of {@link DeviceSettingsDTO}. {@code childType} names the
 * concrete model, for example {@code GL-Panel Heater G4}.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record DeviceTypeDTO(@Nullable TypeRefDTO parentType, @Nullable TypeRefDTO childType) {

    public record TypeRefDTO(@Nullable String id, @Nullable String name) {
    }

    public String parentTypeName() {
        final TypeRefDTO parent = parentType;
        if (parent == null) {
            return "";
        }
        final String name = parent.name();
        return name == null ? "" : name;
    }
}
