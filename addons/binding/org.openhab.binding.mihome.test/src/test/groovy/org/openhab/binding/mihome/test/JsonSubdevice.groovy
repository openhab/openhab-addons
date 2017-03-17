/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test;

/**
 * JSON representation of a subdevice used in the communication with the server.
 *
 * @author Svilen Valkanov
 */
public class JsonSubdevice extends JsonDevice {

    public static final String DEFAULT_LABEL = "New Subdevice"
    // ID of the gateway that the subdevice is paired to
    int device_id
    //For open and motion sensors
    Integer sensor_state

    //Only for energy monitor
    double voltage
    int real_power
    int today_wh

    JsonSubdevice(int id,int gatewayID,String type) {
        super(type, id, DEFAULT_LABEL);
        this.device_id = gatewayID
    }

    JsonSubdevice(int id,int gatewayID,String type,Integer state) {
        super(type, id, DEFAULT_LABEL)
        this.device_id = gatewayID
        this.sensor_state = state
    }

    JsonSubdevice(int id,int gatewayID,String type,double voltage,int power,int wh) {
        super(type, id, DEFAULT_LABEL)
        this.device_id = gatewayID
        this.voltage = voltage
        this.real_power = power
        this.today_wh = wh
    }
}
