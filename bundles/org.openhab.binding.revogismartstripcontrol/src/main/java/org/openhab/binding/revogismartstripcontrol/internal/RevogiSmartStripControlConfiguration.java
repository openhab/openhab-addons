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
package org.openhab.binding.revogismartstripcontrol.internal;

/**
 * The {@link RevogiSmartStripControlConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andi Bräu - Initial contribution
 */
public class RevogiSmartStripControlConfiguration {

    public  String serialNumber;

    public int pollInterval;

    public String ipAddress;

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
