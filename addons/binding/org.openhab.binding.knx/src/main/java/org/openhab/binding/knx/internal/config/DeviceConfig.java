/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
