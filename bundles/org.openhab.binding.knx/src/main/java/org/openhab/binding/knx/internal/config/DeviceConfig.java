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
package org.openhab.binding.knx.internal.config;

import java.math.BigDecimal;

/**
 * Configuration object for the device thing handler.
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - refactoring & cleanup
 */
public class DeviceConfig {

    private String address;
    private boolean fetch;
    private BigDecimal pingInterval;
    private BigDecimal readInterval;

    public String getAddress() {
        return address;
    }

    public Boolean getFetch() {
        return fetch;
    }

    public BigDecimal getPingInterval() {
        return pingInterval;
    }

    public BigDecimal getReadInterval() {
        return readInterval;
    }
}
