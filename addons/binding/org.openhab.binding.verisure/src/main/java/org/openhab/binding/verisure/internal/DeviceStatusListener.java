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
package org.openhab.binding.verisure.internal;

import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param updateObject
     *                         The object that was changed.
     */
    public void onDeviceStateChanged(VerisureThingJSON updateObject);

    /**
     * This method us called whenever a device is removed.
     *
     * @param device
     *                   The object that is removed
     */
    public void onDeviceRemoved(VerisureThingJSON updateObject);

    /**
     * This method us called whenever a device is added.
     *
     * @param device
     *                   The object which is added.
     */
    public void onDeviceAdded(VerisureThingJSON updateObject);

}
