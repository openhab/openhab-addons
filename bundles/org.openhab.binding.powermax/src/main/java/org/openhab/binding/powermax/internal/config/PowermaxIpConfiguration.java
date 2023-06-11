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
package org.openhab.binding.powermax.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PowermaxIpConfiguration} is responsible for holding
 * configuration informations associated to a Powermax IP thing type
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxIpConfiguration {

    public String ip = "";
    public int tcpPort = 0;
    public int motionOffDelay = 3;
    public boolean allowArming = false;
    public boolean allowDisarming = false;
    public String pinCode = "";
    public boolean forceStandardMode = false;
    public String panelType = "PowerMaxPro";
    public boolean autoSyncTime = false;
}
