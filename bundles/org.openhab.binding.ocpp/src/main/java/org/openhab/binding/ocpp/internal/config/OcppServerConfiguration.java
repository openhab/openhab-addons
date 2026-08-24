/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ocpp.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the {@code server} bridge — the OCPP JSON WebSocket endpoint.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppServerConfiguration {

    public String host = "0.0.0.0";
    public int port = 8887;
    public int heartbeatInterval = 300;

    public String meterValuesData = "";
    public int meterValueSampleInterval = -1;
    public int clockAlignedDataInterval = -1;
    public boolean disableRemoteTxAuthorization = false;
    public List<String> extraConfig = List.of();
    public int pingInterval = 0;
    public int requestTimeoutSeconds = 30;
    public String authPassword = "";
    public String tlsKeystorePath = "";
    public String tlsKeystorePassword = "";
    public List<String> whitelistTagIds = List.of();
    public List<String> chargerIds = List.of();
}
