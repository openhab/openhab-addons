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
package org.openhab.binding.anthem.internal;

import static org.openhab.binding.anthem.internal.AnthemBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AnthemConfiguration} is responsible for storing the Anthem thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemConfiguration {
    public String host = "";

    public int port = DEFAULT_PORT;

    public int reconnectIntervalMinutes = DEFAULT_RECONNECT_INTERVAL_MINUTES;

    public int commandDelayMsec = DEFAULT_COMMAND_DELAY_MSEC;

    public boolean isValid() {
        return !host.isBlank();
    }

    @Override
    public String toString() {
        return "AnthemConfiguration{ host=" + host + ", port=" + port + ", reconectIntervalMinutes="
                + reconnectIntervalMinutes + ", commandDelayMsec=" + commandDelayMsec + " }";
    }
}
