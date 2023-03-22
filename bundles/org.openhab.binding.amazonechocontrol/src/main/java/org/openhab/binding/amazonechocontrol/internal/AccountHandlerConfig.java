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
package org.openhab.binding.amazonechocontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccountHandlerConfig} holds the configuration for the
 * {@link org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerConfig {
    public int discoverSmartHome = 0;
    public int pollingIntervalSmartHomeAlexa = 60;
    public int pollingIntervalSmartSkills = 120;
}
