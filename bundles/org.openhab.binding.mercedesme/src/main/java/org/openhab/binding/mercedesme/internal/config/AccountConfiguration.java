/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.config;

import static org.openhab.binding.mercedesme.internal.Constants.NOT_SET;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link AccountConfiguration} for Account Bridge
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    public String email = NOT_SET;
    public String password = NOT_SET;
    public String region = NOT_SET;
    public String pin = NOT_SET;
    public int refreshInterval = 15;
}
