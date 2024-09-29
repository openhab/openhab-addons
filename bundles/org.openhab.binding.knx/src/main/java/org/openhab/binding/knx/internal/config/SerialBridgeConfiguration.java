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
package org.openhab.binding.knx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Serial Bridge configuration object.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class SerialBridgeConfiguration extends BridgeConfiguration {

    private String serialPort = "";
    private boolean useCemi = false;

    public String getSerialPort() {
        return serialPort;
    }

    public boolean useCemi() {
        return useCemi;
    }
}
