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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import java.util.List;

/**
 * Public Information of the controller.
 * <p>
 *
 * Currently, only the ipAddress is used for discovery. More fields can be added on demand.
 * <p>
 * Json example:
 *
 * <pre>
 * {
 * "apiVersions":["1.2","2.1"],
 * ...
 * "shcIpAddress":"192.168.1.2",
 * ...
 * }
 * </pre>
 *
 * @author Gerd Zanker - Initial contribution
 */
public class PublicInformation {
    public PublicInformation() {
        this.shcIpAddress = "";
        this.shcGeneration = "";
    }

    public List<String> apiVersions;
    public String shcIpAddress;
    public String shcGeneration;
}
