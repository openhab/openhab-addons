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
package org.openhab.binding.openwebnet.internal.handler.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * BUS Bridge configuration object
 *
 * @author Massimo Valla - Initial contribution
 *
 */
@NonNullByDefault
public class OpenWebNetBusBridgeConfig {

    private BigDecimal port = new BigDecimal(20000);
    private @Nullable String host;
    private String passwd = "12345";
    private boolean discoveryByActivation = false;

    public BigDecimal getPort() {
        return port;
    }

    public @Nullable String getHost() {
        return host;
    }

    public String getPasswd() {
        return passwd;
    }

    public Boolean getDiscoveryByActivation() {
        return discoveryByActivation;
    }
}
