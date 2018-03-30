/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.innogysmarthome.internal.client.entity.Constant;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;

/**
 * Special {@link Action} needed to set a state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class SetStateAction extends Action {

    public static final String ACTION_PARAMETER_SWITCHACTUATOR_ONSTATE = "OnState";
    public static final String ACTION_PARAMETER_VARIABLEACTUATOR_VALUE = "Value";
    public static final String ACTION_PARAMETER_THERMOSTATACTUATOR_POINTTEMPERATURE = "PointTemperature";
    public static final String ACTION_PARAMETER_THERMOSTATACTUATOR_OPERATIONMODE = "OperationMode";
    public static final String ACTION_PARAMETER_ALARMACTUATOR_ONSTATE = "OnState";
    public static final String ACTION_PARAMETER_DIMMERACTUATOR_VALUE = "DimLevel";
    public static final String ACTION_PARAMETER_ROLLERSHUTTERACTUATOR_VALUE = "ShutterLevel";

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_SWITCHACTUATOR} or
     *            {@link Capability#TYPE_VARIABLEACTUATOR}
     * @param state the new state as boolean (true=on, false=off)
     */
    public SetStateAction(String capabilityId, String capabilityType, boolean state) {
        super(ACTION_TYPE_SETSTATE);
        setCapabilityLink(capabilityId);

        List<ActionParameter> parameterList = new ArrayList<>();

        if (capabilityType.equals(Capability.TYPE_SWITCHACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_SWITCHACTUATOR_ONSTATE, "/entity/Constant",
                    new Constant(state)));
        } else if (capabilityType.equals(Capability.TYPE_VARIABLEACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_VARIABLEACTUATOR_VALUE, "/entity/Constant",
                    new Constant(state)));
        } else if (capabilityType.equals(Capability.TYPE_ALARMACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_ALARMACTUATOR_ONSTATE, "/entity/Constant",
                    new Constant(state)));
        }
        setParameterList(parameterList);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new double value
     */
    public SetStateAction(String capabilityId, String capabilityType, double newValue) {
        super(ACTION_TYPE_SETSTATE);
        setCapabilityLink(capabilityId);

        List<ActionParameter> parameterList = new ArrayList<>();

        if (capabilityType.equals(Capability.TYPE_THERMOSTATACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_THERMOSTATACTUATOR_POINTTEMPERATURE,
                    "/entity/Constant", new Constant(newValue)));
        }
        setParameterList(parameterList);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_DIMMERACTUATOR}
     * @param newValue the new int value
     */
    public SetStateAction(String capabilityId, String capabilityType, int newValue) {
        super(ACTION_TYPE_SETSTATE);
        setCapabilityLink(capabilityId);

        List<ActionParameter> parameterList = new ArrayList<>();

        if (capabilityType.equals(Capability.TYPE_DIMMERACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_DIMMERACTUATOR_VALUE, "/entity/Constant",
                    new Constant(newValue)));
        } else if (capabilityType.equals(Capability.TYPE_ROLLERSHUTTERACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_ROLLERSHUTTERACTUATOR_VALUE, "/entity/Constant",
                    new Constant(newValue)));
        }

        setParameterList(parameterList);
    }

    /**
     * Constructs a new {@link SetStateAction}.
     *
     * @param capabilityId String of the 32 character capability id
     * @param capabilityType the type of the {@link Capability}, {@link Capability#TYPE_THERMOSTATACTUATOR}
     * @param newValue the new string value
     */
    public SetStateAction(String capabilityId, String capabilityType, String newValue) {
        super(ACTION_TYPE_SETSTATE);
        setCapabilityLink(capabilityId);

        List<ActionParameter> parameterList = new ArrayList<>();

        if (capabilityType.equals(Capability.TYPE_THERMOSTATACTUATOR)) {
            parameterList.add(new ActionParameter(ACTION_PARAMETER_THERMOSTATACTUATOR_OPERATIONMODE, "/entity/Constant",
                    new Constant(newValue)));
        }
        setParameterList(parameterList);
    }

}
