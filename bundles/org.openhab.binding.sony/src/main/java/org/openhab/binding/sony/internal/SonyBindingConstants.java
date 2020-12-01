/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SonyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyBindingConstants {

    /** The binding identifier for all sony products. */
    public static final String BINDING_ID = "sony";

    /** Constants used by discovery */
    public static final String SONY_REMOTEDEVICE_ID = "sony";
    public static final String SONY_SERVICESCHEMA = "schemas-sony-com";
    public static final String DIAL_SERVICESCHEMA = "dial-multiscreen-org";
    public static final String SONY_IRCCSERVICENAME = "IRCC";
    public static final String SONY_DIALSERVICENAME = "dial";
    public static final String SONY_SCALARWEBSERVICENAME = "ScalarWebAPI";

    /** Thing type identifiers */
    public static final String SIMPLEIP_THING_TYPE_PREFIX = "simpleip";
    public static final String DIAL_THING_TYPE_PREFIX = "dial";
    public static final String IRCC_THING_TYPE_PREFIX = "ircc";
    public static final String SCALAR_THING_TYPE_PREFIX = "scalar";

    /** Misc */
    public static final String MODELNAME_VERSION_PREFIX = "_V";

    // The timeout (in seconds) to wait on a response
    public static final Integer RSP_WAIT_TIMEOUTSECONDS = 10;
    public static final Integer THING_CACHECOMMAND_TIMEOUTMS = 120000;

    /** The user agent for communications (and identification on the device) */
    public static final String NET_USERAGENT = "OpenHab/Sony/Binding";
}
