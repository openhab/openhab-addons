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
package org.openhab.binding.pleos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for the Pleos account bridge.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosAccountConfiguration {

    /**
     * Pleos Account clientId.
     **/
    public @Nullable String clientId;

    /**
     * Pleos Account clientSecret.
     **/
    public @Nullable String clientSecret;

    /**
     * Vehicle brand (Hyundai, Kia or Genesis). Always required.
     **/
    public @Nullable String brand;

    /**
     * refreshInterval for the Vehicle Data
     **/
    public int refreshInterval = 15;

    /**
     * Date the clientSecret was created
     */
    public @Nullable String secretCreationDate;
}
