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
package org.openhab.binding.insteon.internal.device.feature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * Interface for classes that want to listen to notifications from an Insteon device feature
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public interface FeatureListener {
    /**
     * Notifies that the device feature state has been updated
     *
     * @param state the updated state
     */
    public void stateUpdated(State state);

    /**
     * Notifies that the device feature has triggered an event
     *
     * @param event the triggered event name
     */
    public void eventTriggered(String event);
}
