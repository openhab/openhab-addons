/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

/**
 * The {@link Dimmer} class defines a logic for Dimmer device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class Dimmer extends BaseDevice {

    public static final float MAX_BRIGHTNESS = 255;

    public static final String SET_URL = "/api/dimmer/set";
    public static final String STATE_URL = "/api/dimmer/state";

    public Dimmer(String ipAddress) {
        super(BleboxBindingConstants.DIMMERBOX, ipAddress);
    }

    public class SetRequest extends BaseRequest {
        public static final String ROOT_ELEMENT = "dimmer";

        public Integer loadType;
        public Integer desiredBrightness;
        public Boolean overloaded;
        public Boolean overheated;

        public SetRequest setBrightness(Integer value) {
            desiredBrightness = value;

            return this;
        }

        @Override
        public String getRootElement() {
            return ROOT_ELEMENT;
        }
    }

    public class StateResponse extends BaseResponse {
        public static final String ROOT_ELEMENT = "dimmer";

        public Integer loadType;
        public Integer currentBrightness;
        public Integer desiredBrightness;
        public Integer temperature;
        public Boolean overloaded;
        public Boolean overheated;

        @Override
        public String getRootElement() {
            return ROOT_ELEMENT;
        }
    }

    public void setBrightness(Integer value) {
        SetRequest request = new SetRequest().setBrightness(value);

        StateResponse response = postJson(request, SET_URL, StateResponse.class, StateResponse.ROOT_ELEMENT);
    }

    public void setBrightness(PercentType percent) {
        int value = Math.round((255 * percent.floatValue()) / 100);

        setBrightness(value);
    }

    public PercentType getBrightness() {
        StateResponse response = getJson(STATE_URL, StateResponse.class, StateResponse.ROOT_ELEMENT);

        if (response != null) {
            float percent = response.currentBrightness / MAX_BRIGHTNESS;

            return new PercentType(Math.round(percent * 100));
        } else {
            return null;
        }
    }
}
