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

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.LxServerHandler;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ValueSelector type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers: Push-button +/- and Push-button + functional blocks.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlValueSelector extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlValueSelector(uuid);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to value selector controls
     */
    private static final String TYPE_NAME = "valueselector";

    private static final String STATE_VALUE = "value";
    private static final String STATE_MIN = "min";
    private static final String STATE_MAX = "max";
    private static final String STATE_STEP = "step";

    private final Logger logger = LoggerFactory.getLogger(LxControlValueSelector.class);

    private Boolean increaseOnly = false;
    private String format = "%.1f";
    private Double minValue;
    private Double maxValue;
    private Double stepValue;
    private ChannelUID channelId;

    LxControlValueSelector(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxServerHandler thingHandler, LxContainer room, LxCategory category) {
        super.initialize(thingHandler, room, category);
        channelId = addChannel("Dimmer", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_DIMMER),
                defaultChannelLabel, "Value Selector", tags, this::handleCommand, this::getChannelState);
        if (details != null) {
            if (details.format != null) {
                this.format = details.format;
            }
            if (details.increaseOnly != null) {
                this.increaseOnly = details.increaseOnly;
            }
        }
    }

    private void handleCommand(Command command) throws IOException {
        if (minValue == null || maxValue == null) {
            logger.debug("Value selector min or max value missing.");
            return;
        }
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                sendAction(maxValue.toString());
            } else {
                sendAction(minValue.toString());
            }
        } else if (command instanceof IncreaseDecreaseType) {
            if (stepValue == null) {
                logger.debug("Value selector step value missing.");
                return;
            }
            IncreaseDecreaseType type = (IncreaseDecreaseType) command;
            if (increaseOnly != null && type == IncreaseDecreaseType.DECREASE && increaseOnly) {
                logger.debug("Value selector configured to allow increase only.");
                return;
            }
            Double currentValue = getStateDoubleValue(STATE_VALUE);
            if (currentValue != null) {
                Double nextValue = currentValue + (type == IncreaseDecreaseType.INCREASE ? stepValue : -stepValue);
                if (nextValue > maxValue) {
                    nextValue = maxValue;
                }
                if (nextValue < minValue) {
                    nextValue = minValue;
                }
                sendAction(nextValue.toString());
            }
        } else if (command instanceof PercentType) {
            Double value = ((PercentType) command).doubleValue() * (maxValue - minValue) / 100.0 + minValue;
            sendAction(value.toString());
        }
    }

    private PercentType getChannelState() {
        Double value = getStateDoubleValue(STATE_VALUE);
        if (value != null) {
            value = (value - minValue) * 100.0 / (maxValue - minValue);
            return new PercentType(value.intValue());
        }
        return null;
    }

    /**
     * Get dynamic updates to the minimum, maximum and step values. Sets a new state description if these values are
     * available. If no updates to the min/max/step, calls parent class method to update selector value in the
     * framework.
     *
     * @param state state update from the Miniserver
     */
    @Override
    public void onStateChange(LxControlState state) {
        String stateName = state.getName();
        Object value = state.getStateValue();
        try {
            if (value instanceof Double) {
                if (STATE_MIN.equals(stateName)) {
                    minValue = (Double) value;
                } else if (STATE_MAX.equals(stateName)) {
                    maxValue = (Double) value;
                } else if (STATE_STEP.equals(stateName)) {
                    stepValue = (Double) value;
                    if (stepValue <= 0) {
                        logger.warn("Value selector step value <= 0: {}", stepValue);
                        stepValue = null;
                    }
                } else {
                    super.onStateChange(state);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("Error parsing value for state {}: {}", stateName, e.getMessage());
        }
        if (minValue != null && maxValue != null) {
            if (minValue >= maxValue) {
                logger.warn("Value selector min value >= max value: {}, {}", minValue, maxValue);
                minValue = null;
                maxValue = null;
            } else {
                addChannelStateDescription(channelId, new StateDescription(new BigDecimal(minValue),
                        new BigDecimal(maxValue),
                        stepValue != null ? new BigDecimal(stepValue) : new BigDecimal((maxValue - minValue) / 10.0),
                        format, false, null));
            }
        }
    }
}
