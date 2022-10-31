/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.juicenet.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link JuiceNetDeviceChargingStatus } implements DTO for device charging status
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiDeviceChargingStatus {
    public int amps_limit;
    public float amps_current;
    public int voltage;
    public int wh_energy;
    public int savings;
    public int watt_power;
    public int seconds_charging;
    public int wh_energy_at_plugin;
    public int wh_energy_to_add;
    public int flags;
}
