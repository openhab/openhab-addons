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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;

import java.awt.Color;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-LightEffects Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightEffectData {
    @Expose(serialize = true, deserialize = true)
    private boolean enable = false;

    @Expose(serialize = true, deserialize = true)
    private String id = "";

    @Expose(serialize = false, deserialize = true)
    private String name = "";

    @Expose(serialize = false, deserialize = true)
    private int custom = 0;

    @Expose(serialize = false, deserialize = true)
    private int brightness = 0;

    @SerializedName("display_colors")
    @Expose(serialize = false, deserialize = true)
    private List<Color> displayColors = List.of();

    /**
     * Init class with effect id
     */
    public TapoLightEffectData(boolean enable, String fxId) {
        this.enable = enable;
        id = fxId;
    }

    public TapoLightEffectData(String fxId) {
        enable = (fxId.length() > 0 && !fxId.equals(JSON_KEY_LIGHTNING_EFFECT_OFF));
        id = fxId;
    }

    public TapoLightEffectData() {
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setId(String value) {
        id = value;
    }

    public void setCustom(int value) {
        custom = value;
    }

    public void setBrightness(int value) {
        brightness = value;
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public boolean isEnabled() {
        return enable;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCustom() {
        return custom;
    }

    public int getBrightness() {
        return brightness;
    }

    public List<Color> getDisplayColors() {
        return displayColors;
    }
}
