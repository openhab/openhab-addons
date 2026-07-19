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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the accumulated response to an mDNS request.
 *
 * @param services discovered services
 * @param deepSleep whether the device is in deep sleep (answered by a sleep proxy)
 * @param model device model from {@code _device-info._tcp.local}, or {@code null}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record MdnsResponse(List<MdnsService> services, boolean deepSleep, @Nullable String model) {

    /**
     * An empty response.
     */
    public static final MdnsResponse EMPTY = new MdnsResponse(List.of(), false, null);
}
