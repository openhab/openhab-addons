/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal.handler;

import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceClassObject;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceProperty;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.HomeDevice;

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
    void onApplianceStateChanged(String uid, DeviceClassObject dco);

    /**
     * This method is called whenever a "property" of the given appliance has changed.
     *
     * @param uid the UID of the aplliance that has changed
     * @param dco the POJO containing the new state of the property
     */
    void onAppliancePropertyChanged(String uid, DeviceProperty dp);

    /**
     * This method us called whenever an appliance is removed.
     *
     * @param appliance The XGW homedevice definition of the appliance that was removed
     */
    void onApplianceRemoved(HomeDevice appliance);

    /**
     * This method us called whenever an appliance is added.
     *
     * @param appliance The XGW homedevice definition of the appliance that was removed
     */
    void onApplianceAdded(HomeDevice appliance);
}
