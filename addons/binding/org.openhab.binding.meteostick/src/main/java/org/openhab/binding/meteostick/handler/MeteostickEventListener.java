/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meteostick.handler;

/**
 * This interface provides notifications between the bridge and the sensors.
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public interface MeteostickEventListener {
    /**
     * Called each time a new line of data is received
     *
     * @param data a line of data from the meteoStick
     */
    public void onDataReceived(String data[]);
}
