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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

/**
 * Tapo-DynamicLightEffects Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightDynamicFx {
    private static final String FX_JSON_FILE = "/lightningfx/dynamic_light_fx.json";

    @Expose
    private boolean enable = false;

    @Expose
    private String id = JSON_KEY_LIGHTNING_EFFECT_OFF;

    @Expose(serialize = false, deserialize = true)
    private String name = "";

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    public void setEffect(String id) throws TapoErrorHandler {
        setEffect(getEffect(id));
    }

    public void setEffect(TapoLightDynamicFx effect) {
        enable = effect.enable;
        id = effect.id;
        name = effect.name;
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
        if (!isEnabled()) {
            return JSON_KEY_LIGHTNING_EFFECT_OFF;
        }
        return id;
    }

    public String getName() {
        if (!isEnabled()) {
            return JSON_KEY_LIGHTNING_EFFECT_OFF;
        }
        return name;
    }

    public boolean hasEffect(String id) {
        try {
            return getEffect(id) instanceof TapoLightDynamicFx;
        } catch (Exception e) {
            return false;
        }
    }

    /***********************************
     *
     * PRIVATE HELPERS
     *
     ************************************/

    /**
     * Load Dynamic Light FX-Data from JSON
     * 
     * @param id id off effect
     */
    private TapoLightDynamicFx getEffect(String id) throws TapoErrorHandler {
        List<TapoLightDynamicFx> effects = getEffectList();
        for (TapoLightDynamicFx fx : effects) {
            if (fx.id.equals(id)) {
                return fx;
            }
        }
        throw new TapoErrorHandler(TapoErrorCode.ERR_BINDING_FX_NOT_FOUND);
    }

    /**
     * Load All Effects as List from JSON
     */
    private List<TapoLightDynamicFx> getEffectList() throws TapoErrorHandler {
        InputStream is = getClass().getResourceAsStream(FX_JSON_FILE);
        if (is != null) {
            try {
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                Type listType = new TypeToken<ArrayList<TapoLightDynamicFx>>() {
                }.getType();
                return GSON.fromJson(reader, listType);
            } catch (Exception e) {
                throw new TapoErrorHandler(TapoErrorCode.ERR_API_JSON_DECODE_FAIL);
            }
        } else {
            throw new TapoErrorHandler(TapoErrorCode.ERR_BINDING_FX_NOT_FOUND);
        }
    }
}
