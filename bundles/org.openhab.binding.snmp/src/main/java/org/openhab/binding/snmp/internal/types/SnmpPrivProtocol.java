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
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.smi.OID;

/**
 * The {@link SnmpPrivProtocol} enum defines the possible privacy protocols for v3
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum SnmpPrivProtocol {
    AES128(PrivAES128.ID),
    AES192(PrivAES192.ID),
    AES256(PrivAES256.ID),
    DES(PrivDES.ID);

    private final OID oid;

    SnmpPrivProtocol(OID oid) {
        this.oid = oid;
    }

    /**
     * get the OID for this privacy protocol
     *
     * @return the corresponding OID
     */
    public OID getOid() {
        return oid;
    }
}
