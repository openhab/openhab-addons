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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeliosEasyControlsConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernhard Bauer - Initial contribution
 */
@NonNullByDefault
public class HeliosEasyControlsConfiguration {

    /**
     * The binding's refresh interval
     */
    private int refreshInterval;

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
