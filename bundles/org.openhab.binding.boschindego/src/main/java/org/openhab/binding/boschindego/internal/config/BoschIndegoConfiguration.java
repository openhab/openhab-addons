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
package org.openhab.binding.boschindego.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for the Bosch Indego thing.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class BoschIndegoConfiguration {
    public @Nullable String username;
    public @Nullable String password;
    public long refresh = 180;
    public long stateActiveRefresh = 30;
    public long cuttingTimeRefresh = 60;
}
