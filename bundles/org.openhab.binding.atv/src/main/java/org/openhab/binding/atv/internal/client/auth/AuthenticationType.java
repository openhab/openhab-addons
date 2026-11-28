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
package org.openhab.binding.atv.internal.client.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Supported HAP authentication types.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum AuthenticationType {

    /** No authentication (just pass through). */
    Null,

    /** Legacy SRP based authentication. */
    Legacy,

    /** Authentication based on HAP (Home-Kit). */
    HAP,

    /** Authentication based on transient HAP (Home-Kit). */
    Transient
}
