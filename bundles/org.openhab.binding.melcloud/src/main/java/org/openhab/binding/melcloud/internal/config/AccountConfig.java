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
package org.openhab.binding.melcloud.internal.config;

/**
 * Config class for MELCloud account parameters.
 *
 * @author Pauli Anttila - Initial Contribution
 *
 */
public class AccountConfig {

    public String username;
    public String password;
    public int language;

    @Override
    public String toString() {
        return "[username=" + username + ", password=" + getPasswordForPrinting() + ", languageId=" + language + "]";
    }

    private String getPasswordForPrinting() {
        if (password != null) {
            return password.isEmpty() ? "<empty>" : "*********";
        }
        return "<null>";
    }
}
