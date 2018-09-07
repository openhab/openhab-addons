/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.handler.NeeoDeviceHandler;

/**
 * THe configuration class for the device used by {@link NeeoDeviceHandler}
 *
 * @author Tim Roberts - initial contribution
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
