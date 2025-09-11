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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public Information of the controller.
 * <p>
 *
 * Currently, only the ipAddress is used for discovery. More fields can be added on demand.
 * <p>
 * JSON example:
 *
 * <pre>
 * {
 *     "apiVersions": ["2.9","3.2"],
 *     "macAddress": "64-da-a0-ab-cd-ef",
 *     "shcIpAddress": "192.168.0.123",
 *     ...
 *     "shcGeneration": "SHC_1"
 * }
 * </pre>
 *
 * @author Gerd Zanker - Initial contribution
 */
public class PublicInformation {
    public List<String> apiVersions;
    public String macAddress;
    public String shcIpAddress;
    public String shcGeneration;
    public SoftwareUpdateState softwareUpdateState;

    public static boolean isValid(PublicInformation obj) {
        return obj != null && obj.macAddress != null && obj.shcIpAddress != null && obj.shcGeneration != null
                && obj.apiVersions != null && SoftwareUpdateState.isValid(obj.softwareUpdateState);
    }

    /**
     * Returns the API versions as comma-separated list.
     * 
     * @return a comma-separated list of API versions or <code>null</code> if {@link #apiVersions} is <code>null</code>.
     */
    public String getApiVersionsAsCommaSeparatedList() {
        if (apiVersions == null) {
            return null;
        }

        return apiVersions.stream().collect(Collectors.joining(", "));
    }
}
