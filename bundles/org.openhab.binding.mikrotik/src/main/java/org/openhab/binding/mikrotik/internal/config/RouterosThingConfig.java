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
package org.openhab.binding.mikrotik.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosThingConfig} class contains fields mapping thing configuration parameters for
 * RouterOS bridge thing.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosThingConfig implements ConfigValidation {
    public String host = "rb3011";
    public int port = 8728;
    public String login = "admin";
    public String password = "";
    public int refresh = 10;

    @Override
    public boolean isValid() {
        return !host.isBlank() && !login.isBlank() && !password.isBlank() && refresh > 0 && port > 0;
    }

    @Override
    public String toString() {
        return String.format("RouterosThingConfig{host=%s, port=%d, login=%s, password=*****, refresh=%ds}", host, port,
                login, refresh);
    }
}
