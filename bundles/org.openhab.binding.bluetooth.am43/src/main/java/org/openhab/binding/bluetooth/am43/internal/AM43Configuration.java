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
package org.openhab.binding.bluetooth.am43.internal;

/**
 * Configuration class for AM43 Binding.
 *
 * @author Connor Petty - Initial contribution
 */
public class AM43Configuration {

    public String address;
    public int refreshInterval;
    public boolean invertPosition;
    public int commandTimeout;

    @Override
    public String toString() {
        return "AM43Configuration [address=" + address + ", refreshInterval=" + refreshInterval + ", invertPosition="
                + invertPosition + ", commandTimeout=" + commandTimeout + "]";
    }
}
