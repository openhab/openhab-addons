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
package org.openhab.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;

/**
 * The {@link SensorStatusListener} is notified when a sensor status has changed or a sensor has been removed or added.
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface SensorStatusListener {

    /**
     * This method is called whenever the state of the given sensor has changed. The new state can be obtained by
     * {@link FullSensor#getState()}.
     *
     * @param bridge The bridge the changed sensor is connected to.
     * @param sensor The sensor which received the state update.
     */
    void onSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor);

    /**
     * This method is called whenever a sensor is removed.
     *
     * @param bridge The bridge the removed sensor was connected to.
     * @param sensor The removed sensor
     */
    void onSensorRemoved(@Nullable HueBridge bridge, FullSensor sensor);

    /**
     * This method is called whenever a sensor is reported as gone.
     *
     * @param bridge The bridge the reported sensor was connected to.
     * @param sensor The sensor which is reported as gone.
     */
    void onSensorGone(@Nullable HueBridge bridge, FullSensor sensor);

    /**
     * This method is called whenever a sensor is added.
     *
     * @param bridge The bridge the added sensor was connected to.
     * @param sensor The added sensor
     */
    void onSensorAdded(@Nullable HueBridge bridge, FullSensor sensor);
}
