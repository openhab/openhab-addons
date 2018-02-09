/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceClassObject;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceProperty;
import org.openhab.binding.miele.handler.MieleBridgeHandler.HomeDevice;

/**
 *
 * The {@link ApplianceStatusListener} is notified when an appliance status has changed or
 * an appliance has been removed or added.
 *
 * @author Karel Goderis - Initial contribution
 */
public interface ApplianceStatusListener {

    /**
     * This method is called whenever the state of the given appliance has changed.
     *
     * @param uid the UID of the aplliance that has changed
     * @param dco the POJO containing the new state (properties and/or operations)
     */
    public void onApplianceStateChanged(String uid, DeviceClassObject dco);

    /**
     * This method is called whenever a "property" of the given appliance has changed.
     *
     * @param uid the UID of the aplliance that has changed
     * @param dco the POJO containing the new state of the property
     */
    public void onAppliancePropertyChanged(String uid, DeviceProperty dp);

    /**
     * This method us called whenever an appliance is removed.
     *
     * @param appliance The XGW homedevice definition of the appliance that was removed
     */
    public void onApplianceRemoved(HomeDevice appliance);

    /**
     * This method us called whenever an appliance is added.
     *
     * @param appliance The XGW homedevice definition of the appliance that was removed
     */
    public void onApplianceAdded(HomeDevice appliance);

}
