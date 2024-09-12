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
package org.openhab.binding.ecobee.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EcobeeAuthState} represents that steps in the Ecobee PIN
 * authorization process.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
enum EcobeeAuthState {
    /*
     * This is the initial state. It indicates that an "authorize" API call is needed to get
     * the Ecobee PIN.
     */
    NEED_PIN,

    /*
     * This state indicates that an Ecobee PIN request was successful, and that a "token" API
     * call is needed to complete the authorization and get the refresh and access tokens. In
     * order to get the tokens, the user must authorize the application by entering the PIN
     * into the Ecobee web portal.
     */
    NEED_TOKEN,

    /*
     * This state indicates that the "authorize" and "token" steps were successful.
     */
    COMPLETE
}
