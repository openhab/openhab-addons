/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.config;

import static org.openhab.binding.ihc.internal.IhcBindingConstants.*;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Class for holding channel parameterization.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ChannelParams {

    private String channelTypeId;
    private Integer resourceId;
    private String direction;
    private String commandToReact;
    private Integer pulseWidth;
    private Boolean inverted;
    private Long serialNumber;
    private Integer shortPressMaxTime;
    private Integer longPressMaxTime;

    public ChannelParams(Channel channel) throws IllegalArgumentException {
        if (channel == null) {
            throw new IllegalArgumentException("Channel can't be null");
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID != null) {
            channelTypeId = channelTypeUID.getId();
        }

        Map<String, Object> map = channel.getConfiguration().getProperties();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case PARAM_RESOURCE_ID:
                    resourceId = getChannelParameterAsInteger(entry.getValue());
                    break;
                case PARAM_DIRECTION:
                    direction = getChannelParameterAsString(entry.getValue());
                    break;
                case PARAM_CMD_TO_REACT:
                    commandToReact = getChannelParameterAsString(entry.getValue());
                    break;
                case PARAM_SERIAL_NUMBER:
                    serialNumber = getChannelParameterAsLong(entry.getValue());
                    break;
                case PARAM_PULSE_WIDTH:
                    pulseWidth = getChannelParameterAsInteger(entry.getValue());
                    break;
                case PARAM_LONG_PRESS_MAX_TIME:
                    longPressMaxTime = getChannelParameterAsInteger(entry.getValue());
                    break;
                case PARAM_SHORT_PRESS_MAX_TIME:
                    shortPressMaxTime = getChannelParameterAsInteger(entry.getValue());
                    break;
                case PARAM_INVERTED:
                    inverted = getChannelParameterAsBoolean(entry.getValue());
                    break;
            }
        }
    }

    public String getChannelTypeId() {
        return channelTypeId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isDirectionBoth() {
        if (direction != null) {
            if (DIRECTION_READ_WRITE.equals(direction)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDirectionReadOnly() {
        if (direction != null) {
            if (DIRECTION_READ_ONLY.equals(direction)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDirectionWriteOnly() {
        if (direction != null) {
            if (DIRECTION_WRITE_ONLY.equals(direction)) {
                return true;
            }
        }
        return false;
    }

    public String getCommandToReact() {
        return commandToReact;
    }

    public Integer getPulseWidth() {
        return pulseWidth;
    }

    public Boolean getInverted() {
        return inverted;
    }

    public boolean isInverted() {
        if (inverted != null) {
            return inverted;
        }
        return false;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public Integer getShortPressMaxTime() {
        return shortPressMaxTime;
    }

    public Integer getLongPressMaxTime() {
        return longPressMaxTime;
    }

    private Boolean getChannelParameterAsBoolean(Object value) throws IllegalArgumentException {
        if (value != null) {
            try {
                return ((Boolean) value).booleanValue();
            } catch (ClassCastException | NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return null;
    }

    private Integer getChannelParameterAsInteger(Object value) throws IllegalArgumentException {
        if (value != null) {
            try {
                return ((BigDecimal) value).intValue();
            } catch (ClassCastException | NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return null;
    }

    private Long getChannelParameterAsLong(Object value) throws IllegalArgumentException {
        if (value != null) {
            try {
                return ((BigDecimal) value).longValue();
            } catch (ClassCastException | NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return null;
    }

    private String getChannelParameterAsString(Object value) throws IllegalArgumentException {
        if (value != null) {
            try {
                return (String) value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format(
                "[ channelTypeId=%s, resourceId=%d, direction=%s, commandToReact=%s, pulseWidth=%d, inverted=%b, serialNumber=%d, shortPressMaxTime=%d, longPressMaxTime=%d ]",
                channelTypeId, resourceId, direction, commandToReact, pulseWidth, inverted, serialNumber,
                shortPressMaxTime, longPressMaxTime);
    }
}
