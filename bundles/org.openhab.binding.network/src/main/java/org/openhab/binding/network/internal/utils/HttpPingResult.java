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
package org.openhab.binding.network.internal.utils;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Information about the result of an HTTP request which was sent for presence detection.
 *
 * @param statusCode the HTTP status code which was returned by the server
 * @param executionTime the time it took until the response status was received
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public record HttpPingResult(int statusCode, Duration executionTime) {
}
