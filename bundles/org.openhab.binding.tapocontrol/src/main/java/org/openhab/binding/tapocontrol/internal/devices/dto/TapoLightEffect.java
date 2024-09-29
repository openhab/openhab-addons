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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-LightEffects Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightEffect {
    @Expose
    private String id = "";

    @Expose
    private String name = "";

    @Expose
    private Integer brightness = 0;

    @Expose
    @Nullable
    @SerializedName("display_colors")
    private List<int[]> displayColors = List.of();

    @Expose
    private Integer enable = 0;

    @Expose
    @Nullable
    private Integer bAdjusted = 0;

    @Expose
    @Nullable
    @SerializedName("brightness_range")
    private Integer[] brightnessRange = {};

    @Expose
    @Nullable
    private List<int[]> backgrounds = List.of();

    @Expose
    @Nullable
    private Integer custom = 0;

    @Expose
    @Nullable
    private Integer direction = 0;

    @Expose
    @Nullable
    private Integer duration = 0;

    @Expose
    @Nullable
    @SerializedName("expansion_strategy")
    private Integer expansionStrategy = 0;

    @Expose
    @Nullable
    private Integer fadeoff = 0;

    @Expose
    @Nullable
    @SerializedName("hue_range")
    private Integer[] hueRange = {};

    @SerializedName("init_states")
    @Expose
    @Nullable
    private List<Integer[]> initStates = List.of();
    @Expose
    @Nullable
    @SerializedName("random_seed")
    private Integer randomSeed = 0;

    @Expose
    @Nullable
    @SerializedName("repeat_times")
    private Integer repeatTimes = 0;

    @Expose
    @Nullable
    @SerializedName("saturation_range")
    private Integer[] saturationRange = {};

    @Expose
    @Nullable
    @SerializedName("segment_length")
    private Integer segmentLength = 0;

    @Expose
    @Nullable
    private Integer[] segments = {};

    @Expose
    @Nullable
    private List<int[]> sequence = List.of();

    @Expose
    @Nullable
    private Integer spread = 0;

    @Expose
    @Nullable
    private Integer transition = 0;

    @Expose
    @Nullable
    @SerializedName("transition_range")
    private Integer[] transitionRange = {};

    @Expose
    @Nullable
    private String type = "";

    @Expose
    @Nullable
    @SerializedName("trans_sequence")
    private List<int[]> transSequence = List.of();

    @Expose
    @Nullable
    @SerializedName("run_time")
    private Integer runTime = 0;

    /**
     * Init class with effect id
     */
    public TapoLightEffect(boolean enable, String fxId) {
        setEnable(enable);
        id = fxId;
    }

    public TapoLightEffect(String fxId) {
        setEnable((fxId.length() > 0 && !fxId.equals(JSON_KEY_LIGHTNING_EFFECT_OFF)));
        id = fxId;
    }

    public TapoLightEffect() {
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    public void setEnable(boolean enable) {
        this.enable = (enable) ? 1 : 0;
    }

    public void setBrightness(Integer value) {
        brightness = value;
    }

    /**
     * set light fx from fx-name
     * loads fx data from resources/lightningfx/[fxname].json
     * 
     * @param fxName name of effect
     * @return
     */
    public TapoLightEffect setEffect(String fxName) throws TapoErrorHandler {
        if (JSON_KEY_LIGHTNING_EFFECT_OFF.equals(fxName)) {
            enable = 0;
            return this;
        } else {
            InputStream is = getClass().getResourceAsStream("/lightningfx/" + fxName + ".json");
            if (is != null) {
                try {
                    Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    return GSON.fromJson(reader, TapoLightEffect.class);
                } catch (Exception e) {
                    throw new TapoErrorHandler(TapoErrorCode.ERR_API_JSON_DECODE_FAIL, fxName);
                }
            } else {
                throw new TapoErrorHandler(TapoErrorCode.ERR_BINDING_FX_NOT_FOUND, fxName);
            }
        }
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public boolean isEnabled() {
        return enable == 1;
    }

    public String getName() {
        if (!isEnabled()) {
            return JSON_KEY_LIGHTNING_EFFECT_OFF;
        }
        return name;
    }

    public Integer getBrightness() {
        return brightness;
    }
}
