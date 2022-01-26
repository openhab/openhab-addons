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
package org.openhab.binding.infokeydinrail.internal;

import org.openhab.binding.infokeydinrail.internal.handler.InfokeyDhtHandler;

/**
 * The {@link InfokeyDhtHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * This GPIO provider implements the DHT 11 / 22 / AM2302 as native device.
 * </p>
 *
 * <p>
 * The DHT 11 / 22 / AM2302 is connected via Custom Rpi Python Server and get results
 * </p>
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
public class DhtResponse {

    private String title;
    private Integer pin;
    private Float temperatureC;
    private Float temperatureF;
    private Float humidity;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public Float getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Float temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Float getTemperatureF() {
        return temperatureF;
    }

    public void setTemperatureF(Float temperatureF) {
        this.temperatureF = temperatureF;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }
}
