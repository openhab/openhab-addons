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
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;

/**
 * Special {@link Action} needed to set a state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class StateActionSetter extends Action {

    private static final String CONSTANT = "Constant";

    /**
     * Constructs a new {@link StateActionSetter}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_SWITCHACTUATOR} or
     *            {@link Capability#TYPE_VARIABLEACTUATOR}
     * @param state the new state as boolean (true=on, false=off)
     */
    public StateActionSetter(String capabilityId, String capabilityType, boolean state) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParams params = new ActionParams();

        if (Capability.TYPE_SWITCHACTUATOR.equals(capabilityType)) {
            params.setOnState(new BooleanActionParam(CONSTANT, state));
        } else if (Capability.TYPE_VARIABLEACTUATOR.equals(capabilityType)) {
            params.setValue(new BooleanActionParam(CONSTANT, state));
        } else if (Capability.TYPE_ALARMACTUATOR.equals(capabilityType)) {
            params.setOnState(new BooleanActionParam(CONSTANT, state));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link StateActionSetter}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new double value
     */
    public StateActionSetter(String capabilityId, String capabilityType, double newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParams params = new ActionParams();

        if (Capability.TYPE_THERMOSTATACTUATOR.equals(capabilityType)) {
            params.setPointTemperature(new DoubleActionParam(CONSTANT, newValue));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link StateActionSetter}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_DIMMERACTUATOR}
     * @param newValue the new int value
     */
    public StateActionSetter(String capabilityId, String capabilityType, int newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParams params = new ActionParams();

        if (Capability.TYPE_DIMMERACTUATOR.equals(capabilityType)) {
            params.setDimLevel(new IntegerActionParam(CONSTANT, newValue));
        } else if (Capability.TYPE_ROLLERSHUTTERACTUATOR.equals(capabilityType)) {
            params.setShutterLevel(new IntegerActionParam(CONSTANT, newValue));
        }

        setParams(params);
    }

    /**
     * Constructs a new {@link StateActionSetter}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new string value
     */
    public StateActionSetter(String capabilityId, String capabilityType, String newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        final ActionParams params = new ActionParams();

        if (Capability.TYPE_THERMOSTATACTUATOR.equals(capabilityType)) {
            params.setOperationMode(new StringActionParam(CONSTANT, newValue));
        }
        setParams(params);
    }
}
