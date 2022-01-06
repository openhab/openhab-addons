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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxUPnPAVConfig} is the Java class used to map the "UPnPAVConfig"
 * structure used by the UPnP AV configuration API
 * https://dev.freebox.fr/sdk/os/upnpav/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxUPnPAVConfig {
    private Boolean enabled;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
