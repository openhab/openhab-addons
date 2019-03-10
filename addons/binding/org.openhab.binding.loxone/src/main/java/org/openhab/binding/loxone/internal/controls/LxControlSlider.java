/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.loxone.internal.LxServerHandler;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A slider type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a slider control is:
 * <ul>
 * <li>a virtual input of slider type
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSlider extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlSlider(uuid);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to slider controls
     */
    private static final String TYPE_NAME = "slider";
    /**
     * States
     */
    private static final String STATE_VALUE = "value";
    private static final String STATE_ERROR = "error";

    /**
     * The only command accepted by slider is a number with its value
     */

    private Double minValue = 0.0;
    private Double maxValue = 100.0;
    private Double stepValue = 1.0;
    private String format;
    private Double currentValue;

    LxControlSlider(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxServerHandler thingHandler, LxContainer room, LxCategory category) {
        super.initialize(thingHandler, room, category);
        ChannelUID cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel, "Slider", tags, this::handleCommands, this::getChannelState);
        if (details != null) {
            format = details.format;
            if (details.min != null) {
                minValue = details.min;
            }
            if (details.max != null) {
                maxValue = details.max;
            }
            if (details.step != null) {
                stepValue = details.step;
            }
        }
        if (format != null) {
            addChannelStateDescription(cid, new StateDescription(new BigDecimal(minValue), new BigDecimal(maxValue),
                    new BigDecimal(stepValue), format, false, null));
        }
    }

    private void handleCommands(Command command) throws IOException {
        if (command instanceof DecimalType) {
            Double value = ((DecimalType) command).doubleValue();
            if (value > maxValue) {
                value = maxValue;
            } else if (value < minValue) {
                value = minValue;
            }
            sendAction(value.toString());
        }
    }

    private State getChannelState() {
        if (currentValue != null) {
            return new DecimalType(currentValue);
        }
        return UnDefType.UNDEF;
    };

    @Override
    public void onStateChange(LxControlState state) {
        String stateName = state.getName();
        Object stateValue = state.getStateValue();
        if (stateValue instanceof Double) {
            Double value = (Double) stateValue;
            if (STATE_VALUE.equals(stateName)) {
                currentValue = value;
            } else if (STATE_ERROR.equals(stateName) && (value != 0.0)) {
                // when this state update is received with non-zero value, the slider value becomes undefined until a
                // next valid value is received
                currentValue = null;
            }
            super.onStateChange(state);
        }
    }
}
