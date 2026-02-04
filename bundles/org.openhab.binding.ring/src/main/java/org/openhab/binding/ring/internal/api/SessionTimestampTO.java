/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Paul Smedley - Initial contribution
 */

@NonNullByDefault
public class SessionTimestampTO {
    @SerializedName("timestamps")
    public @NonNullByDefault({}) Data[] data;

    public static class Data {
        public int doorbot_id;
        public long timestamp;
    }
}
