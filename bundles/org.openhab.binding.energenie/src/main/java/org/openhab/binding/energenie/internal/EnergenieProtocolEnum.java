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
package org.openhab.binding.energenie.internal;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnergenieProtocolEnum} contains informations for parsing the readState() result.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

@NonNullByDefault
public enum EnergenieProtocolEnum {
    V20(STATE_ON),
    V21(V21_STATE_ON),
    WLAN(WLAN_STATE_ON);

    private final String statusOn;

    private EnergenieProtocolEnum(String statusOn) {
        this.statusOn = statusOn;
    }

    public String getStatusOn() {
        return statusOn;
    }
}
