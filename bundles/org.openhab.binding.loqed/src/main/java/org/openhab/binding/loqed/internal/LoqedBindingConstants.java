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
package org.openhab.binding.loqed.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LoqedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedBindingConstants {

    public static final String BINDING_ID = "loqed";

    public static final ThingTypeUID BRIDGE_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID BRIDGE_TYPE_LOCAL = new ThingTypeUID(BINDING_ID, "local-bridge");
    public static final ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");

    public static final String WEBHOOK_PATH = "/loqed/webhook";

    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_BATTERY_TYPE = "battery-type";
    public static final String CHANNEL_BOLT_STATE = "bolt-state";
    public static final String CHANNEL_GUEST_ACCESS = "guest-access";
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_PARTY_MODE = "party-mode";
    public static final String CHANNEL_TOUCH_TO_CONNECT = "touch-to-connect";
    public static final String CHANNEL_TWIST_ASSIST = "twist-assist";

    public static final String PROPERTY_LOCK_ID = "lockId";
    public static final String PROPERTY_MODEL = "model";
}
