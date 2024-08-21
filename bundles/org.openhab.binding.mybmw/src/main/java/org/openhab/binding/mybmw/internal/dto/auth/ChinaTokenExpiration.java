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
package org.openhab.binding.mybmw.internal.dto.auth;

/**
 * The {@link ChinaTokenExpiration} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChinaTokenExpiration {
    public String jti;// ":"DUMMY$1$A$1637707916782",
    public long nbf;// ":1637707916,
    public long exp;// ":1637711216,
    public long iat;// ":1637707916}
}
