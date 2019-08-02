/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class StatusScheduleResponse extends LocalScheduleResponse {

    private Integer controllerId;

    private Integer customerId;

    private Integer userId;

    private Integer nextpoll;

    private List<Sensor> sensors = new LinkedList<Sensor>();

    private String message;

    private String obsRain;

    private String obsRainWeek;

    private String obsMaxtemp;

    private Integer obsRainUpgrade;

    private String obsRainText;

    private String obsCurrenttemp;

    private String wateringTime;

    private Integer waterSaving;

    private String lastContact;

    private List<Forecast> forecast = new LinkedList<Forecast>();

    private String status;

    private String statusIcon;

    public Integer getControllerId() {
        return controllerId;
    }

    public void setControllerId(Integer controllerId) {
        this.controllerId = controllerId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getNextpoll() {
        return nextpoll;
    }

    public void setNextpoll(Integer nextpoll) {
        this.nextpoll = nextpoll;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getObsRain() {
        return obsRain;
    }

    public void setObsRain(String obsRain) {
        this.obsRain = obsRain;
    }

    public String getObsRainWeek() {
        return obsRainWeek;
    }

    public void setObsRainWeek(String obsRainWeek) {
        this.obsRainWeek = obsRainWeek;
    }

    public String getObsMaxtemp() {
        return obsMaxtemp;
    }

    public void setObsMaxtemp(String obsMaxtemp) {
        this.obsMaxtemp = obsMaxtemp;
    }

    public Integer getObsRainUpgrade() {
        return obsRainUpgrade;
    }

    public void setObsRainUpgrade(Integer obsRainUpgrade) {
        this.obsRainUpgrade = obsRainUpgrade;
    }

    public String getObsRainText() {
        return obsRainText;
    }

    public void setObsRainText(String obsRainText) {
        this.obsRainText = obsRainText;
    }

    public String getObsCurrenttemp() {
        return obsCurrenttemp;
    }

    public void setObsCurrenttemp(String obsCurrenttemp) {
        this.obsCurrenttemp = obsCurrenttemp;
    }

    public String getWateringTime() {
        return wateringTime;
    }

    public void setWateringTime(String wateringTime) {
        this.wateringTime = wateringTime;
    }

    public Integer getWaterSaving() {
        return waterSaving;
    }

    public void setWaterSaving(Integer waterSaving) {
        this.waterSaving = waterSaving;
    }

    public String getLastContact() {
        return lastContact;
    }

    public void setLastContact(String lastContact) {
        this.lastContact = lastContact;
    }

    public List<Forecast> getForecast() {
        return forecast;
    }

    public void setForecast(List<Forecast> forecast) {
        this.forecast = forecast;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusIcon() {
        return statusIcon;
    }

    public void setStatusIcon(String statusIcon) {
        this.statusIcon = statusIcon;
    }
}
