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
 * Configuration for the {@code server} bridge — the OCPP JSON WebSocket endpoint. The boot-config
 * fields drive the ChangeConfiguration burst each charger receives after it boots; a field left at
 * its "unset" default (empty string, or a negative interval) is simply not sent.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppServerConfiguration {

    public String host = "0.0.0.0";
    public int port = 8887;
    public int heartbeatInterval = 300;

    /** MeterValues measurand list to configure on the charger (empty = leave as is). */
    public String meterValuesData = "";
    /** MeterValueSampleInterval seconds (negative = leave as is). */
    public int meterValueSampleInterval = -1;
    /** ClockAlignedDataInterval seconds (negative = leave as is). */
    public int clockAlignedDataInterval = -1;
    /** When true, configure the charger with AuthorizeRemoteTxRequests=false. */
    public boolean disableRemoteTxAuthorization = false;
    /** Extra ChangeConfiguration entries as "key=value" strings, sent verbatim on boot. */
    public List<String> vendorConfig = List.of();
    /** WebSocket ping interval (seconds); 0 disables ping-based connection-loss detection. */
    public int pingInterval = 0;
    /** idTag whitelist for Authorize / StartTransaction. Empty accepts every tag. */
    public List<String> tags = List.of();
    /** Charge point id allow-list. Empty accepts any charger; otherwise unlisted chargers are closed. */
    public List<String> chargers = List.of();
}
