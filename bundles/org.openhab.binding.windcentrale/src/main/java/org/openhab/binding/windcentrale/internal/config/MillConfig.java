/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The configuration of a Mill thing.
 *
 * @author Wouter Born - Initial contribution, add Mill configuration object
 */
@NonNullByDefault
public class MillConfig {

    /**
     * Windmill identifier
     */
    public int millId = 1;

    /**
     * Refresh interval for refreshing the data in seconds
     */
    public int refreshInterval = 30;

    /**
     * Number of wind shares ("Winddelen")
     */
    public int wd = 1;
}
