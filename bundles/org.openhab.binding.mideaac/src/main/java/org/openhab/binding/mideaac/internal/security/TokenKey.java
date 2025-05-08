/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TokenKey} returns the active Token and Key.
 * 
 * @param token For coding/decoding messages
 * @param key For coding/decoding messages
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - JavaDoc and convert to record
 */
@NonNullByDefault
public record TokenKey(String token, String key) {
}
