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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Response of {@code POST /customer/auth/sign-in} and {@code POST /customer/auth/refresh}. The
 * access token is short lived (10 minutes); the refresh token is valid for roughly 420 days.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record SignInResponse(@Nullable String idToken, @Nullable String refreshToken) {
}
