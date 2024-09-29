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
package org.openhab.binding.snmp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;

/**
 * The {@link SnmpChannelConfiguration} class contains fields mapping channel configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SnmpChannelConfiguration {
    public @Nullable String oid;
    public SnmpChannelMode mode = SnmpChannelMode.READ;
    public @Nullable SnmpDatatype datatype;
    public @Nullable String unit;

    public @Nullable String onvalue;
    public @Nullable String offvalue;
    public @Nullable String exceptionValue;

    public boolean doNotLogException = false;
}
