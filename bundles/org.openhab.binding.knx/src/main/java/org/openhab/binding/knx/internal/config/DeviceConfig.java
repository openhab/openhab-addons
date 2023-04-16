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
package org.openhab.binding.knx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration object for the device thing handler.
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - refactoring and cleanup
 */
@NonNullByDefault
public class DeviceConfig {
    private String address = "";
    private boolean fetch = false;
    private int pingInterval = 0;
    private int readInterval = 0;

    public String getAddress() {
        return address;
    }

    public Boolean getFetch() {
        return fetch;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public int getReadInterval() {
        return readInterval;
    }
}
