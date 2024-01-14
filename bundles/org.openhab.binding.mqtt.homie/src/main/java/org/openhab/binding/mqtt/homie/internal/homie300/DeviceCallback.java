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
package org.openhab.binding.mqtt.homie.internal.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes.ReadyState;

/**
 * Callbacks to inform about the Homie Device state, statistics changes, node layout changes.
 * Meant to be used by the Homie thing handler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface DeviceCallback extends ChannelStateUpdateListener {
    /**
     * Called whenever the device state changed
     *
     * @param state The new state
     */
    void readyStateChanged(ReadyState state);

    /**
     * Called, whenever a Homie node was existing before, but is not anymore.
     *
     * @param node The affected node class.
     */
    void nodeRemoved(Node node);

    /**
     * Called, whenever a Homie property was existing before, but is not anymore.
     *
     * @param property The affected property class.
     */
    void propertyRemoved(Property property);

    /**
     * Called, whenever a Homie node was added or changed.
     *
     * @param node The affected node class.
     */
    void nodeAddedOrChanged(Node node);

    /**
     * Called, whenever a Homie property was added or changed.
     *
     * @param property The affected property class.
     */
    void propertyAddedOrChanged(Property property);
}
