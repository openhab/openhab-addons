/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxWifiGlobalConfig} is the Java class used to map the "WifiGlobalConfig"
 * structure used by the Wifi global configuration API
 * https://dev.freebox.fr/sdk/os/wifi/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxWifiGlobalConfig {
    private Boolean enabled;
    private String macFilterState;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getMacFilterState() {
        return macFilterState;
    }

    public void setMacFilterState(String macFilterState) {
        this.macFilterState = macFilterState;
    }
}
