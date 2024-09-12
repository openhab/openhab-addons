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
package org.openhab.binding.tapocontrol.internal.devices.wifi.bulb;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;

import java.util.EnumMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * List with Tapo-Bulb Information LastState
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoBulbLastStates {
    private EnumMap<TapoBulbModeEnum, @Nullable TapoBulbData> lastModeData = new EnumMap<>(TapoBulbModeEnum.class);

    /**
     * INIT
     */
    public TapoBulbLastStates() {
        lastModeData.put(TapoBulbModeEnum.UNKOWN, new TapoBulbData());
    }

    /**
     * Return LastModeData of Mode
     * 
     * @param mode mode to get
     * @return
     */
    public TapoBulbData get(TapoBulbModeEnum mode) {
        if (lastModeData.containsKey(mode)) {
            TapoBulbData lastmode = lastModeData.get(mode);
            if (lastmode != null) {
                return lastmode;
            }
        }
        return getDefaultData(mode);
    }

    /**
     * Set LastmodeData for Mode
     * 
     * @param mode mode to set
     * @param data actual state-data
     */
    public void put(TapoBulbModeEnum mode, TapoBulbData data) {
        lastModeData.put(mode, data);
    }

    /**
     * Get DefaultData
     * 
     * @param mode mode to get
     * @return
     */
    private TapoBulbData getDefaultData(TapoBulbModeEnum mode) {
        TapoBulbData defaultData = new TapoBulbData();
        defaultData.setBrightness(100);
        defaultData.setSaturation(100);
        defaultData.switchOnOff(true);
        defaultData.setDynamicLightEffectId("L1");

        if (TapoBulbModeEnum.WHITE_LIGHT.equals(mode)) {
            defaultData.setColorTemp(BULB_MAX_COLORTEMP - BULB_MIN_COLORTEMP);
        }
        return defaultData;
    }
}
