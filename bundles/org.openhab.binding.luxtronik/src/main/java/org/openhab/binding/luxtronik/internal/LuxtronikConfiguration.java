/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * @author Christoph Scholz - Finished migration to openHAB 3
 */
@NonNullByDefault
public class LuxtronikConfiguration {
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_POLLING_INTERVAL = 60;
    /**
     * The host or IP address to connect to
     */
    public String host = "";

    /**
     * The TCP port number to connect to
     */
    public Integer port = DEFAULT_PORT;

    /**
     * The socket connection timeout
     */
    public Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    /**
     * The polling interval in s
     */
    public Integer pollingInterval = DEFAULT_POLLING_INTERVAL;
}
