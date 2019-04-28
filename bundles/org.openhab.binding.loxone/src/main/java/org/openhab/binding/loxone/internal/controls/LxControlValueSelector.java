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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.types.LxState;
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
class LxControlValueSelector extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlValueSelector(uuid);
        }

        @Override
        String getType() {
            return "valueselector";
        }
    }

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
    private ChannelUID numberChannelId;

    private LxControlValueSelector(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        channelId = addChannel("Dimmer", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_DIMMER),
                defaultChannelLabel, "Value Selector", tags, this::handleCommand, this::getChannelState);
        numberChannelId = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + " / Number", "Value Selector by number", tags, this::handleNumberCommand,
                this::getChannelNumberState);

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
        if (minValue == null || maxValue == null || minValue >= maxValue) {
            logger.debug("Value selector min or max value missing or min>max.");
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

    private void handleNumberCommand(Command command) throws IOException {
        if (minValue == null || maxValue == null || minValue >= maxValue) {
            logger.debug("Value selector min or max value missing or min>max.");
            return;
        }
        if (command instanceof DecimalType) {
            Double value = ((DecimalType) command).doubleValue();
            if (value < minValue || value > maxValue) {
                logger.debug("Value {} out of {}-{} range", value, minValue, maxValue);
                return;
            }
            sendAction(value.toString());
        }
    }

    private PercentType getChannelState() {
        Double value = getStateDoubleValue(STATE_VALUE);
        if (value != null && minValue != null && maxValue != null && minValue <= value && value <= maxValue) {
            value = ((value - minValue) * 100.0) / (maxValue - minValue);
            return new PercentType(value.intValue());
        }
        return null;
    }

    private DecimalType getChannelNumberState() {
        Double value = getStateDoubleValue(STATE_VALUE);
        if (value != null && minValue != null && maxValue != null && minValue <= value && value <= maxValue) {
            return new DecimalType(value);
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
    public void onStateChange(LxState state) {
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
        if (minValue != null && maxValue != null && stepValue != null && minValue < maxValue) {
            StateDescription description = new StateDescription(new BigDecimal(minValue), new BigDecimal(maxValue),
                    new BigDecimal(stepValue), format, false, null);
            addChannelStateDescription(channelId, description);
            addChannelStateDescription(numberChannelId, description);
        }
    }
}
