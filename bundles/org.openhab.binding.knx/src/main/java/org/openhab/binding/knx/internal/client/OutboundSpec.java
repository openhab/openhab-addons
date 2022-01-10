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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.Type;

import tuwien.auto.calimero.GroupAddress;

/**
 * Describes the relevant parameters for writing to the KNX bus.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface OutboundSpec {

    /**
     * Get the datapoint type.
     *
     * @return the datapoint type
     */
    String getDPT();

    /**
     * The group address to be used.
     *
     * @return the group address
     */
    @Nullable
    GroupAddress getGroupAddress();

    /**
     * The command or state to be sent.
     *
     * @return the command/state
     */
    Type getType();
}
