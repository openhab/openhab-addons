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
package org.openhab.binding.snmp.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.snmp4j.security.SecurityLevel;

/**
 * The {@link SnmpSecurityModel} enum defines the security model for v3
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum SnmpSecurityModel {
    NO_AUTH_NO_PRIV(SecurityLevel.NOAUTH_NOPRIV),
    AUTH_NO_PRIV(SecurityLevel.AUTH_NOPRIV),
    AUTH_PRIV(SecurityLevel.AUTH_PRIV);

    private final int securityLevel;

    SnmpSecurityModel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * get the numeric security level
     *
     * @return the int representing this security level
     */
    public int getSecurityLevel() {
        return securityLevel;
    }
}
