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
package org.openhab.binding.yeelight.internal.lib.device;

/**
 * Implemented by devices with the possibility to enable nightlight mode.
 *
 * @author Viktor Koop - Initial contribution
 */
public interface DeviceWithNightlight {

    /**
     * Toggle the nightlight mode on or off.
     *
     * @param mode
     */
    void toggleNightlightMode(boolean mode);
}
