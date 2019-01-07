/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Hue API config object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueAuthorizedConfig extends HueUnauthorizedConfig {
    public String uuid = "5673dfa7-272c-4315-9955-252cdd86131c";

    public String timeformat = "24h";
    public String timezone = ZonedDateTime.now().getOffset().getId().replace("Z", "+00:00");
    public String UTC = "2018-11-10T15:24:23";
    public String localtime = "2018-11-10T16:24:23";

    public String devicename = "Philips Hue";

    public String fwversion = "0x262e0500";

    public boolean rfconnected = true;
    public int zigbeechannel = 15;
    public boolean linkbutton = false;
    public transient boolean createNewUserOnEveryEndpoint = false;
    public int panid = 19367;

    public boolean dhcp = true;
    public String gateway = "192.168.0.1";
    public String ipaddress = "192.168.0.46";
    public String netmask = "255.255.255.0";
    public int networkopenduration = 60;

    public String proxyaddress = "none";
    public int proxyport = 0;

    public final Map<String, HueUserAuth> whitelist = new TreeMap<>();
}
