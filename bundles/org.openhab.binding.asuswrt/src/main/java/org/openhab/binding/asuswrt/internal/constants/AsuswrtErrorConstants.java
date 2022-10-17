/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AsuswrtErrorConstants} class defines error constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtErrorConstants {

    /****************************************
     * LIST OF ERROR MESSAGES
     ****************************************/
    public static final String ERR_CONN_TIMEOUT = "Connection Timeout";
    public static final String ERR_RESPONSE = "Response not okay";
    public static final String ERR_JSON_FOMRAT = "Unexpected or malfomrated JSON-response";
    public static final String ERR_JSON_UNKNOWN_MEMBER = "JSON member not found";
}
