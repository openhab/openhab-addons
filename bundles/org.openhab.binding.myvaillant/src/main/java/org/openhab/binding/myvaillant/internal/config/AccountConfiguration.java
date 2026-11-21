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
package org.openhab.binding.myvaillant.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of the account bridge. The update settings apply to all systems of the account.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    public String username = "";
    public String password = "";
    public String brand = "vaillant";
    public String country = "germany";

    /** Seconds between updates of the systems */
    public int refreshInterval = 300;
    /** Minutes between updates of the energy values, 0 disables them */
    public int energyRefreshInterval = 60;
    public boolean fetchRts = false;
    public boolean fetchMpc = false;
    public boolean fetchConnectionStatus = false;
    public boolean fetchTroubleCodes = false;

    /** Seconds to wait after a command before updating, the API takes a while to return the new values */
    public int refreshDelay = 10;
    /** Hours */
    public double quickVetoDuration = 3;
    /** Days */
    public int holidayDuration = 365;
    /** Days */
    public int manualCoolingDuration = 30;
}
