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
    public static final ThingTypeUID BRIDGE = new ThingTypeUID(ID, "bridge");
    public static final ThingTypeUID LOCK = new ThingTypeUID(ID, "lock");
    public static final ThingTypeUID CLOUD = new ThingTypeUID(ID, "cloud");
    public static final Set<ThingTypeUID> SUPPORTED = Set.of(BRIDGE, CLOUD, LOCK);
    public static final String C_LOCK = "lock", C_STATE = "state", C_DOOR = "doorState", C_BATTERY = "battery",
            C_CHARGING = "charging", C_JAMMED = "jammed", C_CONNECTED = "connected", C_ACTION = "action";
    public static final String C_AUTO_LOCK_ENABLED = "autoLockEnabled";
    public static final String C_AUTO_LOCK_DELAY = "autoLockDelay";
    public static final String C_AUTO_LOCK_IMPLICIT_ENABLED = "autoLockImplicitEnabled";
    public static final String C_AUTO_LOCK_IMPLICIT_DELAY = "autoLockImplicitDelay";
    public static final String C_PULL_SPRING_ENABLED = "pullSpringEnabled";
    public static final String C_PULL_SPRING_DURATION = "pullSpringDuration";
    public static final String C_AUTO_PULL_SPRING_ENABLED = "autoPullSpringEnabled";
    public static final String C_POSTPONED_LOCK_ENABLED = "postponedLockEnabled";
    public static final String C_POSTPONED_LOCK_DELAY = "postponedLockDelay";
    public static final String C_BUTTON_LOCK_ENABLED = "buttonLockEnabled";
    public static final String C_BUTTON_UNLOCK_ENABLED = "buttonUnlockEnabled";

    private TedeeBindingConstants() {
    }
}
