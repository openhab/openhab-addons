/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Request for the status of a Power Station. Answer can be deserialized in a {@link StatusResponse}
 *
 * @author Iwan Bron - Initial contribution
 */

@NonNullByDefault
public class StatusRequest {
    private String powerStationId;

    public StatusRequest(String powerStationId) {
        this.powerStationId = powerStationId;
    }

    public void setPowerStationId(String powerStationId) {
        this.powerStationId = powerStationId;
    }

    public String getPowerStationId() {
        return powerStationId;
    }
}
