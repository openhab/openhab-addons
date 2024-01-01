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
package org.openhab.binding.miele.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;

/**
 * The {@link DiscoveryListener} is notified when any appliance has been removed or added.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface DiscoveryListener {

    /**
     * This method is called whenever any appliance is removed.
     *
     * @param appliance The XGW homedevice definition of the appliance that was removed
     */
    void onApplianceRemoved(HomeDevice appliance);

    /**
     * This method is called whenever any appliance is added.
     *
     * @param appliance The XGW homedevice definition of the appliance that was added
     */
    void onApplianceAdded(HomeDevice appliance);
}
