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
package org.openhab.binding.vesync.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link VeSyncBridgeConfiguration} class contains fields mapping the configuration parameters for the bridge.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncBridgeConfiguration {

    /**
     * The clear text password to access the vesync API.
     */
    @Nullable
    public String password = "";

    /**
     * The email address / username to access the vesync API.
     */
    @Nullable
    public String username = "";

    /**
     * The polling interval to use for air purifier devices.
     */
    @Nullable
    public Integer airPurifierPollInterval;
}
