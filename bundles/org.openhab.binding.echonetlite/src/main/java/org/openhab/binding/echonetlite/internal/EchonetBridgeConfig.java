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
package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EchonetBridgeConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetBridgeConfig {

    @Nullable
    public String multicastAddress;
    public int port;

    @Override
    public String toString() {
        return "EchonetBridgeConfig{" + "multicastAddress='" + multicastAddress + '\'' + ", port=" + port + '}';
    }
}
