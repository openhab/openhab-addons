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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link Vehicle} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class Vehicle {

    public String color;
    public String display_name;
    public String id;
    public String option_codes;
    public String vehicle_id;
    public String vin;
    public String tokens[];
    public String state;
    public boolean remote_start_enabled;
    public boolean calendar_enabled;
    public boolean notifications_enabled;
    public String backseat_token;
    public String backseat_token_updated_at;

    Vehicle() {
    }
}
