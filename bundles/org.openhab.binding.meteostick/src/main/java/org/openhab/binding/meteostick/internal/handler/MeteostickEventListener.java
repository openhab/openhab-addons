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
package org.openhab.binding.meteostick.internal.handler;

/**
 * This interface provides notifications between the bridge and the sensors.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public interface MeteostickEventListener {
    /**
     * Called each time a new line of data is received
     *
     * @param data a line of data from the meteoStick
     */
    void onDataReceived(String[] data);
}
