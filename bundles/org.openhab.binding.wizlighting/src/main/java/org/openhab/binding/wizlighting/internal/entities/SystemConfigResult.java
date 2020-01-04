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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This POJO represents the "result" of one request for a bulb's system
 * configuration I assume the same packet could be used as the param of a
 * 'setSystemConfig' request, but I'm not willing to risk ruining my bulbs by
 * trying it.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class SystemConfigResult extends FirstBeatResponseParam {
    // The ID of room the bulb is assigned to
    public int roomId;
    // Not sure what the home lock is
    public boolean homeLock;
    // Also not sure about the pairing lock
    public boolean pairingLock;
    // Obviously a type ID
    // The value is 0 for both BR30 and A19 full color bulbs
    public int typeId;
    // The module name
    // The value is "ESP01_SHRGB1C_31" for both BR30 and A19 full color bulbs
    public String moduleName = "ESP01_SHRGB1C_31";
    // The ID of group the bulb is assigned to
    // I don't know how to group bulbs, all of mine return 0
    public int groupId;
    // Not sure what the numbers mean
    // For a full color A19 I get [33,1]
    // For a full coloer BR30 I get [37,1]
    public int drvConf[] = {};
}
