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
package org.openhab.binding.knx.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.Type;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.process.ProcessEvent;

/**
 * This interface must be implemented by classes that provide a type mapping
 * between openHAB and KNX.
 * When a command or status update is sent to an item on the openHAB event bus,
 * it must be clear, in which format it must be sent to KNX and vice versa.
 *
 * @author Kai Kreuzer
 *
 */
@NonNullByDefault
public interface KNXTypeMapper {

    /**
     * maps an openHAB command/state to a string value which correspond to its datapoint in KNX
     *
     * @param type a command or state
     * @param dpt the corresponding datapoint type
     * @return datapoint value as a string
     */
    @Nullable
    public String toDPTValue(Type type, @Nullable String dpt);

    /**
     * maps a datapoint value to an openHAB command or state
     *
     * @param datapoint the source datapoint
     * @param data the datapoint value as an ASDU byte array (see <code>{@link ProcessEvent}.getASDU()</code>)
     * @return a command or state of openHAB
     */
    @Nullable
    public Type toType(Datapoint datapoint, byte[] data);

    @Nullable
    public Class<? extends Type> toTypeClass(@Nullable String dpt);
}
