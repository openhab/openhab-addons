/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.config;

/**
 * Serial Bridge configuration object.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class SerialBridgeConfiguration extends BridgeConfiguration {

    private String serialPort;

    public String getSerialPort() {
        return serialPort;
    }

}
