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
package org.openhab.binding.knx.internal.client;

import java.util.Set;

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
     * @return a Set of group addresses.
     */
    Set<GroupAddress> getGroupAddresses();
}
