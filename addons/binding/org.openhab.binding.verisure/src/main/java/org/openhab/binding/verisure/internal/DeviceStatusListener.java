/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

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
     *            The object that was changed.
     */
    public void onDeviceStateChanged(VerisureObjectJSON updateObject);

    /**
     * This method us called whenever a device is removed.
     *
     * @param device
     *            The object that is removed
     */
    public void onDeviceRemoved(VerisureObjectJSON updateObject);

    /**
     * This method us called whenever a device is added.
     * 
     * @param device
     *            The object which is added.
     */
    public void onDeviceAdded(VerisureObjectJSON updateObject);

}
