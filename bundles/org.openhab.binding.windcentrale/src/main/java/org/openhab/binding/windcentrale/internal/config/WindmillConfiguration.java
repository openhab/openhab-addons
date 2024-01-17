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
 * The configuration of a Windcentrale windmill thing.
 *
 * @author Wouter Born - Initial contribution, add Mill configuration object
 * @author Wouter Born - Add support for new API with authentication
 */
@NonNullByDefault
public class WindmillConfiguration {

    /**
     * Windmill name
     */
    public String name = "";

    /**
     * Refresh interval for refreshing the data in seconds
     */
    public int refreshInterval = 30;

    /**
     * Number of wind shares ("Winddelen")
     */
    public int shares = 1;
}
