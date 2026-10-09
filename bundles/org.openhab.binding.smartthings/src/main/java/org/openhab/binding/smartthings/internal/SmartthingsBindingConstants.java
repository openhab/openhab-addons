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
package org.openhab.binding.smartthings.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Identifiers for the local Samsung OCF binding.
 *
 * @author Bob Raker - Initial contribution
 * @author Kai Kreuzer - Local-only binding rewrite
 */
@NonNullByDefault
public final class SmartthingsBindingConstants {
    public static final String BINDING_ID = "smartthings";
    public static final ThingTypeUID THING_TYPE_LOCAL_APPLIANCE = new ThingTypeUID(BINDING_ID, "localAppliance");

    private SmartthingsBindingConstants() {
    }
}
