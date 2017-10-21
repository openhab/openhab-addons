/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GateBox} class defines a logic for GateBox device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class GateBox extends BaseDevice {
    public static final float MAX_CHANNEL_VALUE = 2.55f;
    public static final String SET_URL = "/s/p";
    public static final String STATE_URL = "/api/gate/state";
    private Logger logger = LoggerFactory.getLogger(GateBox.class);

    public GateBox(String ipAddress) {
        super(BleboxBindingConstants.GATEBOX, ipAddress);
    }

    public GateBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse implements BaseResponse {
        public String currentPos;
        public String desiredPos;

        @Override
        public String getRootElement() {
            return null;
        }

        public PercentType getPosition() {
            return PercentType.valueOf(currentPos);
        }
    }

    public class SetRequest implements BaseRequest {
        public String desiredPos;

        @Override
        public String getRootElement() {
            return "gate";
        }
    }

    public StateResponse getStatus() {
        StateResponse response = null;
        try {
            response = getJson(STATE_URL, StateResponse.class, null);
        } catch (Exception e) {
            logger.error("getStatus(): Error: {}", e);
        }

        return response;
    }

    public void setPosition(PercentType p) {
        try {
            getJson(SET_URL, StateResponse.class, "gate");
        } catch (Exception e) {
            logger.error("setPosition(): Error: {}", e);
        }
    }

    public void setState(OnOffType p) {
        if (p == OnOffType.ON) {
            setPosition(PercentType.HUNDRED);
        } else {
            setPosition(PercentType.ZERO);
        }
    }

}
