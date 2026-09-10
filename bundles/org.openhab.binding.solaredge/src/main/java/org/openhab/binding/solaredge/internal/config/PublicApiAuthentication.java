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
package org.openhab.binding.solaredge.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication methods supported by the SolarEdge Monitoring API V2.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum PublicApiAuthentication {
    API_KEY,
    OAUTH
}
