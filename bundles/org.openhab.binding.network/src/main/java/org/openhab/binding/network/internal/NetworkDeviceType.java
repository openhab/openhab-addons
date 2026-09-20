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
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The kind of device a {@link org.openhab.binding.network.internal.handler.NetworkHandler} instance monitors.
 * It determines which presence detection method is configured for the thing.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public enum NetworkDeviceType {
    /** Presence detection by ICMP and ARP pings, optionally supported by DHCP sniffing. */
    PING,
    /** Presence detection by connecting to a TCP port. */
    TCP_SERVICE,
    /** Presence detection by sending an HTTP(S) request and evaluating the returned status code. */
    HTTP
}
