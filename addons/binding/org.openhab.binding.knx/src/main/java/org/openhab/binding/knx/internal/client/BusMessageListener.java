/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

/**
 * Callback interface for KNX bus messages
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@NonNullByDefault
public interface BusMessageListener {

    /**
     * Called when the KNX bridge receives a group write telegram
     *
     * @param bridge
     * @param destination
     * @param asdu
     */
    public void onGroupWrite(AbstractKNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu);

    /**
     * Called when the KNX bridge receives a group read telegram
     *
     * @param bridge
     * @param destination
     * @param asdu
     */
    public void onGroupRead(AbstractKNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu);

    /**
     * Called when the KNX bridge receives a group read response telegram
     *
     * @param bridge
     * @param destination
     * @param asdu
     */
    public void onGroupReadResponse(AbstractKNXClient client, IndividualAddress source, GroupAddress destination,
            byte[] asdu);

}
