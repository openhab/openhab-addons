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
package org.openhab.binding.adorne.internal.hub;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AdorneHubChangeNotify} interface is used by the {@link AdorneHubController} to notify listeners about
 * Adorne device status and hub connection changes.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public interface AdorneHubChangeNotify {
    /**
     * Notify listener about state change of on/off and brightness state
     *
     * @param zoneId zone ID for which change occurred
     * @param onOff new on/off state
     * @param brightness new brightness
     */
    void stateChangeNotify(int zoneId, boolean onOff, int brightness);

    /**
     * Notify listener about hub connection change
     *
     * @param connected new connection state
     */
    void connectionChangeNotify(boolean connected);
}
