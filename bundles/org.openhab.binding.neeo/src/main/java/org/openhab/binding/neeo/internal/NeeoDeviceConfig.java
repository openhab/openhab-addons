/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration used by {@link org.openhab.binding.neeo.internal.handler.NeeoDeviceHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceConfig {

    /** The NEEO device key */
    @Nullable
    private String deviceKey;

    /**
     * Gets the device key
     *
     * @return the device key
     */
    @Nullable
    public String getDeviceKey() {
        return deviceKey;
    }

    /**
     * Sets the device key.
     *
     * @param deviceKey the new device key
     */
    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }
}
