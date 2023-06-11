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
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

/**
 * Special {@link Action} needed to control shutters.
 *
 * @author Marco Mans
 */
public class ShutterAction extends Action {

    public enum ShutterActions {
        UP,
        DOWN,
        STOP
    }

    private static final String TYPE_STOP_RAMP = "StopRamp";
    private static final String TYPE_START_RAMP = "StartRamp";
    private static final String DIRECTION_RAMP_UP = "RampUp";
    private static final String DIRECTION_RAMP_DOWN = "RampDown";
    private static final String CONSTANT = "Constant";
    private static final String NAMESPACE_COSIP = "CosipDevices.RWE";

    /**
     * Describes a Shutteraction
     *
     * @param capabilityId String of the 32 character capability id
     * @param action Which action to perform (UP, DOWN, STOP)
     */
    public ShutterAction(String capabilityId, ShutterActions action) {
        setTargetCapabilityById(capabilityId);
        setNamespace(NAMESPACE_COSIP);
        final ActionParams params = new ActionParams();

        if (ShutterActions.STOP.equals(action)) {
            setType(TYPE_STOP_RAMP);
        } else if (ShutterActions.UP.equals(action)) {
            setType(TYPE_START_RAMP);
            params.setRampDirection(new StringActionParam(CONSTANT, DIRECTION_RAMP_UP));
        } else if (ShutterActions.DOWN.equals(action)) {
            setType(TYPE_START_RAMP);
            params.setRampDirection(new StringActionParam(CONSTANT, DIRECTION_RAMP_DOWN));
        }
        setParams(params);
    }
}
