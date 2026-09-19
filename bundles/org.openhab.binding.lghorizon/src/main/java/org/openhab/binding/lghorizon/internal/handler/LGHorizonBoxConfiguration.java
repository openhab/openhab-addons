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
package org.openhab.binding.lghorizon.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for a {@code box} thing (one physical set-top box).
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonBoxConfiguration {

    // The LG Horizon device id, e.g. as found via discovery.
    public String deviceId = "";

    // Optional: profile id to use for favourite channels/language; defaults to the first profile.
    public String profileId = "";
}
