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
package org.openhab.binding.ntp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NtpThingConfiguration} is responsible for holding
 * the thing configuration settings
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class NtpThingConfiguration {

    public String hostname = "0.pool.ntp.org";
    public int refreshInterval = 60;
    public int refreshNtp = 30;
    public int serverPort = 123;
    public @Nullable String timeZone;
}
