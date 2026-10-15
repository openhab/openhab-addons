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

package org.openhab.binding.tedee.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * 
 * The {@link TedeeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public final class TedeeBindingConstants {
    public static final String ID = "tedee";

    public static final ThingTypeUID BRIDGE = new ThingTypeUID(ID, "local-bridge");
    public static final ThingTypeUID LOCK = new ThingTypeUID(ID, "lock");
    public static final ThingTypeUID CLOUD = new ThingTypeUID(ID, "cloud-bridge");

    public static final Set<ThingTypeUID> SUPPORTED = Set.of(BRIDGE, CLOUD, LOCK);

    public static final String C_LOCK = "lock";
    public static final String C_STATE = "state";
    public static final String C_DOOR = "door-state";
    public static final String C_BATTERY = "battery";
    public static final String C_CHARGING = "charging";
    public static final String C_JAMMED = "jammed";
    public static final String C_CONNECTED = "connected";
    public static final String C_ACTION = "action";

    public static final String C_AUTO_LOCK_ENABLED = "auto-lock-enabled";
    public static final String C_AUTO_LOCK_DELAY = "auto-lock-delay";
    public static final String C_AUTO_LOCK_IMPLICIT_ENABLED = "auto-lock-implicit-enabled";
    public static final String C_AUTO_LOCK_IMPLICIT_DELAY = "auto-lock-implicit-delay";
    public static final String C_PULL_SPRING_ENABLED = "pull-spring-enabled";
    public static final String C_PULL_SPRING_DURATION = "pull-spring-duration";
    public static final String C_AUTO_PULL_SPRING_ENABLED = "auto-pull-spring-enabled";
    public static final String C_POSTPONED_LOCK_ENABLED = "postponed-lock-enabled";
    public static final String C_POSTPONED_LOCK_DELAY = "postponed-lock-delay";
    public static final String C_BUTTON_LOCK_ENABLED = "button-lock-enabled";
    public static final String C_BUTTON_UNLOCK_ENABLED = "button-unlock-enabled";

    private TedeeBindingConstants() {
    }
}
