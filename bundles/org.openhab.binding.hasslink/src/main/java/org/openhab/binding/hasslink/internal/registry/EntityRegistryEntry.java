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
package org.openhab.binding.hasslink.internal.registry;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EntityRegistryEntry} class represents an entity registry entry in Home Assistant.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record EntityRegistryEntry( //
        String entityId, //
        @Nullable String deviceId, //
        @Nullable String areaId, //
        Set<String> labels, //
        @Nullable String disabledBy, //
        @Nullable String name, //
        @Nullable String originalName //
) {
    public boolean isDisabled() {
        String disabled = this.disabledBy;
        return disabled != null && !disabled.isBlank();
    }
}
