/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.client.BusMessageListener;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link GroupAddressListener} is an interface that needs to be
 * implemented by classes that want to listen to Group Addresses
 * on the KNX bus
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public interface GroupAddressListener extends BusMessageListener {

    /**
     * Called to verify if the GroupAddressListener has an interest in the given GroupAddress
     *
     * @param destination
     */
    public boolean listensTo(GroupAddress destination);

}
