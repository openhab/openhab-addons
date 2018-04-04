/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.config.internal;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Configuration parameters for Serial Bridge
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairSerialBridgeConfig {
    /** path or name of serial port, usually /dev/ttyUSB0 format for linux/mac, COM1 for windows */
    public String serialPort;
    /** ID to use when sending commands on the Pentair RS485 bus. */
    public Integer id;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serialPort", serialPort).append("id", id).toString();
    }
}
