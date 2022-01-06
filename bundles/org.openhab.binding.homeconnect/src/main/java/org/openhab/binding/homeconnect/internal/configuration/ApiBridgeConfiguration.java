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
package org.openhab.binding.homeconnect.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ApiBridgeConfiguration {

    private String clientId = "";
    private String clientSecret = "";
    private boolean simulator;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isSimulator() {
        return simulator;
    }

    public void setSimulator(boolean simulator) {
        this.simulator = simulator;
    }

    @Override
    public String toString() {
        return "ApiBridgeConfiguration [clientId=" + clientId + ", clientSecret=" + clientSecret + ", simulator="
                + simulator + "]";
    }
}
