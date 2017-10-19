/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

/**
 * The {@link LightBoxS} class defines a logic for LightBoxS device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class LightBoxS extends BaseDevice {

    public static final float MAX_CHANNEL_VALUE = 2.55f;

    public static final String SET_URL = "/api/light/set";
    public static final String STATE_URL = "/api/light/state";

    public PercentType LastKnownBrigthness = null;

    public LightBoxS(String ipAddress) {
        super(BleboxBindingConstants.WLIGHTBOXS, ipAddress);
    }

    public class StateResponse extends BaseResponse {
        public static final String ROOT_ELEMENT = "light";

        public String currentColor;
        public String desiredColor;

        @Override
        public String getRootElement() {
            return ROOT_ELEMENT;
        }

        public PercentType getWhiteBrightness() {
            return fromHexToPercentType(currentColor);
        }
    }

    public class SetRequest extends BaseRequest {
        public static final String ROOT_ELEMENT = "light";

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

    public void setWhiteBrightness(PercentType p) {
        SetRequest request = new SetRequest();
        request.desiredColor = percentToHex(p);

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

    public static String percentToHex(PercentType percent) {
        int p = (int) (percent.doubleValue() * MAX_CHANNEL_VALUE);

        return String.format("%02X", p);
    }

    public static PercentType fromHexToPercentType(String hex) {
        int w = Integer.parseInt(hex, 16);

        return new PercentType(Math.round(w / MAX_CHANNEL_VALUE));
    }

}
