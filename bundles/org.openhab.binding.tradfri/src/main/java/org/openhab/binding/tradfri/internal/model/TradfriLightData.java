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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tradfri.internal.TradfriColor;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriLightData} class is a Java wrapper for the raw JSON data about the light state.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Holger Reichert - Support for color bulbs
 * @author Christoph Weitkamp - Restructuring and refactoring of the binding
 */
@NonNullByDefault
public class TradfriLightData extends TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriLightData.class);

    public TradfriLightData() {
        super(LIGHT);
    }

    public TradfriLightData(JsonElement json) {
        super(LIGHT, json);
    }

    public TradfriLightData setBrightness(PercentType brightness) {
        attributes.add(DIMMER, new JsonPrimitive((int) Math.floor(brightness.doubleValue() * 2.54)));
        return this;
    }

    public @Nullable PercentType getBrightness() {
        PercentType result = null;

        JsonElement dimmer = attributes.get(DIMMER);
        if (dimmer != null) {
            result = TradfriColor.xyBrightnessToPercentType(dimmer.getAsInt());
        }

        return result;
    }

    public TradfriLightData setTransitionTime(int seconds) {
        attributes.add(TRANSITION_TIME, new JsonPrimitive(seconds));
        return this;
    }

    public int getTransitionTime() {
        JsonElement transitionTime = attributes.get(TRANSITION_TIME);
        if (transitionTime != null) {
            return transitionTime.getAsInt();
        } else {
            return 0;
        }
    }

    public TradfriLightData setColorTemperature(PercentType c) {
        TradfriColor color = new TradfriColor(c);
        int x = color.xyX;
        int y = color.xyY;
        logger.debug("New color temperature: {},{} ({} %)", x, y, c.intValue());
        attributes.add(COLOR_X, new JsonPrimitive(x));
        attributes.add(COLOR_Y, new JsonPrimitive(y));
        return this;
    }

    public @Nullable PercentType getColorTemperature() {
        JsonElement colorX = attributes.get(COLOR_X);
        JsonElement colorY = attributes.get(COLOR_Y);
        if (colorX != null && colorY != null) {
            TradfriColor color = new TradfriColor(colorX.getAsInt(), colorY.getAsInt(), null);
            return color.getColorTemperature();
        } else {
            return null;
        }
    }

    public TradfriLightData setColor(HSBType hsb) {
        TradfriColor color = new TradfriColor(hsb);
        attributes.add(COLOR_X, new JsonPrimitive(color.xyX));
        attributes.add(COLOR_Y, new JsonPrimitive(color.xyY));
        return this;
    }

    public @Nullable HSBType getColor() {
        // XY color coordinates plus brightness is needed for color calculation
        JsonElement colorX = attributes.get(COLOR_X);
        JsonElement colorY = attributes.get(COLOR_Y);
        JsonElement dimmer = attributes.get(DIMMER);
        if (colorX != null && colorY != null && dimmer != null) {
            int x = colorX.getAsInt();
            int y = colorY.getAsInt();
            int brightness = dimmer.getAsInt();
            TradfriColor color = new TradfriColor(x, y, brightness);
            return color.getHSB();
        }
        return null;
    }

    public TradfriLightData setOnOffState(boolean on) {
        attributes.add(ONOFF, new JsonPrimitive(on ? 1 : 0));
        return this;
    }

    public boolean getOnOffState() {
        JsonElement onOff = attributes.get(ONOFF);
        if (onOff != null) {
            return onOff.getAsInt() == 1;
        } else {
            return false;
        }
    }

    public String getJsonString() {
        return root.toString();
    }
}
