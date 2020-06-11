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
package org.openhab.binding.snmp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.snmp.internal.SnmpChannelMode;
import org.openhab.binding.snmp.internal.SnmpDatatype;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 * The {@link SnmpInternalChannelConfiguration} class contains fields mapping channel configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class SnmpInternalChannelConfiguration {
    public final ChannelUID channelUID;
    public final OID oid;
    public final SnmpChannelMode mode;
    public final SnmpDatatype datatype;

    public final @Nullable Variable onValue;
    public final @Nullable Variable offValue;
    public final State exceptionValue;
    public final boolean doNotLogException;

    public SnmpInternalChannelConfiguration(ChannelUID channelUID, OID oid, SnmpChannelMode mode, SnmpDatatype datatype,
            @Nullable Variable onValue, @Nullable Variable offValue, State exceptionValue, boolean doNotLogException) {
        this.channelUID = channelUID;
        this.oid = oid;
        this.mode = mode;
        this.datatype = datatype;
        this.onValue = onValue;
        this.offValue = offValue;
        this.exceptionValue = exceptionValue;
        this.doNotLogException = doNotLogException;
    }
}
