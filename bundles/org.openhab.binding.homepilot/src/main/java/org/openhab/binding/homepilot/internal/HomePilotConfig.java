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
package org.openhab.binding.homepilot.internal;

/**
 * @author Steffen Stundzig - Initial contribution
 */
public class HomePilotConfig {

    private String address = null;

    public String getAddress() {
        if (address == null) {
            throw new IllegalStateException("The address for the hompilot bridge is not set correctly. "
                    + "Do you have setup a .things file with e.g.: 'homepilot:bridge:default [ address=\"YOUR_HOMEPILOT_IP\" ]'?");
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
