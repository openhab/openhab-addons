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

package org.openhab.binding.touchwand.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TouchWandThermostatCurrentStatus} implements Thermostat unit
 * CurrentStatus data property.
 *
 * @author Roie Geron - Initial contribution
 */

@NonNullByDefault
public class TouchWandThermostatCurrentStatus {

    private String ac_type = "";
    private int value;
    private int thermo_mode;
    private String mode = "";
    private int temp;
    private int thermo_temp;
    private String state = "";
    private int roomTemp;
    private String fan = "";
    private String communication_status = "";

    public String getAcType() {
        return this.ac_type;
    }

    public void setAcType(String ac_type) {
        this.ac_type = ac_type;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getThermoMode() {
        return this.thermo_mode;
    }

    public void setThermoMode(int thermo_mode) {
        this.thermo_mode = thermo_mode;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getTemp() {
        return this.temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTargetTemperature() {
        return this.thermo_temp;
    }

    public void setTargetTemperature(int thermo_temp) {
        this.thermo_temp = thermo_temp;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getRoomTemperature() {
        return this.roomTemp;
    }

    public void setRoomTemperature(int roomTemp) {
        this.roomTemp = roomTemp;
    }

    public String getFanLevel() {
        return this.fan;
    }

    public void setFanLevel(String fan) {
        this.fan = fan;
    }

    public String getCommunicationStatus() {
        return this.communication_status;
    }

    public void setCommunicationStatus(String communication_status) {
        this.communication_status = communication_status;
    }
}
