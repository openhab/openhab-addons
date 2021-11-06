/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EVNotifyConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
public class EVNotifyConfiguration {

    /**
     * AKey
     */
    public @Nullable String akey;

    /**
     * Token
     */
    public @Nullable String token;

    /**
     * Version
     */
    public String version = "V2";

    /**
     * Refresh interval
     */
    public Integer refreshInterval = 120;
}
