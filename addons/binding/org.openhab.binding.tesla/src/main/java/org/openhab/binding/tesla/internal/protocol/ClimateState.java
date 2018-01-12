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
 * The {@link ClimateState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ClimateState {

    public float inside_temp;
    public float outside_temp;
    public float driver_temp_setting;
    public float passenger_temp_setting;
    public boolean is_auto_conditioning_on;
    public boolean is_front_defroster_on;
    public boolean is_rear_defroster_on;
    public int fan_status;
    public int seat_heater_left;
    public int seat_heater_right;
    public int seat_heater_rear_left;
    public int seat_heater_rear_right;
    public int seat_heater_rear_center;
    public int seat_heater_rear_right_back;
    public int seat_heater_rear_left_back;
    public boolean smart_preconditioning;

    ClimateState() {
    }
}
