/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

/**
 * Hue API config object. Also accessible for non-authorized users.
 * Enpoint: /api/config
 *
 * @author David Graeff - Initial contribution
 */
public class HueUnauthorizedConfig {
    public String apiversion = "1.16.0";
    public String bridgeid = "00212EFFFF022F6E";
    public String datastoreversion = "60";
    public String starterkitid = "";
    public String modelid = "BSB002";
    public String name = "openHAB Devices";

    public String swversion = "2.5.46";
    public String mac = "b8:27:eb:1d:d8:0c";
    public boolean factorynew = false;
    public String replacesbridgeid = null;
}
