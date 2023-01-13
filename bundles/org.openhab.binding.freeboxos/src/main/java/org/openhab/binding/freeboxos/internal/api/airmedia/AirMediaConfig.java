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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.rest.ActivableConfigIntf;

/**
 * The {@link AirMediaConfig} is the Java class used to map the "AirMediaConfig" structure used by the AirMedia
 * configuration API
 *
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaConfig implements ActivableConfigIntf {
    private boolean enabled;
    private String password;

    private AirMediaConfig(boolean enabled, String password) {
        this.enabled = enabled;
        this.password = password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }
}
