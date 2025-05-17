/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ZwaveJSBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeConfiguration {

    public String hostname = "";
    public int port = 3000;
    public int maxMessageSize = 2097152;
    public boolean configurationChannels = false;

    public boolean isValid() {
        return port > 0 && !hostname.isBlank();
    }
}
