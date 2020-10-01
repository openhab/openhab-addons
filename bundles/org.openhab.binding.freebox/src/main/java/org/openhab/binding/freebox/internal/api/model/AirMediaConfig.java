/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link AirMediaConfig} is the Java class used to map the "AirMediaConfig"
 * structure used by the AirMedia configuration API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class AirMediaConfig {
    protected boolean enabled;
    protected String password;

    public AirMediaConfig(boolean enabled) {
        this(enabled, "");
    }

    public AirMediaConfig(boolean enabled, String password) {
        this.enabled = enabled;
        this.password = password;
    }

    public Boolean isEnabled() {
        return enabled;
    }
}
