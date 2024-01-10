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
package org.openhab.binding.elroconnects.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ElroConnectsConnector} class represents a device response received from the ELRO Connects
 * cloud.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsConnector {

    String devTid = "";
    public String ctrlKey = "";
    public String binVersion = "";
    public String binType = "";
    public String sdkVer = "";
    public String model = "";
    public boolean online;
    public String desc = "";

    public String getDevTid() {
        return devTid;
    }
}
