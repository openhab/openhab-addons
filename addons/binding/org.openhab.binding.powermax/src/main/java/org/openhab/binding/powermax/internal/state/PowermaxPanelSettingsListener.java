/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

/**
 * The {@link PowermaxPanelSettingsListener} is a listener for updated
 * alarm panel settings
 *
 * @author Laurent Garnier - Initial contribution
 */
public interface PowermaxPanelSettingsListener {

    /**
     * This method is called when the bridge thing handler identifies
     * a change in the alarm panel settings
     *
     * @param settings the updated alarm panel settings or null if the panel settings are unknown
     */
    public void onPanelSettingsUpdated(PowermaxPanelSettings settings);

    /**
     * This method is called when the bridge thing handler identifies
     * a change in one zone settings
     *
     * @param zoneNumber the zone number
     * @param settings the updated alarm panel settings or null if the panel settings are unknown
     */
    public void onZoneSettingsUpdated(int zoneNumber, PowermaxPanelSettings settings);
}
