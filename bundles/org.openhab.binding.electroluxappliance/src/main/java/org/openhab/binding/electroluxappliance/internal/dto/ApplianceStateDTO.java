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
package org.openhab.binding.electroluxappliance.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApplianceStateDTO} class defines the DTO for the Electrolux Appliance State.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ApplianceStateDTO {
    private String applianceId = "";
    private String connectionState = "";
    private String status = "";

    public String getApplianceId() {
        return applianceId;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public String getStatus() {
        return status;
    }

    // You can optionally add a toString() method for easier debugging
    @Override
    public String toString() {
        return "ApplianceStateDTO{" + "applianceId='" + applianceId + '\'' + ", connectionState='" + connectionState
                + '\'' + ", status='" + status + '\'' + '}';
    }
}
