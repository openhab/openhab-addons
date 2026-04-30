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
package org.openhab.binding.dahuadoor.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DahuaDoorConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaDoorConfiguration {

    /**
     * Configuration parameters for the Dahua door device, including hostname, authentication
     * credentials, and snapshot path.
     */
    public String hostname = "";
    public String username = "";
    public String password = "";
    public String snapshotPath = "";
    public boolean useHttps = false;

    // WebRTC / go2rtc settings
    public boolean enableWebRTC = false;
    public String go2rtcPath = "";
    public int go2rtcApiPort = 1984;
    public int webRtcPort = 8555;
    public String stunServer = "stun.l.google.com:19302";
    public int rtspChannel = 1;
    public int rtspSubtype = 0;

    // SIP client settings
    public boolean enableSip = false;
    public String sipExtension = "";
    public String sipPassword = "";
    public int localSipPort = 5060;
    public String sipRealm = "VDP";
}
