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
package org.openhab.binding.atv.internal.client.scan;

import java.net.Inet4Address;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an mDNS service.
 *
 * @param type service type, e.g. {@code _mediaremotetv._tcp.local}
 * @param name service instance name (without the type suffix)
 * @param address announced IPv4 address, or {@code null} when unknown
 * @param port announced port (0 when unknown, e.g. sleep proxy placeholder)
 * @param properties decoded TXT properties (keys compared case-insensitively)
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record MdnsService(String type, String name, @Nullable Inet4Address address, int port,
        Map<String, String> properties) {
}
