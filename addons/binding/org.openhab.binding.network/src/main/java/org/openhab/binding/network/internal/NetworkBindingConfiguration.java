/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Contains the binding configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author David Graeff
 */
public class NetworkBindingConfiguration {
    public Boolean allowSystemPings = true;
    public Boolean allowDHCPlisten = true;
    public BigDecimal cacheDeviceStateTimeInMS = BigDecimal.valueOf(2000);
    public String arpPingToolPath = "arping";

    public void update(@NonNull NetworkBindingConfiguration newConfiguration) {
        this.allowSystemPings = newConfiguration.allowSystemPings;
        this.allowDHCPlisten = newConfiguration.allowDHCPlisten;
        this.cacheDeviceStateTimeInMS = newConfiguration.cacheDeviceStateTimeInMS;
        this.arpPingToolPath = newConfiguration.arpPingToolPath;
    }
}
