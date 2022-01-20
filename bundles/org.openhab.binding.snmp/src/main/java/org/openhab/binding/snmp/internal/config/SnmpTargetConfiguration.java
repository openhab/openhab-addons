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
package org.openhab.binding.snmp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.SnmpProtocolVersion;

/**
 * The {@link SnmpTargetConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SnmpTargetConfiguration {
    public @Nullable String hostname;
    public int port = 161;
    public String community = "public";
    public int refresh = 60;
    public SnmpProtocolVersion protocol = SnmpProtocolVersion.v1;
    public int timeout = 1500;
    public int retries = 2;
}
