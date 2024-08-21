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
package org.openhab.binding.evcc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EvccConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EvccConfiguration {

    /**
     * URL of the evcc instance, e.g. https://demo.evcc.io
     */
    public @Nullable String url;
    /**
     * Interval for state fetching in seconds.
     */
    public int refreshInterval = 60;
}
