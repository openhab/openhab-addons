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
package org.openhab.binding.icloud.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the configuration of an iCloud Account Thing.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
@NonNullByDefault
public class ICloudAccountThingConfiguration {
    public @Nullable String appleId;
    public @Nullable String password;
    public int refreshTimeInMinutes = 10;
}
