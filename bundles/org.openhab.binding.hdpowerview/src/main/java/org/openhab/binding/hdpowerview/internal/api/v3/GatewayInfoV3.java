/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api.v3;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.HubFirmware;

/**
 * DTO for the Generation 3 gateway information.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GatewayInfoV3 {
    public @Nullable String fwVersion;
    public @Nullable String serialNumber;

    public HubFirmware toHubFirmware() {
        Firmware firmware = new Firmware();
        String fwVersion = this.fwVersion;
        if (fwVersion != null) {
            @NonNull
            String[] parts = fwVersion.split("\\.");
            if (parts.length > 0) {
                firmware.revision = Integer.valueOf(parts[0]);
            }
            if (parts.length > 1) {
                firmware.subRevision = Integer.valueOf(parts[1]);
            }
            if (parts.length > 2) {
                firmware.build = Integer.valueOf(parts[2]);
            }
        }

        String name = "Generation 3 Hub";
        if (serialNumber != null) {
            name = String.format("%s (serial: %s)", name, serialNumber);
        }
        firmware.name = name;

        HubFirmware result = new HubFirmware();
        result.mainProcessor = firmware;
        return result;
    }
}
