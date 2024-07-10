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
package org.openhab.binding.insteon2.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonDeviceConfiguration} is the configuration for an insteon device thing.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonDeviceConfiguration {

    private String address = "";

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        String s = "";
        s += " address=" + address;
        return s;
    }
}
