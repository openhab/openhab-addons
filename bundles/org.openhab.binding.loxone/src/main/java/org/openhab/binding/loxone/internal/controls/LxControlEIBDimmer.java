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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;

import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * An EIB dimmer type of control on Loxone Miniserver.
 * <p>
 * This control is absent in the API documentation. It looks like it behaves like a normal Dimmer, but it is missing the
 * information about min, max and step values.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlEIBDimmer extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlEIBDimmer(uuid);
        }

        @Override
        String getType() {
            return "eibdimmer";
        }
    }

    /**
     * States
     */
    private static final String STATE_POSITION = "position";
    private static final Double DEFAULT_MIN = 0.0;
    private static final Double DEFAULT_MAX = 100.0;
    private static final Double DEFAULT_STEP = 5.0;

    /**
     * Command string used to set the dimmer ON
     */
    private static final String CMD_ON = "On";
    /**
     * Command string used to set the dimmer to OFF
     */
    private static final String CMD_OFF = "Off";

    LxControlEIBDimmer(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        LxCategory category = getCategory();
        if (category != null && category.getType() == LxCategory.CategoryType.LIGHTS) {
            tags.addAll(LxTags.LIGHTING);
        }
        addChannel("Dimmer", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_DIMMER), defaultChannelLabel,
                "Dimmer", tags, this::handleCommands, this::getChannelState);
    }

    Double getMin() {
        return DEFAULT_MIN;
    }

    Double getMax() {
        return DEFAULT_MAX;
    }

    Double getStep() {
        return DEFAULT_STEP;
    }

    private void handleCommands(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                sendAction(CMD_ON);
            } else {
                sendAction(CMD_OFF);
            }
        } else if (command instanceof PercentType percentCommand) {
            setPosition(percentCommand.doubleValue());
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            Double value = getStateDoubleValue(STATE_POSITION);
            Double min = getMin();
            Double max = getMax();
            Double step = getStep();
            if (value != null && max != null && min != null && step != null && min >= 0 && max >= 0 && max > min) {
                if (increaseDecreaseCommand == IncreaseDecreaseType.INCREASE) {
                    value += step;
                    if (value > max) {
                        value = max;
                    }
                } else {
                    value -= step;
                    if (value < min) {
                        value = min;
                    }
                }
                sendAction(value.toString());
            }
        }
    }

    private PercentType getChannelState() {
        Double value = mapLoxoneToOH(getStateDoubleValue(STATE_POSITION));
        if (value != null && value >= 0 && value <= 100) {
            return new PercentType(value.intValue());
        }
        return null;
    }

    /**
     * Sets the current position of the dimmer
     *
     * @param position position to move to (0-100, 0 - full off, 100 - full on)
     * @throws IOException error communicating with the Miniserver
     */
    private void setPosition(Double position) throws IOException {
        Double loxonePosition = mapOHToLoxone(position);
        if (loxonePosition != null) {
            sendAction(loxonePosition.toString());
        }
    }

    private Double mapLoxoneToOH(Double loxoneValue) {
        if (loxoneValue != null) {
            // 0 means turn dimmer off, any value above zero should be mapped from min-max range
            if (Double.compare(loxoneValue, 0.0) == 0) {
                return 0.0;
            }
            Double max = getMax();
            Double min = getMin();
            if (max != null && min != null && max > min && min >= 0 && max >= 0) {
                return 100 * (loxoneValue - min) / (max - min);
            }
        }
        return null;
    }

    private Double mapOHToLoxone(Double ohValue) {
        if (ohValue != null) {
            // 0 means turn dimmer off, any value above zero should be mapped to min-max range
            if (Double.compare(ohValue, 0.0) == 0) {
                return 0.0;
            }
            Double max = getMax();
            Double min = getMin();
            if (max != null && min != null) {
                return min + ohValue * (max - min) / 100; // no rounding to integer value is needed as loxone is
                                                          // accepting floating point values
            }
        }
        return null;
    }
}
