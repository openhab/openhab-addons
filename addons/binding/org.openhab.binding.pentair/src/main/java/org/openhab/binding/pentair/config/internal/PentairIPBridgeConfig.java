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
 * Configuration parameters for IP Bridge
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairIPBridgeConfig {
    /** IP address of destination */
    public String address;
    /** Port of destination */
    public Integer port;

    /** ID to use when sending commands on the Pentair RS485 bus. */
    public Integer id;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("address", address).append("port", port).append("id", id).toString();
    }
}
