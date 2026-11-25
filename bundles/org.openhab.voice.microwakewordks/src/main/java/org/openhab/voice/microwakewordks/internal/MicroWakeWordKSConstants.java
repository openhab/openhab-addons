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
package org.openhab.voice.microwakewordks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MicroWakeWordKSConstants} class defines the service identifiers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MicroWakeWordKSConstants {
    public static final String SERVICE_NAME = "microWakeWord";
    public static final String SERVICE_ID = "microwakewordks";
    public static final String SERVICE_CATEGORY = "voice";
    public static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
}
