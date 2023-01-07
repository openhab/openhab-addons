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
package org.openhab.binding.tapocontrol.internal.structures;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.awt.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Tapo-LightningEffect Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightEffect {
    private Integer enable = 0;
    private String id = "";
    private String name = "";
    private Integer custom = 0;
    private Integer brightness = 0;
    private Integer[] colorTempRange = { 9000, 9000 }; // :[9000,9000]
    private Color displayColors[] = { Color.WHITE };

    private JsonObject jsonObject = new JsonObject();

    /**
     * INIT
     */
    public TapoLightEffect() {
        setData();
    }

    /**
     * Init DeviceInfo with new Data;
     * 
     * @param jso JsonObject new Data
     */
    public TapoLightEffect(JsonObject jso) {
        setData(jso);
    }

    /**
     * Set Data (new JsonObject)
     * 
     * @param jso JsonObject new Data
     */
    public TapoLightEffect setData(JsonObject jso) {
        /* create empty jsonObject to set efault values if has no lighning effect */
        if (jso.has(DEVICE_PROPERTY_EFFECT)) {
            this.jsonObject = jso;
        } else {
            jsonObject = new JsonObject();
        }
        setData();
        return this;
    }

    private void setData() {
        this.enable = jsonObjectToInt(jsonObject, PROPERTY_LIGHTNING_EFFECT_ENABLE);
        this.id = jsonObjectToString(jsonObject, PROPERTY_LIGHTNING_EFFECT_ID);
        this.name = jsonObjectToString(jsonObject, PROPERTY_LIGHTNING_EFFECT_NAME);
        this.custom = jsonObjectToInt(jsonObject, PROPERTY_LIGHTNING_EFFECT_CUSTOM); // jsonObjectToBool
        this.brightness = jsonObjectToInt(jsonObject, PROPERTY_LIGHTNING_EFFECT_BRIGHNTESS);
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    public void setEnable(Boolean enable) {
        this.enable = enable ? 1 : 0;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setCustom(Boolean enable) {
        this.custom = enable ? 1 : 0;
    }

    public void setBrightness(Integer value) {
        this.brightness = value;
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public Integer getEnable() {
        return this.enable;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Integer getCustom() {
        return this.custom;
    }

    public Integer getBrightness() {
        return this.brightness;
    }

    public Integer[] getColorTempRange() {
        return this.colorTempRange;
    }

    public Color[] getDisplayColors() {
        return this.displayColors;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
