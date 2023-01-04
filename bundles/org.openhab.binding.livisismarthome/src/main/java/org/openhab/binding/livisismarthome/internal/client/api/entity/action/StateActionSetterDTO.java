/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.api.entity.action;

import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;

/**
 * Special {@link ActionDTO} needed to set a state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class StateActionSetterDTO extends ActionDTO {

    private static final String CONSTANT = "Constant";
    private static final String ACTION_TYPE_SETSTATE = "SetState";

    /**
     * Constructs a new {@link StateActionSetterDTO}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link CapabilityDTO}, {@link CapabilityDTO#TYPE_SWITCHACTUATOR} or
     *            {@link CapabilityDTO#TYPE_VARIABLEACTUATOR}
     * @param state the new state as boolean (true=on, false=off)
     */
    public StateActionSetterDTO(String capabilityId, String capabilityType, boolean state) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParamsDTO params = new ActionParamsDTO();

        if (CapabilityDTO.TYPE_SWITCHACTUATOR.equals(capabilityType)) {
            params.setOnState(new BooleanActionParamDTO(CONSTANT, state));
        } else if (CapabilityDTO.TYPE_VARIABLEACTUATOR.equals(capabilityType)) {
            params.setValue(new BooleanActionParamDTO(CONSTANT, state));
        } else if (CapabilityDTO.TYPE_ALARMACTUATOR.equals(capabilityType)) {
            params.setOnState(new BooleanActionParamDTO(CONSTANT, state));
        } else if (CapabilityDTO.TYPE_THERMOSTATACTUATOR.equals(capabilityType)) {
            final String operationMode;
            if (state) {
                operationMode = CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_AUTO;
            } else {
                operationMode = CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_MANUAL;
            }
            params.setOperationMode(new StringActionParamDTO(CONSTANT, operationMode));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link StateActionSetterDTO}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link CapabilityDTO}, {@link CapabilityDTO#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new double value
     */
    public StateActionSetterDTO(String capabilityId, String capabilityType, double newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParamsDTO params = new ActionParamsDTO();

        if (CapabilityDTO.TYPE_THERMOSTATACTUATOR.equals(capabilityType)) {
            params.setPointTemperature(new DoubleActionParamDTO(CONSTANT, newValue));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link StateActionSetterDTO}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link CapabilityDTO}, {@link CapabilityDTO#TYPE_DIMMERACTUATOR}
     * @param newValue the new int value
     */
    public StateActionSetterDTO(String capabilityId, String capabilityType, int newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParamsDTO params = new ActionParamsDTO();

        if (CapabilityDTO.TYPE_DIMMERACTUATOR.equals(capabilityType)) {
            params.setDimLevel(new IntegerActionParamDTO(CONSTANT, newValue));
        } else if (CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR.equals(capabilityType)) {
            params.setShutterLevel(new IntegerActionParamDTO(CONSTANT, newValue));
        }

        setParams(params);
    }
}
