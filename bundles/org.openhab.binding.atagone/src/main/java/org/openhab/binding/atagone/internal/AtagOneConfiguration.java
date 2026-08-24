/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atagone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thing configuration parameters for the ATAG ONE thermostat.
 * Field names must match the parameter {@code name} attributes in {@code config.xml}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneConfiguration {

    /** IP address or hostname of the thermostat. Required. */
    public String hostname = "";

    /** HTTP port of the local API. Defaults to 10000. */
    public int port = 10000;

    /** Poll interval in seconds. */
    public int refreshInterval = 30;

    /**
     * Stable client identifier used for pairing (MAC-style hex string).
     * Generated on first run and persisted; empty string means "not yet set".
     */
    public String clientId = "";
}
