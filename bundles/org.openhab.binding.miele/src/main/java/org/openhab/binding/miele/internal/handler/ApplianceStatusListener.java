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
package org.openhab.binding.miele.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miele.internal.api.dto.DeviceClassObject;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;

/**
 * The {@link ApplianceStatusListener} is notified when the status for the subscribed
 * appliance has changed or it has been removed or added.
 *
 * @author Karel Goderis - Initial contribution
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
@NonNullByDefault
public interface ApplianceStatusListener {

    /**
     * This method is called whenever the state of the given appliance has changed.
     *
     * @param dco the POJO containing the new state (properties and/or operations)
     */
    void onApplianceStateChanged(DeviceClassObject dco);

    /**
     * This method is called whenever a "property" of the given appliance has changed.
     *
     * @param dco the POJO containing the new state of the property
     */
    void onAppliancePropertyChanged(DeviceProperty dp);

    /**
     * This method is called whenever an appliance is removed.
     */
    void onApplianceRemoved();

    /**
     * This method is called whenever an appliance is added.
     *
     * @param appliance The XGW homedevice definition of the appliance that was added
     */
    void onApplianceAdded(HomeDevice appliance);
}
