/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.structures;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Tapo-Energy-Monitor Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoEnergyData {
    private Number currentPower = 0;
    private Number todayEnergy = 0;
    private Number monthEnergy = 0;
    private Number todayRuntime = 0;
    private Number monthRuntime = 0;
    private Number[] past24h = new Number[24];
    private Number[] past30d = new Number[30];
    private Number[] past1y = new Number[12];

    private JsonObject jsonObject = new JsonObject();

    /**
     * INIT
     */
    public TapoEnergyData() {
        setData();
    }

    /**
     * Init DeviceInfo with new Data;
     * 
     * @param jso JsonObject new Data
     */
    public TapoEnergyData(JsonObject jso) {
        setData(jso);
    }

    /**
     * Set Data (new JsonObject)
     * 
     * @param jso JsonObject new Data
     */
    public TapoEnergyData setData(JsonObject jso) {
        /* create empty jsonObject to set efault values if has no energydata */
        if (jso.has(JSON_KEY_ENERGY_POWER)) {
            this.jsonObject = jso;
        } else {
            this.jsonObject = new JsonObject();
        }
        setData();
        return this;
    }

    private void setData() {
        this.currentPower = (float) jsonObjectToInt(jsonObject, JSON_KEY_ENERGY_POWER) / 1000;

        this.todayEnergy = jsonObjectToInt(jsonObject, JSON_KEY_ENERGY_ENERGY_TODAY);
        this.monthEnergy = jsonObjectToInt(jsonObject, JSON_KEY_ENERGY_ENERGY_MONTH);
        this.todayRuntime = jsonObjectToInt(jsonObject, JSON_KEY_ENERGY_RUNTIME_TODAY);
        this.monthRuntime = jsonObjectToInt(jsonObject, JSON_KEY_ENERGY_RUNTIME_MONTH);
        this.past24h = new Number[24];
        this.past30d = new Number[30];
        this.past1y = new Number[12];
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public Number getCurrentPower() {
        return currentPower;
    }

    public Number getTodayEnergy() {
        return todayEnergy;
    }

    public Number getMonthEnergy() {
        return monthEnergy;
    }

    public Number getYearEnergy() {
        int sum = 0;
        for (int i = 0; i < past1y.length; i++) {
            sum += past1y[i].intValue();
        }
        return sum;
    }

    public Number getTodayRuntime() {
        return todayRuntime;
    }

    public Number getMonthRuntime() {
        return monthRuntime;
    }

    public Number[] getPast24hUsage() {
        return past24h;
    }

    public Number[] getPast30dUsage() {
        return past30d;
    }

    public Number[] getPast1yUsage() {
        return past1y;
    }
}
