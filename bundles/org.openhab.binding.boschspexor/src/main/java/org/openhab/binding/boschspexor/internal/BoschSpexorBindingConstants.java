/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoschSpexorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class BoschSpexorBindingConstants {

    public static final String BINDING_ID = "boschspexor";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SPEXOR_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "spexorAPI");
    public static final ThingTypeUID SPEXOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "spexor");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Set.of(SPEXOR_THING_TYPE, SPEXOR_BRIDGE_TYPE));

    // List of all Channel ids
    public static final String CHANNEL_SPEXOR_INFO = "spexorInfo";
    public static final String CHANNEL_SPEXORS = "spexors";

    // Authorization related Servlet and resources aliases.
    public static final String SPEXOR_OPENHAB_URL = "/spexor";
    public static final String SPEXOR_OPENHAB_RESOURCES_URL = SPEXOR_OPENHAB_URL + "/resources";

    public static final int BACKGROUND_SCAN_REFRESH_MINUTES = 5;

    public static final int OAUTH_EXPIRE_BUFFER = 10;

    public static final String DEVICE_CODE = "deviceCode";
    public static final String PROPERTY_SPEXOR_ID = "deviceID";
    public static final String PROPERTY_SPEXOR_NAME = "deviceName";

    public static final String DEVICE_CODE_REQUEST_TIME = DEVICE_CODE + ".requestTime";
    public static final String DEVICE_CODE_REQUEST_TIME_LIFETIME = DEVICE_CODE + ".lifetime";
    public static final String DEVICE_CODE_REQUEST_INTERVAL = DEVICE_CODE + ".interval";

    public static final String OAUTH_FLOW_AUTHORIZATION_PENDING = "authorization_pending";
    public static final String OAUTH_FLOW_AUTHORIZATION_DECLINED = "authorization_declined";
    public static final String OAUTH_FLOW_BAD_VERIFICATION_CODE = "bad_verification_code";
    public static final String OAUTH_FLOW_EXPIRED_TOKEN = "expired_token";

    public static final String ENDPOINT_SPEXORS = "api/public/spexors/";
    public static final String ENDPOINT_SPEXOR = "api/public/spexor/";

    public static final String getConstantBinding(String constant) {
        return MessageFormat.format("{0}.{1}", BINDING_ID, constant);
    }
}
