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
package org.openhab.binding.freeboxos.internal.api.wifi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableConfig;

/**
 * The {@link WifiConfig} is the Java class used to map the "WifiGlobalConfig"
 * structure used by the Wifi global configuration API
 * https://dev.freebox.fr/sdk/os/wifi/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiConfig implements ActivableConfig {
    // Response classes
    public static class WifiConfigResponse extends Response<WifiConfig> {
    }

    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
