/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.client;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import tuwien.auto.calimero.GroupAddress;

/**
 * Describes the relevant parameters for reading from/listening to the KNX bus.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface InboundSpec {

    /**
     * Get the datapoint type.
     *
     * @return the datapoint type
     */
    String getDPT();

    /**
     * Get the affected group addresses.
     *
     * @return a list of group addresses.
     */
    List<GroupAddress> getGroupAddresses();

}
