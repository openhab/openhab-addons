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
package org.openhab.binding.mystrom.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MyStromDeviceInfo} class contains fields mapping thing thing properties
 *
 * @author Frederic Chastagnol - Initial contribution
 */
@NonNullByDefault
public class MyStromDeviceInfo {
    public String version = "";
    public String mac = "";
    public long type;
    public String ssid = "";
    public String ip = "";
    public String mask = "";
    public String gw = "";
    public String dns = "";
    @SerializedName("static")
    public boolean staticState = false;
    public boolean connected = false;
}
