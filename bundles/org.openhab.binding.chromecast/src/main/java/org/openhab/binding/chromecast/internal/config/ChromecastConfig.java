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
package org.openhab.binding.chromecast.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thing configuration from openHAB.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class ChromecastConfig {
    public @Nullable String ipAddress = null;
    public int port = 8009;
    public long refreshRate = 10;
}
