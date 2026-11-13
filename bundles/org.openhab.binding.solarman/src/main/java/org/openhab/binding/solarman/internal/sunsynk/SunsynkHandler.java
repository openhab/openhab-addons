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
package org.openhab.binding.solarman.internal.sunsynk;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanLoggerHandler;
import org.openhab.core.thing.Thing;

/**
 * Compatibility entry point for previously configured advanced Thing types.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public class SunsynkHandler extends SolarmanLoggerHandler {
    public SunsynkHandler(Thing thing) {
        super(thing);
    }
}
