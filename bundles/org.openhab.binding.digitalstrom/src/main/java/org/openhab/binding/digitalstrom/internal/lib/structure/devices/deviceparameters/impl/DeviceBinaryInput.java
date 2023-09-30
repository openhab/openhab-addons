/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link DeviceBinaryInput} contains all information of a device binary input, e.g. binary input type id (see
 * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum},
 * state and so on.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceBinaryInput {

    private final Short targetGroupType;
    private final Short targetGroup;
    private final Short inputType;
    private final Short inputId;
    private Short stateValue;

    /**
     * Creates a new
     * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum}
     * through the {@link JsonObject} of the binary inputs at json response
     * from digitalSTROM JSON-API or property-tree. Will be automatically added to a
     * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl}, if binary
     * inputs exists.
     *
     * @param jsonObject must not be null
     */
    public DeviceBinaryInput(JsonObject jsonObject) {
        if (jsonObject.get(JSONApiResponseKeysEnum.TARGET_GROUP_TYPE.getKey()) != null) {
            targetGroupType = jsonObject.get(JSONApiResponseKeysEnum.TARGET_GROUP_TYPE.getKey()).getAsShort();
        } else {
            targetGroupType = null;
        }
        if (jsonObject.get(JSONApiResponseKeysEnum.TARGET_GROUP.getKey()) != null) {
            targetGroup = jsonObject.get(JSONApiResponseKeysEnum.TARGET_GROUP.getKey()).getAsShort();
        } else {
            targetGroup = null;
        }
        if (jsonObject.get(JSONApiResponseKeysEnum.INPUT_TYPE.getKey()) != null) {
            inputType = jsonObject.get(JSONApiResponseKeysEnum.INPUT_TYPE.getKey()).getAsShort();
        } else {
            inputType = null;
        }
        if (jsonObject.get(JSONApiResponseKeysEnum.INPUT_ID.getKey()) != null) {
            inputId = jsonObject.get(JSONApiResponseKeysEnum.INPUT_ID.getKey()).getAsShort();
        } else {
            inputId = null;
        }
        if (jsonObject.get(JSONApiResponseKeysEnum.STATE_VALUE.getKey()) != null) {
            stateValue = jsonObject.get(JSONApiResponseKeysEnum.STATE_VALUE.getKey()).getAsShort();
        }
        if (stateValue == null && jsonObject.get(JSONApiResponseKeysEnum.STATE.getKey()) != null) {
            stateValue = jsonObject.get(JSONApiResponseKeysEnum.STATE.getKey()).getAsShort();
        } else {
            stateValue = null;
        }
    }

    /**
     * Returns the current state of this {@link DeviceBinaryInput}.
     *
     * @return the state
     */
    public Short getState() {
        return stateValue;
    }

    /**
     * Sets the state of this {@link DeviceBinaryInput}.
     *
     * @param state the state to set
     */
    public void setState(Short state) {
        this.stateValue = state;
    }

    /**
     * Returns the target group type id of this {@link DeviceBinaryInput}.
     *
     * @return the targetGroupType
     */
    public Short getTargetGroupType() {
        return targetGroupType;
    }

    /**
     * Returns the target group id of this {@link DeviceBinaryInput}.
     *
     * @return the targetGroup
     */
    public Short getTargetGroup() {
        return targetGroup;
    }

    /**
     * Returns the input type id of this {@link DeviceBinaryInput}. Available input types see
     * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum}.
     *
     * @return the inputType
     */
    public Short getInputType() {
        return inputType;
    }

    /**
     * Returns the input id of this {@link DeviceBinaryInput}.
     *
     * @return the inputId
     */
    public Short getInputId() {
        return inputId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputType == null) ? 0 : inputType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeviceBinaryInput)) {
            return false;
        }
        DeviceBinaryInput other = (DeviceBinaryInput) obj;
        if (inputType == null) {
            if (other.inputType != null) {
                return false;
            }
        } else if (!inputType.equals(other.inputType)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceBinaryInput [targetGroupType=" + targetGroupType + ", targetGroup=" + targetGroup + ", inputType="
                + inputType + ", inputId=" + inputId + ", state=" + stateValue + "]";
    }
}
