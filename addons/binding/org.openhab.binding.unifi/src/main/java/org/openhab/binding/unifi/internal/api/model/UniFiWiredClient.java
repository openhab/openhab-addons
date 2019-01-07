/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api.model;

/**
 * A {@link UniFiWiredClient} represents a wired {@link UniFiClient}.
 *
 * A wired client is physically connected to the network - typically it is connected via an Ethernet cable.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiWiredClient extends UniFiClient {

    private String swMac;

    @Override
    public Boolean isWired() {
        return true;
    }

    @Override
    public String getDeviceMac() {
        return swMac;
    }

}
