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

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Forecast {

    private String tempHi;

    private String tempLo;

    private String conditions;

    private String day;

    private Integer pop;

    private Integer humidity;

    private String wind;

    private String icon;

    private String iconLocal;

    /**
     * @return
     */
    public String getTempHi() {
        return tempHi;
    }

    /**
     * @param tempHi
     */
    public void setTempHi(String tempHi) {
        this.tempHi = tempHi;
    }

    /**
     * @return
     */
    public String getTempLo() {
        return tempLo;
    }

    /**
     * @param tempLo
     */
    public void setTempLo(String tempLo) {
        this.tempLo = tempLo;
    }

    /**
     * @return
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * @param conditions
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * @return
     */
    public String getDay() {
        return day;
    }

    /**
     * @param day
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @return
     */
    public Integer getPop() {
        return pop;
    }

    /**
     * @param pop
     */
    public void setPop(Integer pop) {
        this.pop = pop;
    }

    /**
     * @return
     */
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * @param humidity
     */
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * @return
     */
    public String getWind() {
        return wind;
    }

    /**
     * @param wind
     */
    public void setWind(String wind) {
        this.wind = wind;
    }

    /**
     * @return
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return
     */
    public String getIconLocal() {
        return iconLocal;
    }

    /**
     * @param iconLocal
     */
    public void setIconLocal(String iconLocal) {
        this.iconLocal = iconLocal;
    }

}