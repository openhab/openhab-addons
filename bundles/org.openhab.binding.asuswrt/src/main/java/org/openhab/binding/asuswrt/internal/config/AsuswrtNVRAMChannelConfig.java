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

package org.openhab.binding.asuswrt.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AsuswrtNVRAMChannelConfig} class defines the configuration for NVRAM channels in the Asuswrt binding.
 *
 * @author Jeff James - added nvram service configuration
 */
@NonNullByDefault
public record AsuswrtNVRAMChannelConfig(String name, Boolean readonly, @Nullable String service) {
    public AsuswrtNVRAMChannelConfig {
        if (readonly == null) {
            readonly = Boolean.TRUE;
        }
    }
}
