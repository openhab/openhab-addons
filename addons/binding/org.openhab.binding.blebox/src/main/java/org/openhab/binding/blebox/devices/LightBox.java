/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

/**
 * The {@link LightBox} class defines a logic for LightBox device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class LightBox extends BaseDevice {

    public static final float MAX_CHANNEL_VALUE = 2.55f;
    public static final String SET_URL = "/api/rgbw/set";
    public static final String STATE_URL = "/api/rgbw/state";

    public HSBType LastKnownColor = null;
    public PercentType LastKnownBrigthness = null;

    public LightBox(String ipAddress) {
        super(BleboxBindingConstants.WLIGHTBOX, ipAddress);
    }

    public LightBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse extends BaseResponse {
        public static final String ROOT_ELEMENT = "rgbw";

        public String currentColor;
        public String desiredColor;

        public int effectID;

        @Override
        public String getRootElement() {
            return ROOT_ELEMENT;
        }

        public HSBType getColor() {
            return fromHexToHSB(currentColor);
        }

        public PercentType getWhiteBrightness() {
            return fromHexToPercentType(currentColor.substring(6, 8));
        }
    }

    public class SetRequest extends BaseRequest {
        public static final String ROOT_ELEMENT = "rgbw";

        public String desiredColor;

        @Override
        public String getRootElement() {
            return ROOT_ELEMENT;
        }
    }

    public StateResponse getStatus() {
        StateResponse response = getJson(STATE_URL, StateResponse.class, StateResponse.ROOT_ELEMENT);

        return response;
    }

    public void setColor(HSBType hsb) {
        if (LastKnownBrigthness == null) {
            LastKnownBrigthness = PercentType.ZERO;
        }

        SetRequest request = new SetRequest();
        request.desiredColor = fromHSBToHex(hsb) + percentToHex(LastKnownBrigthness);

        StateResponse response = postJson(request, SET_URL, StateResponse.class, StateResponse.ROOT_ELEMENT);

        LastKnownColor = hsb;
    }

    public void setWhiteBrightness(PercentType p) {
        if (LastKnownColor == null) {
            LastKnownColor = HSBType.BLACK;
        }

        SetRequest request = new SetRequest();
        request.desiredColor = fromHSBToHex(LastKnownColor) + percentToHex(p);

        StateResponse response = postJson(request, SET_URL, StateResponse.class, StateResponse.ROOT_ELEMENT);

        LastKnownBrigthness = p;
    }

    public void setWhiteBrightness(OnOffType p) {
        if (p == OnOffType.ON) {
            setWhiteBrightness(PercentType.HUNDRED);
        } else {
            setWhiteBrightness(PercentType.ZERO);
        }
    }

    public static String fromHSBToHex(HSBType hsb) {
        int r = Math.round(hsb.getRed().intValue() * MAX_CHANNEL_VALUE);
        int g = Math.round(hsb.getGreen().intValue() * MAX_CHANNEL_VALUE);
        int b = Math.round(hsb.getBlue().intValue() * MAX_CHANNEL_VALUE);

        String toRet = String.format("%02X", r) + String.format("%02X", g) + String.format("%02X", b);

        return toRet;
    }

    public static HSBType fromHexToHSB(String color) {
        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);
        int w = Integer.parseInt(color.substring(4, 6), 16);

        return HSBType.fromRGB(r, g, b);
    }

    public static String percentToHex(PercentType percent) {
        int p = (int) (percent.doubleValue() * MAX_CHANNEL_VALUE);

        return String.format("%02X", p);
    }

    public static PercentType fromHexToPercentType(String hex) {
        int w = Integer.parseInt(hex, 16);

        return new PercentType(Math.round(w / MAX_CHANNEL_VALUE));
    }

}
