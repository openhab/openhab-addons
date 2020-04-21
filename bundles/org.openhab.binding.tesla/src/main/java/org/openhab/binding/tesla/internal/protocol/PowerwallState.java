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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link PowerwallState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class PowerwallState {

    public String site_name;
    public float energy_left;
    public float total_pack_energy;
    public String grid_status;
    public JsonObject backup;
    public JsonObject user_settings;
    public JsonObject components;
    public String default_real_mode;
    public String operation;
    public JsonArray power_reading;

    PowerwallState() {
    }

}
