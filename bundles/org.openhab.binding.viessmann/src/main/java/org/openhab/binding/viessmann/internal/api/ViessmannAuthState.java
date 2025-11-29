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
package org.openhab.binding.viessmann.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ViessmannAuthState} represents that steps in the authorization process.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
enum ViessmannAuthState {
    /*
     * This is the initial state. It indicates that an "authorize" API call is needed to get
     * the Viessmann authcode.
     */
    NEED_AUTH,

    /*
     * This state indicates that a "login" fails
     */
    NEED_LOGIN,

    /*
     * This state indicates that a code request was successful, and that a "token" API
     * call is needed to complete the authorization and get the refresh and access tokens.
     */
    NEED_TOKEN,

    /*
     * This state indicates that a refresh token is needed.
     */
    NEED_REFRESH_TOKEN,

    /*
     * This state indicates that the "authorize" and "token" steps were successful.
     */
    COMPLETE
}
