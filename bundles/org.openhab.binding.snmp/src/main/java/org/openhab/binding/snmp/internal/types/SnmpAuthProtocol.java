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
package org.openhab.binding.snmp.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.snmp4j.security.AuthHMAC128SHA224;
import org.snmp4j.security.AuthHMAC192SHA256;
import org.snmp4j.security.AuthHMAC256SHA384;
import org.snmp4j.security.AuthHMAC384SHA512;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.smi.OID;

/**
 * The {@link SnmpAuthProtocol} enum defines the possible authentication protocols for v3
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum SnmpAuthProtocol {
    MD5(AuthMD5.ID),
    SHA(AuthSHA.ID),
    HMAC128SHA224(AuthHMAC128SHA224.ID),
    HMAC192SHA256(AuthHMAC192SHA256.ID),
    HMAC256SHA384(AuthHMAC256SHA384.ID),
    HMAC384SHA512(AuthHMAC384SHA512.ID);

    private final OID oid;

    SnmpAuthProtocol(OID oid) {
        this.oid = oid;
    }

    /**
     * get the OID for this authentication protocol
     *
     * @return the corresponding OID
     */
    public OID getOid() {
        return oid;
    }
}
