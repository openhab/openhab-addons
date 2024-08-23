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
package org.openhab.binding.livisismarthome.internal.client.api.entity.device;

/**
 * Defines the {@link GatewayDTO} structure.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class GatewayDTO {

    /**
     * Version of the configuration. Changes each time the configuration was changed via the LIVISI client app.
     */
    private String configVersion;

    /**
     * @return the configuration version
     */
    public String getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the config version to set
     */
    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }
}
