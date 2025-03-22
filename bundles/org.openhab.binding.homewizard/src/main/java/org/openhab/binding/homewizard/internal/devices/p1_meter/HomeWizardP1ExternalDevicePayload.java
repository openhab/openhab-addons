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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardP1ExternalDevicePayload {

    private String uniqueId = "";
    private String type = "";
    private String timestamp = "";
    private String value = "";
    private String unit = "";

    public String getUniqueId() {
        return uniqueId;
    }

    public String getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return super.toString() + "  " + String.format("""
                Data [uniqueId: %s type: %s, timestamp: %s, value: %s, unit: %s]"

                """, uniqueId, type, timestamp, value, unit);
    }
}
