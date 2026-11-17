/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;

/**
 * Asks the channels of a bridge for the data type of a group address.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface DataTypeLookup {

    /** {@return the data type a channel decodes the address with, null if no channel uses it} */
    @Nullable
    DataType dataTypeOf(GroupAddress address);
}
