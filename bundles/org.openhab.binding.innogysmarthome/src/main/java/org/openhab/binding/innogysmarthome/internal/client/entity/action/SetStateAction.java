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
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;

/**
 * Special {@link Action} needed to set a state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class SetStateAction extends Action {

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_SWITCHACTUATOR} or
     *            {@link Capability#TYPE_VARIABLEACTUATOR}
     * @param state the new state as boolean (true=on, false=off)
     */
    public SetStateAction(String capabilityId, String capabilityType, boolean state) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        ActionParams params = new ActionParams();

        if (capabilityType.equals(Capability.TYPE_SWITCHACTUATOR)) {
            params.setOnState(new BooleanActionParam("Constant", state));
        } else if (capabilityType.equals(Capability.TYPE_VARIABLEACTUATOR)) {
            params.setValue(new BooleanActionParam("Constant", state));
        } else if (capabilityType.equals(Capability.TYPE_ALARMACTUATOR)) {
            params.setOnState(new BooleanActionParam("Constant", state));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new double value
     */
    public SetStateAction(String capabilityId, String capabilityType, double newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        ActionParams params = new ActionParams();

        if (capabilityType.equals(Capability.TYPE_THERMOSTATACTUATOR)) {
            params.setPointTemperature(new DoubleActionParam("Constant", newValue));
        }
        setParams(params);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_DIMMERACTUATOR}
     * @param newValue the new int value
     */
    public SetStateAction(String capabilityId, String capabilityType, int newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        ActionParams params = new ActionParams();

        if (capabilityType.equals(Capability.TYPE_DIMMERACTUATOR)) {
            params.setDimLevel(new IntegerActionParam("Constant", newValue));
        } else if (capabilityType.equals(Capability.TYPE_ROLLERSHUTTERACTUATOR)) {
            params.setShutterLevel(new IntegerActionParam("Constant", newValue));
        }

        setParams(params);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new string value
     */
    public SetStateAction(String capabilityId, String capabilityType, String newValue) {
        setType(ACTION_TYPE_SETSTATE);
        setTargetCapabilityById(capabilityId);
        ActionParams params = new ActionParams();

        if (capabilityType.equals(Capability.TYPE_THERMOSTATACTUATOR)) {
            params.setOperationMode(new StringActionParam("Constant", newValue));
        }
        setParams(params);
    }

}
