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
package org.openhab.binding.neohub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NeoHubConfiguration} class contains the thing configuration
 * parameters
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubConfiguration {

    public static final String HOST_NAME = "hostName";

    public String hostName = "";
    public int portNumber;
    public int pollingInterval;
    public int socketTimeout;
    public boolean preferLegacyApi;
    public String apiToken = "";
    public boolean useWebSocket;
}
