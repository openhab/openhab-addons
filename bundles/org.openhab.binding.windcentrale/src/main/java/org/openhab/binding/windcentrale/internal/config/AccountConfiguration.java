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
package org.openhab.binding.windcentrale.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration of a Windcentrale account thing.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {
    public static final String USERNAME = "username";
    public String username = "";

    public static final String PASSWORD = "password";
    public String password = "";
}
