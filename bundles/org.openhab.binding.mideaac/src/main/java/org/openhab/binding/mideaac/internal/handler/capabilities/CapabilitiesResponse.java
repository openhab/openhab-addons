/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler.capabilities;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CapabilityResponse} handles the raw capability message
 * from the device
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CapabilitiesResponse {
    private final byte[] rawData;

    public CapabilitiesResponse(byte[] rawData) {
        this.rawData = rawData;
    }

    public byte[] getRawData() {
        return rawData;
    }
}
