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
 * {@link JuiceNetApiDeviceStatus } implements DTO for Device Status
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiDeviceStatus {
    public String ID = "";
    public Long info_timestamp = (long) 0;
    public boolean show_override;
    public String state = "";
    public JuiceNetApiDeviceChargingStatus charging = new JuiceNetApiDeviceChargingStatus();
    public JuiceNetApiDeviceLifetimeStatus lifetime = new JuiceNetApiDeviceLifetimeStatus();
    public int charging_time_left;
    public Long plug_unplug_time = (long) 0;
    public Long target_time = (long) 0;
    public Long unit_time = (long) 0;
    public Long utc_time = (long) 0;
    public long default_target_time = 0;
    public int car_id;
    public int temperature;
}
