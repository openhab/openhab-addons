/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tplinkbridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * TP-Link account credentials.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public record TPLinkCredentials(String username, String password) {

    public boolean areSet() {
        return !username.isBlank() && !password.isBlank();
    }

    @Override
    public String toString() {
        return "TPLinkCredentials[username=" + username + ", password=***]";
    }
}
