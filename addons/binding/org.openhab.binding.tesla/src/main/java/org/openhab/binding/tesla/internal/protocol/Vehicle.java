/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
