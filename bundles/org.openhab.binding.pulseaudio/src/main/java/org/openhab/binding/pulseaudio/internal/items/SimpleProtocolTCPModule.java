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
package org.openhab.binding.pulseaudio.internal.items;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a simple protocol tcp module instance
 *
 * @author Miguel Álvarez Díez - Initial contribution
 */
@NonNullByDefault
public class SimpleProtocolTCPModule extends Module {
    private final int port;

    public SimpleProtocolTCPModule(int id, String name, int port, @Nullable String arguments) {
        super(id, name, arguments);
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
