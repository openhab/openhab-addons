/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

/**
 * The {@link KNXBusListener} is an interface that needs to be
 * implemented by classes that want to listen to KNX bus
 *
 * @author Karel Goderis - Initial contribution
 */
public interface KNXBusListener {

    /**
     *
     * Called when a KNX telegram is seen on the KNX bus
     *
     * @param source - the KNX source sending the telegram
     * @param destination - the destination group address
     * @param asdu - the telegram payload
     */
    public void onActivity(IndividualAddress source, GroupAddress destination, byte[] asdu);

}
