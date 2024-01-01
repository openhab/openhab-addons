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
package org.openhab.binding.pushover.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PushoverBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverBindingConstants {

    private static final String BINDING_ID = "pushover";

    public static final ThingTypeUID PUSHOVER_ACCOUNT = new ThingTypeUID(BINDING_ID, "pushover-account");

    public static final String CONFIG_SOUND = "sound";

    public static final String DEFAULT_SOUND = "default";
    public static final String DEFAULT_TITLE = "openHAB";

    public static final String TEXT_OFFLINE_COMMUNICATION_ERROR = "@text/offline.communication-error";
    public static final String TEXT_OFFLINE_CONF_ERROR_MISSING_APIKEY = "@text/offline.conf-error-missing-apikey";
    public static final String TEXT_OFFLINE_CONF_ERROR_MISSING_USER = "@text/offline.conf-error-missing-user";
    public static final String TEXT_OFFLINE_CONF_ERROR_UNKNOWN = "@text/offline.conf-error-unknown";
    public static final String TEXT_ERROR_SKIP_SENDING_MESSAGE = "@text/error.skip-sending-message";
}
