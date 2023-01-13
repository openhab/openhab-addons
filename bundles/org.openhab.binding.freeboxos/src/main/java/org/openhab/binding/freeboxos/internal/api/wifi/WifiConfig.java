/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.rest.ActivableConfigIntf;

/**
 * The {@link WifiConfig} is the Java class used to map the "Wifi Config" structure
 *
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiConfig implements ActivableConfigIntf {
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
