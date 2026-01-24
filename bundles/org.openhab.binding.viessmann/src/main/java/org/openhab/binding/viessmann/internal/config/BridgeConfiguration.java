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
package org.openhab.binding.viessmann.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class BridgeConfiguration {

    public String user = "";
    public String password = "";
    public String apiKey = "";
    public String installationId = "";
    public String gatewaySerial = "";
    public int apiCallLimit = 1450;
    public int bufferApiCommands = 450;
    public int pollingInterval = 0;
    public int pollingIntervalErrors = 60;
    public boolean disablePolling = false;
}
