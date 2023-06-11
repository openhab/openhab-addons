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
package org.openhab.binding.nikohomecontrol.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link NhcJwtToken2} represents the Niko Home Control II hobby API token payload.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
class NhcJwtToken2 {
    String sub = "";
    String iat = "";
    String exp = "";
    String aud = "";
    String iss = "";
    String jti = "";
    List<String> role = List.of();
}
