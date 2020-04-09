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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link Powerwall} is a datastructure to capture
 * variables sent by the Tesla Powerwall
 *
 * @author Paul Smedley - Initial contribution
 */
public class Powerwall {

    public String energy_site_id;
    public String resource_type;
    public String site_name;
    public String id;
    public String gateway_id;
    public String energy_left;
    public String total_pack_energy;
    public String percentage_charged;
    public String battery_type;
    public boolean backup_capable;
    public String battery_power;
    public boolean sync_grid_alert_enabled;
    public boolean breaker_alert_enabled;

    Powerwall() {
    }
}
