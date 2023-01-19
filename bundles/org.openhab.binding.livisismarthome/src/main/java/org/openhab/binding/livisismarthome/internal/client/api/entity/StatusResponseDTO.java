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
package org.openhab.binding.livisismarthome.internal.client.api.entity;

import org.openhab.binding.livisismarthome.internal.client.api.entity.device.GatewayDTO;

/**
 * Defines the structure of the status response
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class StatusResponseDTO {

    /**
     * The LIVISI SmartHome gateway. Can be null in case there is no registered for the current logged in user.
     */
    private GatewayDTO gateway;

    /**
     * Version of the configuration. Changes each time the configuration was changed via the LIVISI client app.
     */
    private String configVersion;

    /**
     * @return the configuration version
     */
    public String getConfigVersion() {
        // SHC 2 returns a gateway element with the config version.
        if (gateway != null) {
            return gateway.getConfigVersion();
        }
        // SHC 1 (classic) has no gateway element, the configVersion is returned directly within the response object.
        return configVersion;
    }
}
