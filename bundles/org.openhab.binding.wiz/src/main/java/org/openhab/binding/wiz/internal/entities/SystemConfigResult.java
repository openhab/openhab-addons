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
package org.openhab.binding.wiz.internal.entities;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "result" of one request for a bulb's system
 * configuration I assume the same packet could be used as the param of a
 * 'setSystemConfig' request, but I'm not willing to risk ruining my bulbs by
 * trying it.
 *
 * The incoming JSON looks like this:
 *
 * {"method": "getSystemConfig", "id": 22, "env": "pro", "result": {"mac":
 * "theBulbMacAddress", "homeId": xxxxxx, "roomId": xxxxxx, "homeLock": false,
 * "pairingLock": false, "typeId": 0, "moduleName": "ESP01_SHRGB1C_31",
 * "fwVersion": "1.15.2", "groupId": 0, "drvConf":[33,1]}}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class SystemConfigResult {
    // The MAC address the response is coming from
    @Expose
    public String mac = "";
    // Home ID of the bulb
    @Expose
    public int homeId;
    // The ID of room the bulb is assigned to
    @Expose
    public int roomId;
    // Not sure what the home lock is
    @Expose
    public boolean homeLock;
    // Also not sure about the pairing lock
    @Expose
    public boolean pairingLock;
    // Obviously a type ID
    // The value is 0 for both BR30 and A19 full color bulbs
    @Expose
    public int typeId;
    // The module name
    // The value is "ESP01_SHRGB1C_31" for both BR30 and A19 full color bulbs
    @Expose
    public String moduleName = EXPECTED_MODULE_NAME;
    // Firmware version of the bulb
    @Expose
    public String fwVersion = LAST_KNOWN_FIRMWARE_VERSION;
    // The ID of group the bulb is assigned to
    // I don't know how to group bulbs, all of mine return 0
    @Expose
    public int groupId;
    // Not sure what the numbers mean
    // For a full color A19 I get [33,1]
    // For a full coloer BR30 I get [37,1]
    @Expose
    public int[] drvConf = {};
}
