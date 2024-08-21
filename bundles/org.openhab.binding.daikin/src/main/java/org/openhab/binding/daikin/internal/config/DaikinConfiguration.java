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
package org.openhab.binding.daikin.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds configuration data for a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial contribution
 * @author Jimmy Tanagra - Add secure, uuid
 *
 */
@NonNullByDefault
public class DaikinConfiguration {
    public static final String HOST = "host";
    public static final String SECURE = "secure";
    public static final String UUID = "uuid";
    public static final String KEY = "key";

    public @Nullable String host;
    public @Nullable Boolean secure;
    public @Nullable String uuid;
    public @Nullable String key;
    public long refresh;
}
