/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.snmp.internal.types.SnmpAuthProtocol;
import org.openhab.binding.snmp.internal.types.SnmpPrivProtocol;
import org.openhab.binding.snmp.internal.types.SnmpProtocolVersion;
import org.openhab.binding.snmp.internal.types.SnmpSecurityModel;

/**
 * The {@link SnmpTargetConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SnmpTargetConfiguration {
    // common
    public @Nullable String hostname;
    public int port = 161;
    public SnmpProtocolVersion protocol = SnmpProtocolVersion.v1;

    public int refresh = 60;
    public int timeout = 1500;
    public int retries = 2;

    // v1/v2c only
    public String community = "public";

    // v3 only
    public SnmpSecurityModel securityModel = SnmpSecurityModel.NO_AUTH_NO_PRIV;
    public @Nullable String user;
    public @Nullable String engineId;
    public SnmpAuthProtocol authProtocol = SnmpAuthProtocol.MD5;
    public @Nullable String authPassphrase;
    public SnmpPrivProtocol privProtocol = SnmpPrivProtocol.DES;
    public @Nullable String privPassphrase;
}
