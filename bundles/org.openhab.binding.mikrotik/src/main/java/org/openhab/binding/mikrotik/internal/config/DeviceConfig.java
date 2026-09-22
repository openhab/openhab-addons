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
package org.openhab.binding.mikrotik.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceConfig} Gives access to the configuration of any MikrotikDeviceHandler's
 * device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class DeviceConfig implements ConfigValidation {
    public String mac = "";

    @Override
    public boolean isValid() {
        return !mac.isBlank();
    }
}
