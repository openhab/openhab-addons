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
     * @return the configuration version
     */
    public String getGatewayConfigVersion() {
        if (gateway != null) {
            return gateway.getConfigVersion();
        }
        return null;
    }
}
