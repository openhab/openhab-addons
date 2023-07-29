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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PowermaxPanelSettingsListener} is a listener for updated
 * alarm panel settings
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface PowermaxPanelSettingsListener {

    /**
     * This method is called when the bridge thing handler identifies
     * a change in the alarm panel settings
     *
     * @param settings the updated alarm panel settings or null if the panel settings are unknown
     */
    public void onPanelSettingsUpdated(@Nullable PowermaxPanelSettings settings);

    /**
     * This method is called when the bridge thing handler identifies
     * a change in one zone settings
     *
     * @param zoneNumber the zone number
     * @param settings the updated alarm panel settings or null if the panel settings are unknown
     */
    public void onZoneSettingsUpdated(int zoneNumber, @Nullable PowermaxPanelSettings settings);
}
