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
package org.openhab.binding.atv.internal.client.dto;

/**
 * Information about a user account.
 *
 * @param name user name (may be {@code null})
 * @param identifier unique id for the account
 *
 * @author Dan Cunningham - Initial contribution
 */
public record UserAccount(String name, String identifier) {

    @Override
    public String toString() {
        return "Account: " + name + " (" + identifier + ")";
    }
}
