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
package org.openhab.binding.digitalstrom.internal.lib.event.constants;

/**
 * The {@link EventNames} contains all needed event names to subscribe at the digitalSTROM server.
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 */
public class EventNames {

    public static final String ZONE_SENSOR_VALUE = "zoneSensorValue";
    public static final String HEATING_CONTROL_OPERATION_MODE = "heating-controller.operation-mode";
    public static final String STATE_CHANGED = "stateChange";
    public static final String CALL_SCENE = "callScene";
    public static final String UNDO_SCENE = "undoScene";
    public static final String DEVICE_SENSOR_VALUE = "deviceSensorValue";
    public static final String DEVICE_BINARY_INPUT_EVENT = "deviceBinaryInputEvent";
}
