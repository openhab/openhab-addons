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
import org.eclipse.jdt.annotation.Nullable;

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
    public static final String APP_TOKEN = "appToken";

    public String hostAddress = "mafreebox.freebox.fr";
    public @Nullable String appToken;
    public int refreshInterval = 30;
    public long remoteHttpsPort = -1L;
    public boolean discoverPhone = true;
    public boolean discoverNetDevice = false;
    public boolean discoverVM = true;
    public boolean httpsAvailable = false;

    public boolean isValidToken() {
        return appToken != null && !appToken.isEmpty();
    }
}
