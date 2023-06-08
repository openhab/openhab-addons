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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for the Generation 3 Gateway information.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Info {
    private @NonNullByDefault({}) String fwVersion;
    private @NonNullByDefault({}) String serialNumber;

    public String getFwVersion() {
        return fwVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
