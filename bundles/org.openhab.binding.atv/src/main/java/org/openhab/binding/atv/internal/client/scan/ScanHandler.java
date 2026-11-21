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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.conf.Service;

/**
 * Per-protocol handler parsing a discovered mDNS service into a configuration service.
 *
 * @author Dan Cunningham - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface ScanHandler {

    /**
     * Result of a scan handler.
     *
     * @param name device name
     * @param service parsed service
     */
    record Result(String name, Service service) {
    }

    /**
     * Parses a discovered mDNS service.
     *
     * @param service the discovered service
     * @param response the full response the service was found in
     * @return device name and parsed service, or {@code null} when the service should
     *         not contribute a configuration entry
     */
    @Nullable
    Result handle(MdnsService service, MdnsResponse response);
}
