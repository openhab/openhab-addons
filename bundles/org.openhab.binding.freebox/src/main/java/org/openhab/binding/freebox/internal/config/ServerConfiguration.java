/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ServerConfiguration} is responsible for holding
 * configuration informations needed to access/poll the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ServerConfiguration {
    public static final String HOST_ADDRESS = "hostAddress";
    public static final String HTTPS_AVAILABLE = "httpsAvailable";
    public static final String REMOTE_HTTPS_PORT = "remoteHttpsPort";

    public String hostAddress = "mafreebox.freebox.fr";
    public String appToken = "";
    public boolean httpsAvailable = false;
    public long remoteHttpsPort = -1L;
    public Boolean background = true;
    public Boolean discoverPhone = true;
    public Boolean discoverNetDevice = false;
    public Boolean discoverAirPlayReceiver = true;
    public Boolean discoverVM = true;
    public Integer refreshInterval = 30;
}
