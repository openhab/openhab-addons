/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.listener;

import org.openhab.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;

/**
 * The {@link TemperatureControlStatusListener} can be implemented to get informed by configuration and status changes.
 * <br>
 * It also can be implemented as discovery, than the id have to be {@link #DISCOVERY}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface TemperatureControlStatusListener {

    /**
     * The id for discovery.
     */
    static Integer DISCOVERY = -2;

    /**
     * Will be called, if the configuration of the {@link TemperatureControlStatus} has changed.
     *
     * @param tempControlStatus that has changed
     */
    void configChanged(TemperatureControlStatus tempControlStatus);

    /**
     * Will be called, if the target temperature has changed.
     *
     * @param newValue of the target temperature
     */
    void onTargetTemperatureChanged(Float newValue);

    /**
     * Will be called, if the control value has changed.
     *
     * @param newValue of the control value
     */
    void onControlValueChanged(Integer newValue);

    /**
     * Registers a {@link TemperatureControlSensorTransmitter}.
     *
     * @param temperatureSensorTransmitter to register
     */
    void registerTemperatureSensorTransmitter(TemperatureControlSensorTransmitter temperatureSensorTransmitter);

    /**
     * Returns the id of this {@link TemperatureControlStatusListener}.
     *
     * @return id
     */
    Integer getTemperationControlStatusListenrID();
}
