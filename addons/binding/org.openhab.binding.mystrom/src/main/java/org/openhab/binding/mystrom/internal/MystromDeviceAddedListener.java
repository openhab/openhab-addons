/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.internal;

/**
 * Used by the discovery service to track when devices are added.
 *
 * @author Stéphane Raemy - Initial Contribution
 */
public interface MystromDeviceAddedListener {

    /**
     * Called when a Wifi Switch device is discovered.
     */
    public void onWifiSwitchAdded(Device device);

}
