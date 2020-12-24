/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luxtronik.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LuxtronikConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 * @author John Cocula - made port configurable
 * @author Hilbrand Bouwkamp - Migrated to openHAB 3
 */
@NonNullByDefault
public class LuxtronikConfiguration {
    private static final int DEFAULT_PORT = 8888;
    private static final long DEFAULT_REFRESH_INTERVAL = 60L;

    /* The IP address to connect to */
    public String ip = "";

    /* the port to connect to. */
    public int port = DEFAULT_PORT;

    /** Default refresh interval (currently 1 minute) */
    public long refreshInterval = DEFAULT_REFRESH_INTERVAL;
}
